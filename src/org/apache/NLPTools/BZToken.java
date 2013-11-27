package org.apache.NLPTools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;

public class BZToken {

	public BZToken() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String word_file = System.getProperty("user.dir") + File.separator + "resources" + File.separator + "word.txt";
	    String abbr_file = System.getProperty("user.dir") + File.separator + "resources" + File.separator + "abbr.txt";
		
	    String output_dir = args[1];
	    String input_dir=args[0];
	    
	    
		
		try{
			File input_path = new File(input_dir);
			File output_path = new File(output_dir);
			
			SentenceBoundary sb=new SentenceBoundary(word_file,abbr_file);
			
			for (File child : input_path.listFiles()) {
		        if (".".equals(child.getName()) || "..".equals(child.getName()))
		            continue; // Ignore the self and parent aliases.
		        if (child.isFile()){ 
		        	
		        	FileInputStream fstream = new FileInputStream(child);
					  
					DataInputStream in = new DataInputStream(fstream);
					//System.out.println("here ");
					  
					BufferedReader br = new BufferedReader(new InputStreamReader(in));
					BufferedWriter bfw=new BufferedWriter(new FileWriter(output_dir+File.separator+child.getName()+".sent"));
					  
					String strLine;
					
					StringBuffer lines=new StringBuffer();  
					while ((strLine = br.readLine()) != null)   {
						strLine=strLine.replace("\t", " ");
						if (strLine.length()==0){
							bfw.write('\n');
							continue;
						}
						Document doc=new Document(strLine,child.getName());
						sb.detect_boundaries(doc);
						bfw.write(doc.boundary_norm_str().replace("\n"," ")+"\n");
						
					}
					br.close();
					bfw.close();
		        }
		        
			}
			
			
		}
		catch (Exception e){
			System.err.println("Error: " + e.getMessage());	 
		}

	}

}
