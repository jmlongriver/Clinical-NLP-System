/**
 * 
 */
package org.apache.RuleEngine;

import java.util.*;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * @author ywu4 This class is used to load all the patterns from the definition
 *         file
 */
public class Patterns {
	// private members
	private Map<String, String> _map = new HashMap<String, String>();

	// public members

	// public functions
	public String expand(String pattern_str) {
		if (!this._map.containsKey(pattern_str)) {
			System.out
					.println("Patter string: " + pattern_str + " not defined");
			System.exit(-1);
		}
		return this._map.get(pattern_str);
	}

	/**
	 * 
	 */
	public Patterns(String dirr) {
		// TODO Auto-generated constructor stub
		ArrayList<String> al = new ArrayList<String>();
		File input_path = new File(dirr);

		try {

			for (File child : input_path.listFiles()) {
				if (".".equals(child.getName()) || "..".equals(child.getName())
						|| child.getName().startsWith(".")
						|| child.getName().endsWith("~"))
					continue; // Ignore the self and parent aliases.
				if (child.isFile()) {
					al.clear();
					System.out.println("Load patterns from file "
							+ child.toString());
					FileInputStream fstream = new FileInputStream(child);

					DataInputStream in = new DataInputStream(fstream);
					// System.out.println("here ");

					BufferedReader br = new BufferedReader(
							new InputStreamReader(in));

					String strLine;
					String section = "";

					while ((strLine = br.readLine()) != null) {
						strLine = strLine.trim();
						// System.out.println(strLine);
						if (strLine.startsWith("//") || strLine.length() == 0) { // remove
																					// commented
																					// line
							continue;
						}
						if (strLine.endsWith(":")) {
							// meet with new section, finish the current regular
							// expression for current section
							if (section.length() == 0) {
								section = strLine.substring(0,
										strLine.length() - 1).trim();
							} else {
								String expression = this.join(al);
								this._map.put(section, expression);
								section = strLine.substring(0,
										strLine.length() - 1).trim();
								al.clear();

							}

						} else {
							al.add(strLine);
						}

					}
					if (al.size() > 0) {
						for (String st : al) {
							String expression = this.join(al);
							this._map.put(section, expression);
						}
					}
				}

			}

		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}

	// join array of strings

	public String join(ArrayList<String> al) {
		StringBuffer sb = new StringBuffer();
		for (String st : al) {
			sb.append(st + "|");
		}

		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	public void print() {
		System.out.println("\n All patterns:");
		System.out.println(this._map.toString());
	}

	// private functions

	/**
	 * @param args
	 */

}
