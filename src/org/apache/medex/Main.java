/***************************************************************************************************************************
 * This software package is developed by Center for Computational Biomedicine at UTHealth which is directed by Dr. Hua Xu. *
 * The participants of development include Hua Xu, Min Jiang, Yonghui Wu, Anushi Shah							           *
 * Version:  1.0                                                                                                           *
 * Date: 01/30/2012                                                                                                        *
 * Copyright belongs to Dr. Hua Xu , all right reserved                                                                    *
 ***************************************************************************************************************************/


package org.apache.medex;

import java.io.File;



public class Main{ 
	static String location = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
	
	static String lexicon_file = location+".."+File.separator + File.separator + "resources" + File.separator + "lexicon.cfg";
	static String rxnorm_file = location+".."+File.separator + File.separator + "resources" + File.separator + "brand_generic.cfg";
	static String code_file = location+".."+File.separator + File.separator + "resources" + File.separator + "code.cfg";
	static String generic_file = location+".."+File.separator + File.separator + "resources" + File.separator + "rxcui_generic.cfg";
	static String norm_file = location+".."+File.separator + File.separator + "resources" + File.separator + "norm.cfg";
	//static String input_dir = System.getProperty("user.dir") + File.separator + "test_input";
	//static String output_dir = System.getProperty("user.dir") + File.separator + "test_output";
	
	static String word_file = location+".."+File.separator + File.separator + "resources" + File.separator + "word.txt";
	static String abbr_file = location+".."+File.separator + File.separator + "resources" + File.separator + "abbr.txt";
	static String grammar_file = location+".."+File.separator + File.separator + "resources" + File.separator + "grammar.txt";
	static String if_detect_sents = "y";
	static String if_freq_norm = "n";
	static String if_drool_engine = "n";
	static String if_offset_showed = "y";
	static String input_dir = "";
	static String output_dir = "";
	
	private static void usage() {
		System.out.println("[Windows] java -cp lib/*;bin org.apache.medex.Main [-i <input directory>]");
		System.out.println("                                         [-o <output directory>]");
		System.out.println("                                         [-b <use sentenceboundary or not, 'y' as default (y/n)>]");
		System.out.println("                                         [-f <normalize frequency or not, 'n' as default (y/n)>]");
		System.out.println("										 [-d <use drool engine or not, 'n' as default (y/n)>]");
		
		System.out.println("[Unix/Linux] java -cp lib/*:bin org.apache.medex.Main [-i <input directory>]");
		System.out.println("                                         [-o <output directory>]");
		System.out.println("                                         [-b <use sentenceboundary or not, 'y' as default (y/n)>]");
		System.out.println("                                         [-f <normalize frequency or not, 'n' as default (y/n)>]");
		System.out.println("										 [-d <use drool engine or not, 'n' as default (y/n)>]");
		System.exit(-1);
	}
	
	private static void checkBoolArgs(String arg){
		if(!arg.equals("y") && !arg.equals("n")){
			System.out.println("Please input valid argument!");
			usage();
		}
	}
	private static void checkDirectory(String dir){
		File folder = new File(dir);  
		if(dir.trim().equals("") || !folder.exists()){
			System.out.println("Please specify valid input and output directory");
			usage();
		}
	}
	
	private static void parseArgs(String args[]) {
		for (int i = 0; i < args.length; ++i) {
			if (args[i].startsWith("-")) {
				switch (args[i].charAt(1)) {
					case 'i':		
						input_dir = args[++i];
						checkDirectory(input_dir);
						break;
					case 'o':		
						output_dir = args[++i];
						checkDirectory(output_dir);
						break;
					case 'b':		
						if_detect_sents = args[++i];
						checkBoolArgs(if_detect_sents);
						break;
					case 'f':
						if_freq_norm = args[++i];
						checkBoolArgs(if_freq_norm);
						break;
					case 'd':
						if_drool_engine = args[++i];
						checkBoolArgs(if_drool_engine);
						break;
					case 'p':
						if_offset_showed = args[++i];
						checkBoolArgs(if_offset_showed);
						break;
					default:
						usage();
				}
			}
		}
		if(input_dir.equals("") || output_dir.equals("")){
			System.out.println("Please specify the input and output directory");
			System.exit(-1);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{
			
			parseArgs(args);
			long start = System.currentTimeMillis();
			//System.out.println(Util.transformMutiNumber("CPM-PSE CR 8-120 MG CPCR"));		
			MedTagger med = new MedTagger(lexicon_file, rxnorm_file, code_file, generic_file, input_dir, output_dir, word_file, abbr_file, grammar_file, if_detect_sents, norm_file, if_freq_norm, if_drool_engine, if_offset_showed);
			med.run_batch_medtag();
			long end = System.currentTimeMillis();
			System.out.println("total time:"+(end-start));
			
		}
		catch(Exception e){
			e.printStackTrace();
		}

		
	}

}
