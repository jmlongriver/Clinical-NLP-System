/**
 * 
 */
package org.apache.NLPTools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.Vector;

import org.apache.algorithms.SuffixArray;
import org.apache.medex.Lexicon;
import org.apache.NLPTools.Document;
/**
 * @author ywu4
 *
 */
public class Main implements Global{

	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String train_path = args[0];
		String model_path = args[1];
		String test_file = args[2];
		String output_file = args[3];
		POSTagger pos = new POSTagger(train_path, model_path);
		pos.train();
		
		try {
			//String test_file = "resources"+File.separator+"POS"+File.separator+"mipacq_test.txt";
			//String output_file = "resources"+File.separator+"POS"+File.separator+"mipacq_out.txt";
			
					
			
			FileInputStream fstream = new FileInputStream(test_file);
			BufferedWriter out = new BufferedWriter(new FileWriter(output_file));
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			// System.out.println(br.read());
			while ((strLine = br.readLine()) != null) {

				// System.out.println("** "+strLine);
				String[] sents = strLine.split(" ");
				String[] tags = pos.tagging(sents);
				String output = "";
				for(int i=0;i<sents.length;i++)
					output +=sents[i]+"_"+tags[i]+ " ";
				output = output.trim();
				out.write(output+"\n");
			}

			// Close the input stream
			in.close();
		} catch (Exception e) {// Catch exception if any
			e.printStackTrace();
		}
	}

}
