package org.apache.TIMEX;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.TIMEX.MatchResult;
import org.apache.TIMEX.NormPattern;
import org.apache.TIMEX.Patterns;
import org.apache.TIMEX.ProcessingEngine;

public class Main {

	public Main() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		   
		ProcessingEngine pe=new ProcessingEngine();
		String fname="/Users/ywu4/project/Medex_Norm_Freq/freq_test_500.txt";
		
		
		try{
			FileInputStream fstream = new FileInputStream(fname);
					  
			DataInputStream in = new DataInputStream(fstream);					  
			BufferedReader br = new BufferedReader(new InputStreamReader(in));			  
			String strLine;
			
			BufferedWriter bfw=new BufferedWriter(new FileWriter(fname+".TX3"));
			
			ArrayList<MatchResult> result=null;
			while ((strLine = br.readLine()) != null)   {
				String[] secs=strLine.split("\t");
				result=pe.extract(secs[0].trim());
				
				String stt="";
				//System.out.println("result size: "+result.size());
				for(MatchResult al:result){
					//System.out.println("al: "+al.match_str+" "+al.norm_str+" "+al.section);
					stt=stt+al.match_str+"|"+al.norm_str+"||";
				}
				if (stt.length() > 0 ){
					stt=stt.substring(0,stt.length()-2);
				}
				stt=strLine.trim()+"\t"+stt.trim();
				bfw.write(stt.toString()+"\n");
			}
			bfw.close();
		}
		catch (Exception e){
			System.err.println("Error: " + e.getMessage());	 
		}
			

	}

}
