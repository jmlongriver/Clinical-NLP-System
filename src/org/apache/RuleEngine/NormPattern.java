/**
 * 
 */
package org.apache.RuleEngine;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ywu4
 * 
 */
public class NormPattern {
	// private member
	private Map<String, Map<String, String>> _map = new HashMap<String, Map<String, String>>();

	/**
	 * 
	 */
	public NormPattern(String dirr) {
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
					System.out.println("Load norm patterns from file "
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
								// build second layer dictionary
								HashMap<String, String> thm = new HashMap<String, String>();
								for (String st : al) {
									String[] words = st.split("=>");
									// System.out.println(words[0]+" "+words[1]);
									String stt1 = words[0].trim();
									String stt2 = words[1].trim();
									thm.put(stt1, stt2);
								}
								this._map.put(section, thm);

								section = strLine.substring(0,
										strLine.length() - 1).trim();
								al.clear();

							}

						} else {
							al.add(strLine);
						}

					}
					if (al.size() > 0) {
						HashMap<String, String> thm = new HashMap<String, String>();
						for (String st : al) {
							String[] words = st.split("=>");
							// System.out.println(words[0]+" "+words[1]);
							String stt1 = words[0].trim();
							String stt2 = words[1].trim();
							thm.put(stt1, stt2);
						}
						this._map.put(section, thm);
					}
				}

			}

		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}

	public void print() {
		System.out.println("\nAll Norm patterns:");
		System.out.println(this._map.toString());
	}

	public String norm(String section, String stt) {
		if (!this._map.containsKey(section)) {
			System.out.println("section not defined in NormPattern");
			System.exit(-1);
		}
		Map<String, String> tmap = this._map.get(section);
		String sttl = stt.toLowerCase();
		if (!tmap.containsKey(sttl)) {
			System.out.println("norm string ## " + stt
					+ " ## not defined for section " + section);
			// System.exit(-1);
			return stt + "_NO_NORM_PATTERN";
		}
		return tmap.get(sttl);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
