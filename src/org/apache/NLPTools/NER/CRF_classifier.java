package org.apache.NLPTools.NER;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Properties;

import org.apache.log4j.FileAppender;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.apache.medex.Lexicon;
import org.apache.medex.Main;
import org.apache.medex.MedTagger;


import org.apache.NLPTools.Document;
import org.apache.NLPTools.Global;
import org.apache.NLPTools.Sentence;
import org.apache.NLPTools.SentenceBoundary;
import org.apache.NLPTools.Token;
import org.apache.algorithms.Classifier;
import org.apache.algorithms.SuffixArray;
import org.apache.commons.lang.StringUtils;

/**
* This class call CRF++ to solve the sequential labeling problem.
* It provides the functions of training and testing with CRF++ on 
* data set from I2B2 NER task.   
*
* @author Min Jiang
*/ 

public class CRF_classifier implements Classifier
{
	public String os;
	public String location;
	
	public Lexicon lex;
	
	protected String modelFile;
	protected String template_file;
	protected String training_feature_file;
	protected String test_feature_file;
	protected Properties prop;
	protected String word_file;
	protected String abbr_file;
	
	protected static Logger logger;
	
	//parameters
	protected  String NOTE_SEPARATOR;
	protected  String EMPTY_VALUE;
	protected  String[] TAGNAMES ;
	protected  String BEGIN_PREFIX ;
	protected  String INTER_PREFIX ;
	protected  String NON_PREDICT;
	protected  String HYPER_PARAM;
	protected  String CUT_OFF;
	
	
	
	public CRF_classifier(String os, String location, String template_file, Properties prop){
		//initialize the parameters
		this.os = os;
		this.location = location;
		this.template_file = template_file;
		this.prop = prop;
		this.word_file = location+ File.separator + "resources" + File.separator + "word.txt";
		this.abbr_file = location+ File.separator + "resources" + File.separator + "abbr.txt";
		this.training_feature_file = location + "resources" + File.separator + "CRF" + File.separator +"training_feature.txt";
		this.test_feature_file = location + "resources" + File.separator + "CRF" + File.separator +"test_feature.txt";
		this.modelFile = location + "resources" + File.separator + "CRF" + File.separator +	 "model_chem.txt";
		this.BEGIN_PREFIX =  prop.getProperty("BEGIN_PREFIX");
		this.INTER_PREFIX =  prop.getProperty("INTER_PREFIX");
		this.NOTE_SEPARATOR = prop.getProperty("NOTE_SEPARATOR");
		this.NON_PREDICT = prop.getProperty("NON_PREDICT");
		this.TAGNAMES = prop.getProperty("TAGNAMES").split(",");
		this.HYPER_PARAM = prop.getProperty("HYPER_PARAM");
		this.CUT_OFF = prop.getProperty("CUT_OFF");
		
		//set logger
		logger = Logger.getLogger(CRF_classifier.class.getName());
		SimpleLayout layout = new SimpleLayout();    
	    try{
	    	lex = new Lexicon(location+"resources" + File.separator + "CRF" + File.separator +"lexicon_3.txt");
	    	FileAppender appender = new FileAppender(layout,"log.txt",false); 
	    	logger.addAppender(appender);
		    logger.setLevel(Level.DEBUG);
	    }
	    catch(Exception e){
	    	e.printStackTrace();
	    }
	    
		
	}
	
	
	/**
	 * Merge all the features that extracted from the document
	 * 
	 * @param features	a list of features
	 * @return a string contains merged features
	 * 
	 * @see CRF_classifier
	 */
	private String mergeFeatures(ArrayList<String> features){
		String mergedFeature = "";
		ArrayList<String[]> feature_lists = new ArrayList<String[]>();
		int length_feature = 0;
		for(int i= 0; i<features.size(); i++){
			String feature = features.get(i);
			String feature_rows[] = feature.split("\n");
			feature_lists.add(feature_rows);
			/*
			logger.debug("one feature ----------------------------------");
			for (int j= 0; j<feature_rows.length; j++){
				logger.debug(feature_rows[j]);
			}*/
			
			int length = feature_rows.length;
			if(length != length_feature && length_feature != 0){
				System.out.println("ERROR: the length of features are not equal");
				System.out.println(length);
				System.out.println(length_feature);
				
				
				System.exit(-1);
			}
			else{
				length_feature = length;
			}
		}
		
		for(int j=0; j<length_feature; j++){
			String feature_line = "";
			for(int i=0; i<feature_lists.size(); i++){
				feature_line += feature_lists.get(i)[j] + "\t";
			}
			mergedFeature += feature_line.trim() + "\n";
		}
		return mergedFeature;
	}
	
	/**
	 * this function is called to create all the features for the given document
	 * 
	 * @param doc an instance of Document
	 * @return feature string
	 * 
	 * @see CRF_classifier
	 */
	public String generatefeature(Document doc){
		ArrayList<String> Features = new ArrayList<String>();
		try{
			//create features
			
			String wordFeature = new WordFeature().createFeatureInstance(doc, prop);
			String prefixFeature = new PrefixFeature().createFeatureInstance(doc, prop);
			String suffixFeature = new SuffixFeature().createFeatureInstance(doc, prop);
			String orthoFeature = new OrthoFeature().createFeatureInstance(doc, prop);
			String posFeature = new POSFeature().createFeatureInstance(doc, prop);
			//String sentenceFeature = new SentenceFeature().createFeatureInstance(doc, prop);
			
			String semanticFeature = new SemanticFeature(lex).createFeatureInstance(doc, prop);
			Features.add(wordFeature);
			Features.add(posFeature);
			Features.add(prefixFeature);
			Features.add(suffixFeature);
			Features.add(orthoFeature);
			Features.add(semanticFeature);
			//Features.add(sentenceFeature);
				
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return mergeFeatures(Features);
			
	}
	
	/**
	 * execute the batch command
	 * 
	 * @param command	command need to be executed
	 * @param outputFile	the path of output file
	 * 
	 * @see CRF_classifier
	 */
	private void executecommand(String command, String outputFile){
		try{
			//System.out.println(command);
			Runtime r = Runtime.getRuntime();
			Process p = r.exec(command);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

	        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
	        BufferedWriter output = null;
	        if(!outputFile.equals("")){
	        	output = new BufferedWriter(new FileWriter(outputFile));
	        }
			//System.out.println("Here is the standard output of the command:\n");
            String s;
		    //System.out.println("output:"+outputFile.toString());
            while ((s = stdInput.readLine()) != null) {
				if(outputFile.equals("")){
					System.out.println(s);
				}
				else{
					output.write(s+"\n");
					output.flush();
				}
            }
            
            // read any errors from the attempted command
            //System.out.println("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }
           
            stdError.close();
			stdInput.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private String createI2B2Output(String text, String phrase, String tag, int startIndex, int endIndex){
		
		if(phrase.endsWith(" ")){
			endIndex = endIndex-1;
		}
		String I2B2output = "c=\""+ phrase.trim().toLowerCase() + "\" ";
		
		//c="a workup" 27:2 27:3||t="test"
		//System.out.println("-------------------");
		//System.out.println(phrase);
		//System.out.println(tag);
		//System.out.println(startIndex);
		//System.out.println(endIndex);
		String lines[] = text.split("\n");
		
		int cursor_start = 0;
		int cursor_end = 0;
		int line_start = 0;
		int line_end = 0;
		int index_start = 0;
		int index_end = 0;
		for(int i=0; i<lines.length; i++){
			if(cursor_start + lines[i].length() + 1 >startIndex){
				line_start = i+1;
				break;
			}
			cursor_start += lines[i].length() + 1; 
		}
		
		for(int i=0; i<lines.length; i++){
			if(cursor_end + lines[i].length() + 1 > endIndex){
				line_end = i+1;
				break;
			}
			cursor_end += lines[i].length() + 1; 
		}
		//System.out.println(startIndex);
		//System.out.println(endIndex);
		//System.out.println(cursor_start);
		//System.out.println(cursor_end);
		//System.out.println(line_start);
		//System.out.println(line_end);
		
		String start_tokens[] = lines[line_start-1].split(" ");
		String end_tokens[] = lines[line_end-1].split(" ");
		//System.out.println(StringUtils.join(start_tokens, " "));
		//System.out.println(StringUtils.join(end_tokens, " "));
		for(int j=0; j<start_tokens.length; j++){
			//System.out.println(start_tokens[j]);
			//System.out.println("J:"+j);
			//System.out.println(cursor_start);
			//System.out.println(end_tokens[j].length());
			if(cursor_start + start_tokens[j].length() >= startIndex){
				
				index_start = j;
				break;
			}
			cursor_start += start_tokens[j].length() + 1;
		}
		
		for(int k=0; k<end_tokens.length+1; k++){
			//System.out.println(end_tokens[k]);
			//System.out.println("K:"+k);
			//System.out.println(cursor_end);
			//System.out.println(end_tokens[k].length());
			if(cursor_end + end_tokens[k].length() >= endIndex){
				index_end = k;
				break;
			}
			cursor_end += end_tokens[k].length()+1;
		}
		
		//System.out.println("index_start:"+index_start);
		//System.out.println("index_end:"+index_end);
		if(phrase.trim() == ""){
			System.out.println(startIndex);
			System.out.println(endIndex);
		}
		I2B2output += String.valueOf(line_start)+":"+ String.valueOf(index_start)+" ";
		I2B2output +=String.valueOf(line_end)+":"+ String.valueOf(index_end);
		I2B2output += "||t=\""+tag+"\"\n";
		
		return I2B2output;
	}
	
	/**
	 * output all the name entities predicted by CRF
	 * 
	 * @param output_file	predict result from CRF
	 * @param doc	an instance of Document to represent the clinical notes
	 * @param tag_file	the path of file that contains all the output entities
	 * 
	 * @see CRF_classifier
	 */
	private void printEntities(String output_file, Document doc, String tag_file, String sent_file, String text){
		try{
			FileInputStream fstream = new FileInputStream(output_file);
			DataInputStream in = new DataInputStream(fstream);
			// System.out.println("here ");
	
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			BufferedWriter tagoutput = new BufferedWriter(new FileWriter(tag_file));
			BufferedWriter sentoutput = new BufferedWriter(new FileWriter(sent_file));
			String strLine;
			
			StringBuffer lines = new StringBuffer();
			while ((strLine = br.readLine()) != null) {
				//System.out.println(strLine);
				lines.append(strLine+"\n");
				//lines.append('\n');
				
			}
			
			String[] sents = lines.toString().split("\n\n");
			
			
			Vector<Sentence> final_sents = doc.sentence();
			Vector<Token> token_list = doc.token_vct();
			for (int sent_index = 0; sent_index < final_sents.size(); sent_index++) {
				Sentence s = final_sents.get(sent_index);
				sentoutput.write(doc.original_txt().substring(s.absStart(), s.absEnd()).replace("\n", "")+"\n");
				int start_token_index = s.startTokenIndex();
				int end_token_index = s.endTokenIndex();
				int length = end_token_index - start_token_index+1;
				
				String output_sent = sents[sent_index];
				String tokens[] = output_sent.split("\n");
				if(tokens.length != length){
					System.out.println("length not equal");
					System.out.println(tokens.length);
					System.out.println(length);
					System.out.println(output_sent);
					System.out.println("-----------------------");
					//for (int cur_token_index = start_token_index; cur_token_index <= end_token_index; cur_token_index++) {
					//tagoutput.write(token_list.get(cur_token_index).str()+"\n");
					//}
					System.exit(-1);
				}
				int index = 0;
				String cur_tagname = "";
				String cur_tag_output = "";
				int cur_start = 0;
				int cur_end =0;
				boolean[] inTag = new boolean[TAGNAMES.length];
				for(int i=0; i<TAGNAMES.length; i++){
					inTag[i] = false;
				}
				
				for (int cur_token_index = start_token_index; cur_token_index <= end_token_index; cur_token_index++) {
					Token cur_token = token_list.get(cur_token_index);
					
					//String Token_str = cur_token.str();
					
					String fields[] = tokens[index].trim().split("\t");
					String tag = fields[fields.length-1];
					
					if(tag.equals(NON_PREDICT)){
						if(!cur_tag_output.equals("")){
							//tagoutput.write(cur_tag_output.trim() + "\t"+cur_tagname +"\t" +cur_start +"\t"+ (cur_start+cur_tag_output.trim().length()-1)+"\n");
							tagoutput.write(cur_tag_output.trim()+"\n");
							//tagoutput.write(createI2B2Output(text, cur_tag_output, cur_tagname, cur_start, (cur_start+cur_tag_output.trim().length()-1)));
							cur_start = 0;
							cur_end = 0;
							cur_tagname = "";
							cur_tag_output = "";
							for(int i=0; i<TAGNAMES.length; i++){
								inTag[i] = false;
							}
									
						}
					}
					
					for(int i=0; i<TAGNAMES.length; i++){
						if(tag.equals(BEGIN_PREFIX + TAGNAMES[i])){
							if(!cur_tag_output.equals("")){
								//tagoutput.write(cur_tag_output.trim() + "\t"+cur_tagname +"\t" +cur_start +"\t"+ (cur_start+cur_tag_output.trim().length()-1)+"\n");
								tagoutput.write(cur_tag_output.trim()+"\n");
								//tagoutput.write(createI2B2Output(text, cur_tag_output, cur_tagname, cur_start, (cur_start+cur_tag_output.trim().length()-1)));
								cur_start = 0;
								cur_end = 0;
								cur_tagname = "";
								cur_tag_output = "";
								for(int j=0; j<TAGNAMES.length; j++){
									inTag[j] = false;
								}
							}
							
							inTag[i] = true;
							cur_tag_output += fields[0] + " ";
							cur_tagname = TAGNAMES[i] ;
							cur_start = cur_token.startPos();
							cur_end = cur_token.endPos();
						}
						
						if(tag.equals(INTER_PREFIX + TAGNAMES[i])){
							if(inTag[i]){
								cur_tag_output += fields[0] + " ";
								cur_end = cur_token.endPos();
							}
							else{
								//tagoutput.write(cur_tag_output.trim() + "\t"+cur_tagname +"\t" +cur_start +"\t"+ (cur_start+cur_tag_output.trim().length()-1)+"\n");
								if(!cur_tag_output.equals("")){
									tagoutput.write(cur_tag_output.trim()+"\n");
									//tagoutput.write(createI2B2Output(text, cur_tag_output, cur_tagname, cur_start, (cur_start+cur_tag_output.trim().length()-1)));
									cur_start = 0;
									cur_end = 0;
									cur_tagname = "";
									cur_tag_output = "";
									for(int k=0; k<TAGNAMES.length; k++){
										inTag[k] = false;
									}
								}
							}
						}
						
					}
					
					//System.out.println(tokens[index].trim() +"\t"+ cur_token.startPos()+ "\t"+cur_token.endPos());
					index ++;
				}
				
			}
			sentoutput.close();
			tagoutput.close();
			br.close();
			in.close();
			fstream.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	/**
	 * call this method to train on the training file to build the prediction model 
	 * 
	 * @param trainingfile	the path of the file for training
	 * 
	 * @see CRF_classifier
	 */
	public void train(String trainingFile){
		
		
		String command;
		if(os.startsWith("Windows")){
			command = location + "lib\\CRF\\windows\\crf_learn -f "+ this.HYPER_PARAM + " -c " + this.CUT_OFF + " "+template_file + " "+trainingFile + " "+modelFile;
		}
		else{
			command = location + "lib/CRF/crf_learn -f "+ this.HYPER_PARAM + " -c " + this.CUT_OFF+" "+template_file + " "+trainingFile + " "+modelFile;
		}
		//System.out.println("start training...");
		executecommand(command, "");
		setupModel(modelFile);
	}
	
	/**
	 * output the start and end position of given phrase
	 * 
	 * @param filetext	text of note
	 * @param start_line index of line that contains the start token 
	 * @param end_line	index of line that contains the end token 
	 * @param token_start	index of start token in the phrase
	 * @param token_end	index of end token in the phrase 
	 * @return [start position of phrase, end position]
	 * 
	 * @see CRF_classifier
	 */
	private int[] getIndex(String filetext, int start_line, int end_line, int token_start, int token_end){
		//System.out.println("-----------------------");
		String lines[] = filetext.split("\n");
		int[] return_index = new int[2];
		int cursor_start = 0;
		int cursor_end = 0;
		for(int i=0; i<start_line-1; i++){
			cursor_start += lines[i].length()+1;
		}
		
		for(int i=0; i<end_line-1; i++){
			cursor_end += lines[i].length()+1;
		}
		//System.out.println(cursor_start);
		//System.out.println(cursor_end);
		
		String start_tokens[] = lines[start_line-1].split(" ");
		String end_tokens[] = lines[end_line-1].split(" ");
		//System.out.println(StringUtils.join(start_tokens, " "));
		//System.out.println(StringUtils.join(end_tokens, " "));
		for(int j=0; j<token_start; j++)
			cursor_start += start_tokens[j].length() + 1;
		
		for(int k=0; k<token_end+1; k++){
			//System.out.println(end_tokens[k]);
			cursor_end += end_tokens[k].length()+1;
		}
		return_index[0] = cursor_start;
		return_index[1] = cursor_end-1;
		
		return return_index;
	}
	
	/**
	 * call this method to generate the training file for the 2010 i2b2 challenge
	 * 
	 * @param train_dir	   the path of the clinical notes for generating features
	 * @param train_file	the path of the training file generated
	 * 
	 * @see CRF_classifier
	 */
	public void generateChemTraining2(String train_dir, String train_file){
		
		try{
			//System.out.println("Creating annotatation corpus...");
			File input_path = new File(train_dir);
			BufferedWriter output = new BufferedWriter(new FileWriter(train_file));
			
			for (File child : input_path.listFiles()) {
				System.out.println("Building training file on "+ child.getName() + "...");
				FileInputStream fstream = new FileInputStream(train_dir + File.separator+ child.getName());
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				
				String text = "";
				String strLine;
				
				StringBuffer lines = new StringBuffer();
				while ((strLine = br.readLine()) != null) {
					//System.out.println(strLine);
					lines.append(strLine+"\n");
					//lines.append('\n');
					text+= strLine+"\n";
				}
				
				Document doc = new Document(lines.toString(), child.getName());
				String output_str = "";
				String feature = generatefeature(doc);
				//String features[] = feature.split("\n");
				
				output.write(child+"\n");
				output.write(feature+"\n");
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 * call this method to generate the training file for the 2010 i2b2 challenge
	 * 
	 * @param train_dir	   the path of the clinical notes for generating features
	 * @param train_file	the path of the training file generated
	 * 
	 * @see CRF_classifier
	 */
	public void generateChemTraining(String train_dir, String train_file){
		
		try{
			//System.out.println("Creating annotatation corpus...");
			File input_path = new File(train_dir);
			BufferedWriter output = new BufferedWriter(new FileWriter(train_file));
			//FileInputStream fstream_bio = new FileInputStream(location+ "\\resources"+ File.separator + "CRF" + File.separator +"chem_BIO.txt");
			//DataInputStream in_bio = new DataInputStream(fstream_bio);
			//BufferedReader br_bio = new BufferedReader(new InputStreamReader(in_bio));
			//String tag = br_bio.readLine();
			//System.out.println(tag);
			/*
			String items[] = tag.split("\t");
			for (int i=0; i<items.length; i++)
				System.out.println(items[i]+"|");
			*/
			for (File child : input_path.listFiles()) {
				System.out.println("Building training file on "+ child.getName() + "...");
				FileInputStream fstream = new FileInputStream(train_dir + File.separator+ child.getName());
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				
				String text = "";
				String strLine;
				
				StringBuffer lines = new StringBuffer();
				while ((strLine = br.readLine()) != null) {
					//System.out.println(strLine);
					lines.append(strLine+"\n");
					//lines.append('\n');
					text+= strLine+"\n";
				}
				
				Document doc = new Document(lines.toString(), child.getName());
				String output_str = "";
				String feature = generatefeature(doc);
				//String features[] = feature.split("\n");
				
				/*
				for(int k=0; k<features.length; k++){
					String tag = br_bio.readLine();
					//System.out.println(tag);
					String items[] = tag.split("\t");
					if(items.length == 2) 
						output_str += features[k]+"\t"+items[1].trim()+"\n";
					else{
						if(items.length > 2){
							for (int i=0; i<items.length; i++)
								System.out.println(items[i]+"|");
							System.exit(-1);
						}
						output_str += "\n";
					}
				}
				br_bio.readLine();*/
				output.write(feature+"\n");
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
	}
	/**
	 * call this method to generate the training file for the 2010 i2b2 challenge
	 * 
	 * @param train_dir	   the path of the clinical notes for generating features
	 * @param train_file	the path of the training file generated
	 * 
	 * @see CRF_classifier
	 */
	public void generateI2b2Training(String train_dir, String train_file){
		
		try{
			//System.out.println("Creating annotatation corpus...");
			File input_path = new File(train_dir);
			BufferedWriter output = new BufferedWriter(new FileWriter(train_file));
			for (File child : input_path.listFiles()) {
				System.out.println("Building training file on "+ child.getName() + "...");
				//read text
				
				FileInputStream fstream = new FileInputStream(train_dir + File.separator+ child.getName());
				String con_file_name = child.getName().replace(".txt", ".con");
				FileInputStream fstream_con = new FileInputStream("C:\\vandy\\Project\\I2B2_2010\\DATA\\GOLDEN_DATA\\concept\\" + con_file_name);
				
				
				
				DataInputStream in = new DataInputStream(fstream);
				DataInputStream in_con = new DataInputStream(fstream_con);
				// System.out.println("here ");
		
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				BufferedReader br_con = new BufferedReader(new InputStreamReader(in_con));
				String text = "";
				String strLine;
				
				StringBuffer lines = new StringBuffer();
				while ((strLine = br.readLine()) != null) {
					//System.out.println(strLine);
					lines.append(strLine+"\n");
					//lines.append('\n');
					text+= strLine+"\n";
				}
				
				Document doc = new Document(lines.toString(), child.getName());
				
				String feature = generatefeature(doc);
				
				
				
				
				HashMap<Integer, String> hashmap = new HashMap<Integer, String>();
				lines = new StringBuffer();
				while ((strLine = br_con.readLine()) != null) {
					
					String regexp = "c=\"(.*?)\" (\\d+):(\\d+) (\\d+):(\\d+)\\|\\|t=\"(.*?)\""; 
					Pattern pattern = Pattern.compile(regexp);
				    Matcher m = pattern.matcher(strLine);
				    String phrase = "";
				    int start_line = 0;
				    int end_line = 0;
				    int start_index = 0;
				    int end_index = 0;
				    String tag = "";
				    while(m.find()){
				    	phrase = m.group(1);
				    	start_line = Integer.parseInt(m.group(2));
				    	start_index = Integer.parseInt(m.group(3));
				    	end_line = Integer.parseInt(m.group(4));
				    	end_index = Integer.parseInt(m.group(5));
				    	tag = m.group(6);
				    }
				    int whole_index[] = getIndex(text.trim(), start_line, end_line, start_index, end_index);
				    
				    
				    
				    for(int k=whole_index[0]; k<=whole_index[1]; k++){
				    	if(k==whole_index[0]){
				    		hashmap.put(k, prop.getProperty("BEGIN_PREFIX")+tag);
				    	}
				    	else{
				    		hashmap.put(k, prop.getProperty("INTER_PREFIX")+tag);
				    	}
				    }
				    //System.out.println(whole_index[0]);
				    //System.out.println(whole_index[1]);
				    //System.out.println(text.substring(whole_index[0], whole_index[1]));
				    
				}
				
				//System.out.println("end reading anntotation");
				String out_tag = "";
				Vector<Sentence> final_sents = doc.sentence();
				Vector<Token> token_list = doc.token_vct();
				for (int sent_index = 0; sent_index < final_sents.size(); sent_index++) {
					Sentence s = final_sents.get(sent_index);
					int start_token_index = s.startTokenIndex();
					int end_token_index = s.endTokenIndex();
					String[] token_strs = new String[end_token_index-start_token_index+1];
					
					for (int cur_token_index = start_token_index; cur_token_index <= end_token_index; cur_token_index++) {
						Token cur_token = token_list.get(cur_token_index);
						int start_index = cur_token.startPos();
						if(hashmap.containsKey(start_index)){
							out_tag  += hashmap.get(start_index) + "\n";
						}
						else{
							out_tag += "O\n";
						}
						
					}
					out_tag += "\n";
				}
				String tags[] = out_tag.split("\n");
				String features[] = feature.split("\n");
				//System.out.println(out_tag.split("\n").length);
				//System.out.println(feature.split("\n").length);
				String output_str = "";
				for(int k=0; k<tags.length; k++){
					output_str += features[k]+"\t"+tags[k]+"\n";
				}
				output_str+="\n\n";
				output.write(output_str);
				output.flush();
				
			}
			output.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	protected void setupModel(String modelFile){
		this.modelFile = modelFile;
	}
	
	protected void setTrainFeature(String trainfeature){
		this.training_feature_file = trainfeature;
	}
	
	protected void setTestFeature(String testfeature){
		this.modelFile = modelFile;
	}
	
	/**
	 * call this method to predict on test data
	 * 
	 * @param test_dir	the path of directory that contains test files 
	 * @param output_dir	the path of directory that contains predict results
	 * 
	 * @see CRF_classifier
	 */
	public void predict(String test_dir, String output_dir){
		try{
			File input_path = new File(test_dir);
			for (File child : input_path.listFiles()) {
				System.out.println("predict on "+child+"...");
				//read text
				FileInputStream fstream = new FileInputStream(test_dir + File.separator+ child.getName());
		
				DataInputStream in = new DataInputStream(fstream);
				// System.out.println("here ");
		
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String strLine;
		
				StringBuffer lines = new StringBuffer();
				String txt = "";
				while ((strLine = br.readLine()) != null) {
					//System.out.println(strLine);
					lines.append(strLine+"\n");
					//lines.append('\n');
					txt += strLine + "\n";
				}
				
				Document doc = new Document(lines.toString(), child.getName());
				//SentenceBoundary sc = new SentenceBoundary(this.word_file, this.abbr_file);
				//sc.detect_boundaries(doc);
				
				String feature = generatefeature(doc);
				//System.out.println(child.getName());
				//System.out.println("feature:"+feature);
				String feature_file = output_dir + File.separator + "feature_"+child.getName();
				//System.out.println(feature_file);
				
				BufferedWriter output = new BufferedWriter(new FileWriter(feature_file));
				output.write(feature);
				output.flush();
				String command;
				
				if (feature_file.indexOf(" ") >= 0){
					feature_file = "\""+feature_file + "\"";
				}
				if(os.startsWith("Windows")){
					command = location + "lib\\CRF\\windows\\crf_test -m "+" "+this.modelFile + " "+feature_file;
				}
				else{
					command = location + "lib/CRF/crf_test  -m "+" "+this.modelFile + " "+feature_file;
				}
				String output_file = output_dir+File.separator+child.getName()+".out";
				String tag_file =  output_dir+File.separator+child.getName()+".tag";
				String sent_file =  output_dir+File.separator+child.getName()+".sent";
				System.out.println(command);
				executecommand(command, output_file);
				
				printEntities(output_file, doc, tag_file, sent_file, txt.trim());
			}
		}
		catch(Exception e){
			System.out.println(e.toString());
		}
		
	}
}
