package org.apache.medex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.StringTokenizer; 

import org.apache.TIMEX.MatchResult;
import org.apache.TIMEX.ProcessingEngine;
import org.apache.algorithms.VectorSpaceModel;

import org.apache.NLPTools.Document;
import org.apache.NLPTools.Global;
import org.apache.NLPTools.Sentence;
import org.apache.NLPTools.SentenceBoundary;
import org.apache.NLPTools.Token;
import org.apache.NLPTools.CFGparser.EarleyParser;
import org.apache.NLPTools.CFGparser.TreeNode;
import org.apache.NLPTools.CFGparser.med_parser_grammar;
import org.apache.NLPTools.Global.SuffixArrayResult;
import org.apache.NLPTools.Global.TextSectionType;
import org.apache.algorithms.SuffixArray;
import org.apache.commons.lang.StringUtils;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.javatuples.Quintet;

/**
 * This class implements a semantic tagger for medication
 */
public class MedTagger {

	BufferedWriter out; // output file for tag results
	BufferedWriter out2; // output file for detected sentence
	BufferedWriter log; // output file for log
	boolean ifLog = true;
	ArrayList<Quartet<String, String, Integer, Pair<Pair<String, Integer>, Integer>>> token_tags_temp;
	ArrayList<Quartet<String, String, Integer, Pair<Pair<String, Integer>, Integer>>> token_tags;
	/**
	 * dictionary that stores RXNORM information including drug, brand and
	 * generic name etc.
	 */
	HashMap<String, ArrayList<String>> rx_norm_map;
	/*
	 * dictionary that stores the mapping information between drug and
	 * code(e.g., UMLS code, RXNORM code etc.)
	 */
	HashMap<String, ArrayList<String>> rx_code_map;
	HashMap<String, HashMap<String, Integer>> rx_form_map;
	HashMap<String, String> normalization_map;
	HashMap<String, String> rx_code_name_map;
	
	/*
	 * dictionary that stores the mapping information between RXCUI and generic
	 * name
	 */
	HashMap<String, String> rx_generic_map;
	/*
	 * lower case text of current sentence
	 */
	String sent_lower;

	/*
	 * section name of current sentence
	 */
	String section;
	
	Encoder coder;
	Util utility;

	// immediate results for each sentence during the tagging process
	String[] sent_token_array;
	String[] sent_tag_array;
	int[] sent_token_start_array;
	int[] sent_token_end_array;
	int[] sent_tag_index_array;

	// input for sentence boundary
	String word_file;
	String abbr_file;
	SentenceBoundary sb;

	// lexicon file for tagging
	String lex_fname;
	Lexicon lex;
	String if_freq_norm;
	String if_drool_engine;
	String if_offset_showed;
	String if_detect_sents; // if sentence boundary functionality is called or
							// not
	String input_dir; // input directory that contains clinical notes
	String output_dir; // output directory that contains the tagging output
	String rx_norm_file; // name of RXNORM dictionary file that contains mapping
							// information among drug, brand and generic name
	String rx_code_file; // name of coding dictionary file that stores the
							// coding mapping information
	String norm_file;
	
	String rx_generic_file; // name of dictionary file that contains the mapping
							// between RXCUIs and generic name

	String grammar_file;// grammar file for semantic parsing
	SemanticRuleEngine rule_engine; // object of SemanticRuleEngine for handling
									// disambiguation and transformation

	ProcessingEngine freq_norm_engine; 

	// store the tagging result for each type of tag in signature
	String drug;
	String brand;
	String dose_form;
	String strength;
	String dose_amt;
	String route;
	String frequency;
	String duration;
	String necessity;
	String generic_name;
	String generic_code;

	

	
	

	// store the coding information generated for current signature
	static String umls_code;
	static String rx_code;

	public MedTagger(String lexicon_path, String rxnorm_path, String code_path,
			String generic_path, String input_path, String output_path,
			String word_file, String abbr_file, String grammar_file,
			String if_detect_sents, String norm_map_file, String if_freq_norm, String if_drool_engine, String if_offset_showed) {

		this.lex_fname = lexicon_path;
		this.rx_norm_file = rxnorm_path;
		this.rx_code_file = code_path;
		this.rx_generic_file = generic_path;

		this.input_dir = input_path;
		this.output_dir = output_path;
		this.word_file = word_file;
		this.abbr_file = abbr_file;
		this.grammar_file = grammar_file;
		this.if_detect_sents = if_detect_sents;
		this.if_freq_norm = if_freq_norm;
		this.if_drool_engine = if_drool_engine;
		this.if_offset_showed = if_offset_showed;
		this.norm_file = norm_map_file;
		
		
		// initialization process including loading lexicon, create the object
		// of SentenceBoundary and SemanticRuleEngine etc.
		try {
			//long t1 = System.nanoTime();
			System.out.println("Loading configuration files ...");
			rx_norm_map = new HashMap<String, ArrayList<String>>();
			loadRXNorm(rx_norm_file);
			//System.out.println("Loading rxnorm finished");
			rx_code_map = new HashMap<String, ArrayList<String>>(60);
			rx_form_map = new HashMap<String, HashMap<String, Integer>>();
			normalization_map = new HashMap<String, String>();
			rx_code_name_map = new HashMap<String, String>();
			
			loadNormMapping(norm_file);
			//long t5 = System.nanoTime();
			//System.out.println("load norm mapping and rxnorm:" + (t5 - t1));
			//System.out.println("Loading normalization mapping finished");
			this.utility = new Util(normalization_map);
			//System.out.println("Creat util finished");
			//System.out.println("finished load norm mapping");
			//System.out.println(normalization_map.size());
			try{
				loadRXCoding(rx_code_file);
			}catch(Exception e){
				e.printStackTrace();
			}
			//System.out.println("finished load rxcoding");
			
			//long t4 = System.nanoTime();
			//System.out.println("load rxcoding:" + (t4 - t5));
			
			rx_generic_map = new HashMap<String, String>();
			loadGeneric(generic_path);
			
			//long t3 = System.nanoTime();
			
			//System.out.println("load generic:" + (t3 - t4));
			this.coder = new Encoder(rx_code_map, rx_code_name_map, normalization_map, rx_generic_map);
			
			String s[] = new String[9];
			s[0] = "blood product FFF DRUG";
			s[1] = "";
			s[2] = "";
			s[3]="";
			s[4] = "";
			s[5]="";
			s[6]="";
			s[7] = "";
			s[8] = "";
			
			this.log = new BufferedWriter(new FileWriter(
					System.getProperty("user.dir") + File.separator + "log"+File.separator+"0.txt"));
			//long t111 = System.nanoTime();
			//
			//for (int i=0; i<1000; i++){
			//coder.encode(s, log);
			//}
			
			//long t112 = System.nanoTime();
			//System.out.println("encoding:" + (t112 - t111));
			//System.out.println("00000000000000000000000000");
			
			
			this.lex = new Lexicon(this.lex_fname);
			// System.out.println("Loading lexicon for sentence boundary...");
			this.sb = new SentenceBoundary(this.word_file, this.abbr_file);
			this.rule_engine = new SemanticRuleEngine();
			this.freq_norm_engine = new ProcessingEngine();
			
			//long t2 = System.nanoTime();
			//System.out.println("Load lexicon cost:" + (t2 - t4));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Call function of "medtagging" to parse every note in the input directory
	 * 
	 * @see MedTagger
	 */
	public void run_batch_medtag() {
		
		File input_path = new File(this.input_dir);
		File output_path = new File(this.output_dir);
		try {

			int note_num = 0;
			for (File child : input_path.listFiles()) {
				// System.out.println(child.getName());
				if (".".equals(child.getName()) || "..".equals(child.getName()))
					continue; // Ignore the self and parent aliases.
				if (child.isFile()) {
					System.out.println("Processing file " + child.getName() + " ...");
					FileInputStream fstream = new FileInputStream(child);

					DataInputStream in = new DataInputStream(fstream);
					// System.out.println("here ");

					BufferedReader br = new BufferedReader(
							new InputStreamReader(in));
					String strLine;

					StringBuffer lines = new StringBuffer();
					while ((strLine = br.readLine()) != null) {
						lines.append(strLine);
						
						lines.append('\n');
					}
				

					if (lines.toString().equals("")) {
						continue;
					}

					Document doc = new Document(lines.toString(),
							child.getName());
					br.close();
					in.close();
					fstream.close();
					lines = null;

					strLine = null;
					
					medtagging(doc);
					doc = null;

					if (note_num == 100) {
						note_num = 0;
						System.gc();
						// System.out.println("gc was called");
					}

					/*
					 * Vector<Vector<DrugTag>> sigs = doc.signature(); for(int
					 * i=0;i<sigs.size();i++){ for(int
					 * j=0;j<sigs.get(i).size();j++){
					 * System.out.println(sigs.get
					 * (i).get(j).str()+"|"+sigs.get(i
					 * ).get(j).type()+"|"+sigs.get
					 * (i).get(j).startPos()+"|"+sigs.get(i).get(j).endPos()); }
					 * }
					 */
					note_num += 1;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * write log if the flag is set
	 * 
	 * @param msg
	 */
	public void writeLog(String msg) {
		if (ifLog) {
			try {
				log.write(msg);
			} catch (IOException e) {
				System.out.println(e.toString());
			}

		}
	}

	/**
	 * <p>
	 * Incorporates main logic of drug identification with its signature for the
	 * given text.
	 * </p>
	 * <ul>
	 * <li>Detect sentences in the given document</li>
	 * <li>For each sentence</li>
	 * <li><i>Identify section name based on rules</i></li>
	 * <li><i>Generate semantic tags by calling function of "transform" from
	 * class "SemanticRuleEngine"</i></li>
	 * <li><i>Break semantic tag list into signature list</i></li>
	 * <li><i>For each signature list: 1> call "EarleyParser" to do semantic
	 * parsing; 2> change output format of the signatures and print out them</i>
	 * </li>
	 * </ul>
	 * 
	 * @param doc
	 *            a document object built based on a clinical note
	 * 
	 * @see MedTagger
	 */
	public void medtagging(Document doc) {

		// read from clinical notes
		// Document doc = new Document("");

		try {

			this.out = new BufferedWriter(new FileWriter(output_dir
					+ File.separator + doc.fname()));
			this.out2 = new BufferedWriter(new FileWriter(
					System.getProperty("user.dir") + File.separator + "sents"
							+ File.separator + doc.fname()));
			this.log = new BufferedWriter(new FileWriter(
					System.getProperty("user.dir") + File.separator + "log"
							+ File.separator + doc.fname()));
			this.section = "NOMED";

			// detect sentences in the document
			writeLog("----------------------------------------------\n");
			writeLog(doc.fname() + "\n");
			//long t1 = System.nanoTime();

			if (if_detect_sents.equals("y")) {
				this.sb.detect_boundaries(doc);
				doc.break_long_sentence();
			}

			// semantic tag look-up search
			SuffixArray sa = new SuffixArray(doc, lex, '\0',
					Global.SuffixArrayMode.WORD,
					Global.SuffixArrayCaseMode.NON_SENSITIVE);
			Vector<SuffixArrayResult> result = sa.search();
			///long t2 = System.nanoTime();
			//System.out.println("sentence boundary cost:"+(t2-t1));
			// read the search results and stored them into structure of
			// "Document"

			for (int i = 0; i < result.size(); i++) {
				String str = doc.get_str_by_token(result.get(i).start_token,
						result.get(i).end_token);
				DrugTag dt = new DrugTag(result.get(i).start_pos,
						result.get(i).end_pos, TextSectionType.DRUG, str,
						result.get(i).start_token, result.get(i).end_token,
						result.get(i).semantic_type);
				
				doc.add_drug_tag(dt);

			}
			doc.filter_overlapped_drug_tag();
			writeLog("*********\n");
			// writeLog(tag_dict.toString()+"\n");

			Vector<DrugTag> filter_tags = doc.filtered_drug_tag();
			HashMap<Integer, Pair<String, Integer>> tag_dict = new HashMap<Integer, Pair<String, Integer>>();

			for (int j = 0; j < filter_tags.size(); j++) {
				for (int k = filter_tags.get(j).start_token(); k <= filter_tags.get(j).end_token(); k++) {
					tag_dict.put(k, Pair.with(filter_tags.get(j).semantic_tag(), j));

				}
			}

			Vector<Token> token_list = doc.token_vct();
			Iterator<Token> itoken = token_list.iterator();

			Vector<Sentence> final_sents = doc.sentence();
			

			// Iterate each sentence
			System.out.println(final_sents.size());
				
			for (int sent_index = 0; sent_index < final_sents.size(); sent_index++) {// handle
																						// sentence
																						// by
																						// sentence
				//long tsent0 = System.nanoTime();
				Sentence s = final_sents.get(sent_index);
				
				int start_token_index = s.startTokenIndex();
				int end_token_index = s.endTokenIndex();

				// get result from look-up tagger and store them
				// System.out.println("-------");
				// System.out.println(end_token_index);
				// System.out.println(start_token_index);
				sent_token_array = new String[end_token_index
						- start_token_index + 1];
				sent_tag_array = new String[end_token_index - start_token_index
						+ 1];
				sent_token_start_array = new int[end_token_index
						- start_token_index + 1];
				sent_token_end_array = new int[end_token_index
						- start_token_index + 1];
				sent_tag_index_array = new int[end_token_index
						- start_token_index + 1];
				int sent_token_index = 0;
				String sent_text = "";
				for (int cur_token_index = start_token_index; cur_token_index <= end_token_index; cur_token_index++) {
					// System.out.println(cur_token_index);
					String tag = "TK";
					int tag_index = 0;
					if (tag_dict.containsKey(cur_token_index)) {
						tag = tag_dict.get(cur_token_index).getValue0();
						tag_index = tag_dict.get(cur_token_index).getValue1();
					}

					Token cur_token = token_list.get(cur_token_index);
					String Token_str = cur_token.str();

					sent_token_array[sent_token_index] = Token_str;
					sent_tag_array[sent_token_index] = tag;
					sent_token_start_array[sent_token_index] = cur_token
							.startPos();
					sent_token_end_array[sent_token_index] = cur_token.endPos();
					sent_tag_index_array[sent_token_index] = tag_index;
					writeLog(cur_token_index + "\t" + Token_str + "\t" + tag
							+ "\t" + cur_token.startPos() + "\t"
							+ cur_token.endPos() + "\n");
					sent_text += Token_str + " ";
					sent_token_index++;

				}

				sent_text = sent_text.trim();
				writeLog("-----------------------------------------------------\n");
				writeLog("SENTENCENO" + sent_index + "\n");
				writeLog(sent_text + "\n");
				out2.write(sent_text + "\n");
				//System.out.println("\n"+sent_text);
				// System.out.println("_____________________________________\n"+sent_text);
				// sent = sent.replace("\r"," ");
				// sent = sent.replace("\n", " ");
				sent_lower = sent_text.toLowerCase();
				// System.out.println("sent_lower :"+sent_lower);

				ArrayList<Pair<String, Integer>> sent_lower_map = new ArrayList<Pair<String, Integer>>();
				String[] sent_lower_split = sent_lower.split(" ");
				for (int j = 0; j < sent_lower_split.length; j++) {
					Pair<String, Integer> pair_a1 = Pair.with(
							sent_lower_split[j], j);
					sent_lower_map.add(pair_a1);
				}
				// System.out.println(sent_lower_map);

				// extract section information
				int section_index = sent_text.indexOf(":");
				if (section_index >= 0) {
					// writeLog("change section!\n");
					String section_name = sent_text.substring(0, section_index);
					// writeLog("section name:"+section_name);

					if (sent_lower.indexOf("medication") >= 0)
						section = "MED";
					else if (sent_lower.indexOf("allerg") >= 0)
						section = "ALLERGY";
					else if (sent_lower.indexOf("family history") >= 0
							|| sent_lower.indexOf("family medical history") >= 0
							|| sent_lower.indexOf("family and social history") >= 0)
						section = "FAMILY HISTORY";
					else if (sent_lower.indexOf("lab") >= 0)
						section = "LABS";
					else
						section = "NOMED";
				}

				writeLog("Section:" + section + "\n");

				ArrayList sents_token_map = new ArrayList();
				for (int j = 0; j < sent_token_array.length; j++) {
					Pair<String, Integer> pair_a1 = Pair.with(
							sent_token_array[j], j);
					Pair<Pair<String, Integer>, Integer> pair_a2 = Pair.with(
							pair_a1, j);
					sents_token_map.add(pair_a2);
					// System.out.println("sent_token_array :"+sent_token_array[j]);
				}

				writeLog("** Input for tagger, String Array :\n");
				for(int j = 0; j < sent_token_array.length; j++)
				{
					writeLog(sent_token_array[j]+"\t"+sent_tag_array[j]+"\n");
				}
				//long tsent1 = System.nanoTime();
				//System.out.println("before rule engine cost:"+(tsent1-tsent0));
				// hire ruled-based engine to modify the tagging result

				token_tags = rule_engine.transform(rule_engine,
						sent_token_array, sent_tag_array, sent_tag_index_array,
						sents_token_map, section, log, if_drool_engine);
				//long tsent2 = System.nanoTime();
				//System.out.println("rule engine cost:"+(tsent2-tsent1));
				/*
				out.write("** Output from tagger : token_tags :"+token_tags+"\n");
				
				
				 for(int i=0;i<token_tags.size();i++) {
					 if(!token_tags.get(i).getValue1().equals("TK")) {
						 out.write("TAGTAG:"+"\t"+sent_index+"_"+token_tags.get(i).getValue0()+"_"+token_tags.get(i).getValue1()+"\n"); }
					 writeLog(token_tags.get(i).toString()+"\n"); }
				*/ 
				// update tag position information
				writeLog("updated_token_info:\n");
				ArrayList<Quartet<String, String, Integer, Integer>> updated_token_info = new ArrayList<Quartet<String, String, Integer, Integer>>();
				int prev_endpos = 0;

				for (int k = 0; k < token_tags.size(); k++) {
					
					Quartet<String, String, Integer, Pair<Pair<String, Integer>, Integer>> tagtuple = token_tags
							.get(k);
					String token_text = tagtuple.getValue0();
					String token_tag = tagtuple.getValue1();
					int start_token;
					if (k == 0) {
						prev_endpos = tagtuple.getValue2();
						start_token = 0;
					} else {
						start_token = prev_endpos + 1;
					}
					Pair<Pair<String, Integer>, Integer> pair = tagtuple
							.getValue3();
					//start_token = tagtuple.getValue2();
					int end_token = pair.getValue1();
					Quartet<String, String, Integer, Integer> new_token = Quartet
							.with(token_text, token_tag,
									sent_token_start_array[start_token],
									sent_token_end_array[end_token]);
					updated_token_info.add(new_token);
					writeLog(new_token + "\n");
					prev_endpos = end_token;
				}

				for (int j = 0; j < token_tags.size(); j++) {
					int index = j + 1;
					if (index < token_tags.size()) {
						String str1 = token_tags.get(j).getValue0();
						String str2 = token_tags.get(j + 1).getValue0();
						int a;
						if (str2.startsWith(str1 + " "))
							token_tags.remove(j);
					}

				}

				ArrayList<Quintet<String, String, Integer, Pair<Pair<String, Integer>, Integer>, Integer>> pos_temp = new ArrayList();
				int counter = 0;
				writeLog("&&&&&&&&&&&&&&&&&&&\ntoken_tags:\n");

				for (int j = 0; j < token_tags.size(); j++) {
					Quintet<String, String, Integer, Pair<Pair<String, Integer>, Integer>, Integer> quintet1 = token_tags
							.get(j).add(counter);

					pos_temp.add(quintet1);

					writeLog(token_tags.get(j) + "\n");
					counter++;
				}
				// System.out.println("pos_temp :"+pos_temp);

				// remove non-clinial tags
				ArrayList tags = new ArrayList();
				for (int j = 0; j < pos_temp.size(); j++) {
					if (!pos_temp.get(j).getValue1().equals("TK")
							&& !pos_temp.get(j).getValue1()
									.equals("INDICATION"))
						tags.add(pos_temp.get(j).getValue0());
				}
				writeLog("input for breakmed :" + pos_temp + "\n");

				// break sentence into sections that contains only one drug
				ArrayList<ArrayList<Quintet<String, String, Integer, Pair<Pair<String, Integer>, Integer>, Integer>>> medicationFindingList = breakSentenceByMed(pos_temp);
				for (int l = 0; l < medicationFindingList.size(); l++)
					writeLog("medicationFindingList :"
							+ medicationFindingList.get(l) + "\n");

				int k = medicationFindingList.size() - 1;
				writeLog("size:" + k + "\n");
				int tokenIndex = 0;

				while (k >= 0) {// for each section
					ArrayList index_list = new ArrayList();
					ArrayList<Quintet<String, String, Integer, Pair<Pair<String, Integer>, Integer>, Integer>> med_token_ttags = medicationFindingList
							.get(k);

					ArrayList sent_semtags = new ArrayList();
					ArrayList sent_semterms = new ArrayList();
					ArrayList sent_token_sem = new ArrayList();
					ArrayList<Pair<Integer, Integer>> sent_tag_offset = new ArrayList<Pair<Integer, Integer>>();
					ArrayList<Pair<Integer, Integer>> sent_tag_offset_final = new ArrayList<Pair<Integer, Integer>>();
					int isDrugScore = 0;
					int tagindex = 0;

					for (int j = 0; j < med_token_ttags.size(); j++) {
						writeLog("med_token_ttags\n");
						writeLog(med_token_ttags.get(j).toString() + "\n");
						if (!med_token_ttags.get(j).getValue1().equals("TK")
								&& !med_token_ttags.get(j).getValue1()
										.equals("INDICATION")
								&& !med_token_ttags.get(j).getValue1()
										.equals("TUNIT")
								&& !med_token_ttags.get(j).getValue1()
										.equals("DOSEUNIT")
								&& !med_token_ttags.get(j).getValue1()
										.equals("UNIT")) {
							sent_token_sem.add(med_token_ttags.get(j));
							tokenIndex = med_token_ttags.get(j).getValue4();
							sent_semtags.add("s"
									+ med_token_ttags.get(j).getValue1());
							Pair<Integer, Integer> pair = Pair.with(
									updated_token_info.get(tokenIndex)
											.getValue2(), updated_token_info
											.get(tokenIndex).getValue3());
							writeLog("token_index:" + tokenIndex + "\n");
							sent_tag_offset.add(pair);
							Pair<Quintet<String, String, Integer, Pair<Pair<String, Integer>, Integer>, Integer>, Integer> p = Pair
									.with(med_token_ttags.get(j), tagindex);
							sent_semterms.add(p);
							index_list.add(tagindex);

							tagindex += 1;
							String temp_tag = med_token_ttags.get(j)
									.getValue1();

							if (temp_tag.equals("DIN")
									|| temp_tag.equals("DBN")
									|| temp_tag.equals("DPN")
									|| temp_tag.equals("DSCD")
									|| temp_tag.equals("DSCDC")
									|| temp_tag.equals("DSCDF"))
								isDrugScore = isDrugScore + 2;
							else
								isDrugScore = isDrugScore + 1;
						}// end of if
							// tokenIndex++;

					}// end of for

					writeLog("sent_tag_offset :" + sent_tag_offset + "\n");
					if (isDrugScore >= 2) {
						String ttags = "";
						for (int j = 0; j < pos_temp.size(); j++) {
							Quintet<String, String, Integer, Pair<Pair<String, Integer>, Integer>, Integer> t = pos_temp
									.get(j);
							if (t.equals(""))
								ttags = t.getValue0() + "/" + t.getValue1();
							else
								ttags = ttags + " " + t.getValue0() + "/"
										+ t.getValue1();

						}

						ArrayList DrugList = new ArrayList();
						String DrugStr = "";

						String sents = "";
						ArrayList<String> semtags = new ArrayList<String>();
						for (int l = 0; l < sent_semtags.size(); l++) {
							// System.out.println("sent_semtags :"+sent_semtags.get(l));
							if (!sent_semtags.get(l).equals("sTUNIT")
									&& !sent_semtags.get(l).equals("sUNIT")) {
								sents = sents + "'" + sent_semtags.get(l)
										+ "' ";
								sent_tag_offset_final.add(sent_tag_offset
										.get(l));
								semtags.add(sent_semtags.get(l).toString()
										.substring(1));
							}
						}
						writeLog("sent_tag_offset_final :"
								+ sent_tag_offset_final + "\n");

						// System.out.println("sentence "+(i+1)+"|"+final_sents.get(i));

						// call CFG parser to parse the semantic tag sequence
						// for each signature
						ArrayList<String> DGList = new ArrayList<String>();

						String startRule = "S -> DGL";
						// System.out.println("sent_semtags :"+sent_semtags);
						writeLog("** Input to EarleyParser constructor:"
								+ sents + "\n");
						// System.out.println("sent_semterms:"+ sent_semterms);
						EarleyParser parser = new EarleyParser(sents,
								new med_parser_grammar(grammar_file), startRule);
						// String parser_check =
						// parser.check_parse(sent_semterms);
						// System.out.println("** Input to EarleyParser parser method() :"+sent_semterms);
						parser.parse();

						TreeNode root = parser.buildParsedTree();
						// System.out.println(sent_semterms);
						// root.preOrder(0);

						root.tree_mapping(0, sent_semterms);

						root.getNodeOut(root);

						if (!root.drugList1_dgssig.isEmpty()) {
							DGList = root.drugList1_dgssig;
							for(int y=0;y<root.drugList1_dgssig.size();y++)
								writeLog("dgssig:"+root.drugList1_dgssig.get(y));
								//writeLog("enter 1\n");
						}
						if (!root.drugList1_dgmsig.isEmpty()
								&& !root.drugList1_andsig.isEmpty()) {
							// System.out.println("** drugList1_dgmsig :"+root.drugList1_dgmsig);
							// System.out.println("** drugList1_andsig :"+root.drugList1_andsig);
							// writeLog("enter 2\n");
							for(int y=0;y<root.drugList1_dgssig.size();y++){
								writeLog("dgmsig:"+root.drugList1_dgmsig.get(y));
								writeLog("andsig:"+root.drugList1_andsig.get(y));
							}
							String str_all = "";
							for (String str : root.drugList1_dgmsig) {
								str_all += str + "\t";
							}

							String str_sig = "";
							for (String str : root.drugList1_andsig) {
								str_sig += str + "\t";
							}

							// System.out.println("str_sig :"+str_sig+"--");
							// System.out.println("str_all :"+str_all+"--");

							for (String sig : root.drugList1_andsig) {
								// System.out.println("sig :"+sig);
								String str_drug = str_all.replaceAll(
										str_sig.trim(), sig.trim());
								// System.out.println("str_drug :"+str_drug);
								DGList.add(str_drug);
							}
						}
						for(int i=0;i<DGList.size(); i++)
							writeLog("** Output from Early parser :" + DGList.get(i)
								+ "\n");

						if (DGList.size() == 0) {// if drug semantic tags cannot
													// be parsed by Early
													// parser, we use regular
													// expression to parse it
													// System.out.println("** regex ** ");

							ArrayList<ArrayList<String>> return_tag_term_list = new ArrayList<ArrayList<String>>();
							writeLog("** Input regex parser :" + sent_semterms
									+ "\n");
							RegexParser r = new RegexParser();
							return_tag_term_list = r.parse(sent_semterms);
							ArrayList<String> DGList_new = new ArrayList<String>();
							writeLog("return_tag_term_list:"
									+ return_tag_term_list);
							int total_num = 0;
							for (int m = 0; m < return_tag_term_list.size(); m++) {
								DGList = return_tag_term_list.get(m);
								for (int l = 0; l < DGList.size(); l++) {
									String[] temp = DGList.get(l).split("\t");
									total_num += temp.length;
								}

								// System.out.println("DGList :"+DGList);
								String DGList_str = "";
								for (int n = 0; n < DGList.size(); n++) {
									// System.out.println("DGList :"+n+": "+DGList.get(n));
									// DGList_str = DGList_str +
									// "\t"+DGList.get(n);

									// System.out.println("DGList_str :"+DGList_str);
									if (!DGList_new.contains(DGList.get(n)
											.toString()))
										DGList_new
												.add(DGList.get(n).toString());

								}

							}
							writeLog("total_num:" + total_num + "\n");
							writeLog("** Output Regex parser :" + DGList_new
									+ "\n");
							int sig_index = 0;

							if (total_num != sent_tag_offset_final.size()) {
								// DGList_new = new ArrayList<String>();
								writeLog("total_num not match!\n");
							}
							// update offset information by mapping
							sent_tag_offset_final = mapParsing(semtags,
									DGList_new, sent_tag_offset_final,
									doc.original_txt());

							for (int p = 0; p < DGList_new.size(); p++) {

								writeLog("** Input for formatDruglist :"
										+ DGList_new.get(p).toString() + "\n");
								String[] siglist = DGList_new.get(p)
										.split("\t");
								writeLog("siglist.length:" + siglist.length
										+ "\n");
								Vector<DrugTag> new_sigs = new Vector<DrugTag>();
								String str_drug_list_pos = "";
								for (int i = 0; i < siglist.length; i++) {
									Pair<Integer, Integer> pos;
									if (sig_index < sent_tag_offset_final
											.size()) {
										pos = sent_tag_offset_final
												.get(sig_index);
									} else {
										writeLog("ERROR: cannot get mapping info\n");
										pos = sent_tag_offset_final.get(0);
									}
									str_drug_list_pos += siglist[i] + "_PPP_"
											+ pos.getValue0() + "_PPP_"
											+ pos.getValue1() + "\t";
									// System.out.println(siglist[i]+ "\t"+
									// pos.getValue0()+"\t"+pos.getValue1() +
									// "\t"+doc.original_txt().substring(pos.getValue0(),
									// pos.getValue1()));
									String[] temp = siglist[i].split("FFF");
									// out.write(temp[1].trim()+ "\t"+
									// pos.getValue0()+"\t"+pos.getValue1()+"\t"+doc.original_txt().substring(pos.getValue0(),
									// pos.getValue1()) + "\n");
									String str1 = temp[1].trim().replace(" ",
											"");
									String str2 = doc
											.original_txt()
											.substring(pos.getValue0(),
													pos.getValue1())
											.replace("\n", "").replace(" ", "");
									writeLog("str1:" + str1 + "\n");
									writeLog("str2:" + str2 + "\n");

									if (!str1.equals(str2)) {
										// out.write("ERROR\n");
										writeLog("ERROR\n");
									}

									sig_index++;
								}

								for (int h = 0; h < sent_token_array.length; h++) {
									writeLog(sent_token_array[h] + "\t"
											+ sent_token_start_array[h] + "\t"
											+ sent_token_end_array[h] + "\n");
								}
								writeLog("str_drug_list_pos:"
										+ str_drug_list_pos + "\n");
								String FStr = formatDruglist(str_drug_list_pos
										.trim());
								// String[] FStr_list = FStr.split("\t");
								// System.out.println("** Output formatDruglist :"+FStr);
								String[] FStr_list = FStr.split("\n");
								writeLog("FStr:" + FStr);

								for (int i = 0; i < FStr_list.length; i++) {
									// System.out.println("FStr_list[i]:"+FStr_list[i]);
									String sigs[] = FStr_list[i]
											.split("__SEP__");

									String[] FStr_list_final = new String[Util.SIGNATURE_STRING.length + 4];

									String sig_encode[] = new String[Util.SIGNATURE_STRING.length];
									// System.out.println(sigs.length);
									for (int j = 0; j < FStr_list_final.length; j++) {
										if (j >= 0
												&& j < Util.SIGNATURE_STRING.length) {
											if (!sigs[j].equals("NA")) {
												String[] temps = sigs[j]
														.split("_");

												sig_encode[j] = temps[0]
														+ " FFF "
														+ Util.SIGNATURE_STRING[j];
											} else {
												sig_encode[j] = "";
											}
											FStr_list_final[j] = sigs[j];
										}

									}
									long start_encode = System.nanoTime();
									 
									String codes[] = coder.encode(sig_encode, log);
									System.out.println("encode string:"+StringUtils.join(sig_encode,"|"));
									long end_encode = System.nanoTime();
									System.out.println("encode cost :"+(end_encode-start_encode));
									System.out.println("-------------------------------");
									umls_code = codes[0];

									rx_code = codes[1];
									
									FStr_list_final[Util.SIGNATURE_STRING.length] = umls_code;
									FStr_list_final[Util.SIGNATURE_STRING.length + 1] = rx_code;
									FStr_list_final[Util.SIGNATURE_STRING.length + 2] = codes[2];
									FStr_list_final[Util.SIGNATURE_STRING.length + 3] = codes[3];
									//System.out.println(StringUtils.join(FStr_list_final,"|"));
									//System.out.println(FStr_list_final[SIGNATURE_STRING.length + 2]);
									String orig_sent = doc.original_txt().substring(s.absStart(), s.absEnd()).replace("\n", " ");
									Vector<DrugTag> signature = print_result(
											sent_index, FStr_list_final,
											orig_sent, doc.original_txt());
									doc.add_signature(signature);
									signature = null;
								}

							}

						} else {
							// System.out.println("in else");
							// System.out.println("return_tag_term_list :"+return_tag_term_list);
							// get codes for drugs

							sent_tag_offset_final = mapParsing(semtags, DGList,
									sent_tag_offset_final, doc.original_txt());
							int sig_index = 0;
							writeLog("DGList_2 :" + DGList + "\n");
							for (int m = 0; m < DGList.size(); m++) {
								// String FStr_list[] =
								// formatDruglist(DGList.get(m).toString()).split("\n");

								String[] siglist = DGList.get(m).split("\t");

								String str_drug_list_pos = "";
								for (int i = 0; i < siglist.length; i++) {
									Pair<Integer, Integer> pos;
									if (sig_index < sent_tag_offset_final
											.size()) {
										pos = sent_tag_offset_final
												.get(sig_index);
									} else {
										writeLog("ERROR: cannot get mapping info\n");
										pos = sent_tag_offset_final.get(0);
									}
									writeLog("siglist:" + siglist[i] + "\n");
									str_drug_list_pos += siglist[i] + "_PPP_"
											+ pos.getValue0() + "_PPP_"
											+ pos.getValue1() + "\t";
									// System.out.println(siglist[i]+ "\t"+
									// pos.getValue0()+"\t"+pos.getValue1()+"\t"+doc.original_txt().substring(pos.getValue0(),
									// pos.getValue1()));
									String[] temp = siglist[i].split("FFF");
									// out.write(temp[1].trim()+ "\t"+
									// pos.getValue0()+"\t"+pos.getValue1()+"\t"+doc.original_txt().substring(pos.getValue0(),
									// pos.getValue1()) + "\n");
									String str1 = temp[1].trim().replace(" ",
											"");
									String str2 = doc
											.original_txt()
											.substring(pos.getValue0(),
													pos.getValue1())
											.replace("\n", "").replace(" ", "");
									writeLog("str1:" + str1 + "\n");
									writeLog("str2:" + str2 + "\n");
									if (!str1.equals(str2)) {
										writeLog("ERROR\n");
										// out.write("ERROR\n");
									}

									sig_index++;
								}

								writeLog("** Input formatDruglist :"
										+ DGList.get(m).toString() + "\n");
								String FStr = formatDruglist(str_drug_list_pos);
								String[] FStr_list = FStr.split("\n");
								// System.out.println("FStr_list:"+FStr_list[1]);
								writeLog("** Output formatDruglist :" + FStr
										+ "\n");

								for (int i = 0; i < FStr_list.length; i++) {
									// System.out.println("FStr_list[i]:"+FStr_list[i]);
									String sigs[] = FStr_list[i]
											.split("__SEP__");

									String[] FStr_list_final = new String[Util.SIGNATURE_STRING.length + 4];

									String sig_encode[] = new String[Util.SIGNATURE_STRING.length];
									// System.out.println(sigs.length);
									for (int j = 0; j < FStr_list_final.length; j++) {
										if (j >= 0
												&& j < Util.SIGNATURE_STRING.length) {
											if (!sigs[j].equals("NA")) {
												String[] temps = sigs[j]
														.split("_");

												sig_encode[j] = temps[0]
														+ " FFF "
														+ Util.SIGNATURE_STRING[j];
											} else {
												sig_encode[j] = "";
											}
											FStr_list_final[j] = sigs[j];
										}

									}
									for (int l = 0; l < FStr_list_final.length; l++)
										writeLog("FStr_list_final:"
												+ FStr_list_final[l]);
									long start_encode = System.nanoTime();
									String codes[] = coder.encode(sig_encode, log);
									umls_code = codes[0];
									rx_code = codes[1];
									//System.out.println("encode return:"+StringUtils.join(codes,"|"));
									long end_encode = System.nanoTime();
									System.out.println("encode string:"+StringUtils.join(sig_encode,"|"));
									System.out.println("encode cost :"+(end_encode-start_encode));
									System.out.println("--------------------------");
									FStr_list_final[Util.SIGNATURE_STRING.length] = umls_code;
									FStr_list_final[Util.SIGNATURE_STRING.length + 1] = rx_code;
									FStr_list_final[Util.SIGNATURE_STRING.length + 2] = codes[2];
									FStr_list_final[Util.SIGNATURE_STRING.length + 3] = codes[3];
									//System.out.println(StringUtils.join(FStr_list_final,"|"));
									//System.out.println(FStr_list_final[SIGNATURE_STRING.length + 2]);
									String orig_sent = doc.original_txt().substring(s.absStart(), s.absEnd()).replace("\n", " ");
									Vector<DrugTag> signature = print_result(
											sent_index, FStr_list_final,
											orig_sent, doc.original_txt());
									doc.add_signature(signature);
									signature = null;
								}

							}
						}
						// System.out.println(DGList.toString());
						DrugList = null;
						DGList = null;

					} // end of if

					k = k - 1;
					sent_semtags = null;
					sent_semterms = null;
					sent_token_sem = null;
					sent_tag_offset = null;
					sent_tag_offset_final = null;
					med_token_ttags = null;

				}// end of while

				// end_line = end_line + final_sents.get(i).length() +1;
				// System.out.println("start_line :"+start_line+"  end line :"+end_line);

				//long tsent3 = System.nanoTime();
				//System.out.println("after rule engine cost:"+(tsent3-tsent2));
			} // end of for final sents

			// System.out.println("Medex :"+(end - start)/1000);
			//long t3 = System.nanoTime();
			//System.out.println("tagger cost:"+(t3-t2));
			result = null;
			tag_dict = null;

			filter_tags = null;
			final_sents = null;
			token_list = null;
			token_tags_temp = null;
			token_tags = null;
			sent_token_array = null;
			sent_tag_array = null;
			sent_token_start_array = null;
			sent_token_end_array = null;
			sent_tag_index_array = null;

			this.out.close();
			this.out2.close();
			this.log.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Mapping the signature list with and without position information
	 * 
	 * @param DGList
	 *            signature list without position information
	 * @param sent_tag_offset_final
	 *            signature list with position information
	 * @param doc_text
	 *            original text of input sentence
	 * @return mapped signature list with position information
	 * @see MedTagger
	 */
	private ArrayList<Pair<Integer, Integer>> mapParsing(ArrayList semtags,
			ArrayList<String> DGList,
			ArrayList<Pair<Integer, Integer>> sent_tag_offset_final,
			String doc_text) {
		ArrayList<Pair<Integer, Integer>> sent_tag_offset_return = new ArrayList<Pair<Integer, Integer>>();
		try {
			writeLog("Start mapping...\n");
			writeLog("semtags:" + semtags.toString() + "\n");
			writeLog("DGList:" + DGList.toString() + "\n");
			writeLog("sent_tag_offset_final" + sent_tag_offset_final + "\n");
			int len = 0;
			ArrayList<String> DGList_new = new ArrayList();
			for (int h = 0; h < DGList.size(); h++) {
				String[] items = DGList.get(h).split("\t");
				len += items.length;
				for (int m = 0; m < items.length; m++) {
					DGList_new.add(items[m]);
				}

			}
			if (semtags.size() >= 2) {

				ArrayList<String> tokens = new ArrayList();
				ArrayList<String> tags = new ArrayList();
				for (int i = 0; i < DGList_new.size(); i++) {
					String[] temp = DGList_new.get(i).split(" FFF ");
					tags.add(temp[0]);
					tokens.add(temp[1]);
				}
				// check if mapping correct
				boolean isCorrect = true;
				String[] strlist1 = new String[sent_tag_offset_final.size()];
				String[] strlist2 = new String[DGList_new.size()];

				for (int i = 0; i < sent_tag_offset_final.size(); i++) {
					int start = sent_tag_offset_final.get(i).getValue0();
					int end = sent_tag_offset_final.get(i).getValue1();
					String str1 = doc_text.substring(start, end)
							.replace("\n", "").replace(" ", "");
					strlist1[i] = str1;
					writeLog("str1:" + str1 + "\n");
				}
				for (int i = 0; i < tokens.size(); i++) {
					String str2 = tokens.get(i).replace(" ", "");
					strlist2[i] = str2;
					writeLog("str2:" + str2 + "\n");
				}
				if (strlist1.length != strlist2.length) {
					isCorrect = false;
					writeLog("num doesn't match\n");
				} else {
					for (int i = 0; i < strlist1.length; i++) {
						if (!strlist1[i].equals(strlist2[i])) {
							isCorrect = false;
							break;
						}
					}
				}

				// add occurrence information to phrase
				HashMap<String, Integer> found_string = new HashMap<String, Integer>();
				String[] strlist1_occ = new String[sent_tag_offset_final.size()];
				String[] strlist2_occ = new String[DGList_new.size()];

				for (int i = 0; i < strlist2.length; i++) {
					if (found_string.containsKey(strlist2[i])) {
						found_string.put(strlist2[i],
								found_string.get(strlist2[i]) + 1);
					} else {
						found_string.put(strlist2[i], 1);
					}
					strlist2_occ[i] = strlist2[i] + "_"
							+ String.valueOf(found_string.get(strlist2[i]));
				}
				found_string.clear();
				for (int i = 0; i < strlist1.length; i++) {
					if (found_string.containsKey(strlist1[i])) {
						found_string.put(strlist1[i],
								found_string.get(strlist1[i]) + 1);
					} else {
						found_string.put(strlist1[i], 1);
					}
					strlist1_occ[i] = strlist1[i] + "_"
							+ String.valueOf(found_string.get(strlist1[i]));
				}

				if (!isCorrect) {
					// if not correct, we will
					for (int i = 0; i < strlist2_occ.length; i++) {
						boolean is_found = false;
						for (int j = 0; j < strlist1_occ.length; j++) {
							if (strlist1_occ[j].equals(strlist2_occ[i])) {
								writeLog("i:" + strlist2_occ[i] + "\n");
								writeLog("j:" + strlist1_occ[j] + "\n");
								writeLog(sent_tag_offset_final + "\n");
								sent_tag_offset_return
										.add(sent_tag_offset_final.get(j));
								// strlist2[j] = "";
								is_found = true;
								break;
							}
						}
						if (!is_found) {
							sent_tag_offset_return.add(sent_tag_offset_final
									.get(0));
						}
					}
				} else {
					sent_tag_offset_return = sent_tag_offset_final;
				}

			} else {
				sent_tag_offset_return = sent_tag_offset_final;
			}

			writeLog("sent_tag_offset_final_return" + sent_tag_offset_return
					+ "\n");
		} catch (Exception e) {

		}
		return sent_tag_offset_return;
	}

	/**
	 * Searches token-tag list from right, to find the index of a specified
	 * token.
	 * 
	 * @param DList
	 *            list of strings contains drug signature e.g.,
	 *            ["warfarin FFF DIN", "5mg FFF DOSE"]
	 * @return string with drug and signature information separated by "\t"
	 *         e.g., "warfarin\t\t\t5mg\t\t\t\t\t\t"
	 * @see MedTagger
	 */
	private String formatDruglist(String DList) {
		String[] items = DList.split("\t");
		// System.out.println("DList formatDruglist:"+DList);
		String drug, bdrug, ddf, dose, doseamt, rut, freq, du, nec;
		drug = bdrug = ddf = dose = doseamt = rut = freq = du = nec = "NA";

		for (int k = 0; k < items.length; k++) {
			// System.out.println(" ** "+DGList[0]+" "+DGList[1]);
			String DGList_element = items[k];
			// System.out.println(" ** "codi+DGList_element);

			String[] Str = DGList_element.split(" FFF ");
			// System.out.println(" ** Str[0] **"+Str[0]+ " "+Str[1]);
			// String[] temp = Str[1].split("_");

			if ((Str[0].equals("DBN") || Str[0].equals("DIN") || Str[0].equals("DPN")) && drug.equals("NA"))
				drug = Str[1];
			if (Str[0].equals("MDBN") && bdrug.equals("NA"))
				bdrug = Str[1];
			if (Str[0].equals("DDF") && ddf.equals("NA"))
				ddf = Str[1];
			if (Str[0].equals("DOSE") && dose.equals("NA"))
				dose = Str[1];
			if (Str[0].equals("DOSEAMT") && doseamt.equals("NA"))
				doseamt = Str[1];
			if (Str[0].equals("RUT") && rut.equals("NA"))
				rut = Str[1];
			if (Str[0].equals("FREQ") && freq.equals("NA"))
				freq = Str[1];
			if (Str[0].equals("DRT") && du.equals("NA"))
				du = Str[1];
			if (Str[0].equals("NEC") && nec.equals("NA"))
				nec = Str[1];
		}

		String[] Str1_t = { drug, bdrug, ddf, dose, doseamt, rut, freq, du, nec };
		ArrayList Str1 = new ArrayList(Arrays.asList(Str1_t));

		String[] Str2_t = { drug, "NA", ddf, dose, doseamt, rut, freq, du, nec };
		ArrayList Str2 = new ArrayList(Arrays.asList(Str2_t));

		String[] Str3_t = { bdrug, "NA", "NA", "NA", "NA", "NA", "NA", "NA","NA" };
		ArrayList Str3 = new ArrayList(Arrays.asList(Str3_t));

		ArrayList<String> generic_list = new ArrayList<String>();

		String[] temps = bdrug.toLowerCase().split("_");
		String bdrug_name = temps[0];
		String[] temps2 = drug.split("_");
		String drug_name = temps2[0];
		// System.out.println("bdrug:"+bdrug_name);
		// System.out.println("drug:"+drug_name);
		if (!bdrug_name.equals("")) {
			// System.out.println("bdrug :"+bdrug);
			// System.out.println("rx_norm_map :"+rx_norm_map.size());
			if (rx_norm_map.containsKey(bdrug_name)) {
				// generic_list.add((ArrayList<String>) rx_norm_map.get(bdrug));

				generic_list = rx_norm_map.get(bdrug_name);
				// System.out.println("generic_list :"+generic_list);
			}
		}

		// System.out.println(generic_list);
		if (bdrug_name.equals("na")
				|| generic_list.contains(drug_name.toLowerCase())) {

			// System.out.println("Str1 :"+Str1);
			String Str1_return = StringUtils.join(Str1, "__SEP__");
			// System.out.println("Str1_return :"+Str1_return);
			return Str1_return;
		} else {

			String Str2_return = StringUtils.join(Str2, "__SEP__");
			String Str3_return = StringUtils.join(Str3, "__SEP__");
			// System.out.println("Str2_3_return :"+"\t" + Str2_return + "\n" +
			// "\t" + Str3_return);
			return Str2_return + "\n" + Str3_return;

		}

	}

	/**
	 * loads RX coding dictionary for identifying brand name of drug
	 * 
	 * @param rx_norm_file
	 *            name of dictionary file generated from RXNORM
	 * 
	 * @see MedTagger
	 */
	private void loadRXNorm(String rx_norm_file) {

		// System.out.println("** In loadRXNorm");
		try {

			FileInputStream fstream = new FileInputStream(rx_norm_file);

			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			// System.out.println(br.read());
			while ((strLine = br.readLine()) != null) {

				// System.out.println("** "+strLine);
				String[] strLine_split = strLine.split("\t");
				if (strLine_split.length == 2) {
					// System.out.println(strLine_split[0]+
					// " "+strLine_split[1]);
					String brand = strLine_split[1].toLowerCase();
					String generic = strLine_split[0].toLowerCase();

					if (!rx_norm_map.containsKey(brand)) {
						ArrayList<String> newlist = new ArrayList<String>();
						newlist.add(generic);
						rx_norm_map.put(brand, newlist);
					} else {
						ArrayList<String> generic_list = rx_norm_map.get(brand);
						generic_list.add(generic);
						rx_norm_map.put(brand, generic_list);
					}
				}

			}

			// Close the input stream
			in.close();
		} catch (Exception e) {// Catch exception if any
			e.printStackTrace();
		}

	}

	/**
	 * Normalized the input string by removing some special characters
	 * 
	 * @param phrase
	 *            input phrase
	 * 
	 * @see MedTagger
	 */
	private String getNormedPhrase(String phrase) {
		String[] removedSymbol = { "-", "+", "/", "\\", "@", "&" };
		for (int i = 0; i < removedSymbol.length; i++) {
			phrase = phrase.replace(removedSymbol[i], " ");
		}
		String[] items = phrase.split(" ");

		String normed_phrase = "";
		for (int j = 0; j < items.length; j++) {
			if (!items[j].trim().equals("")) {
				normed_phrase += items[j].trim();
			}
		}
		normed_phrase = normed_phrase.trim();
		return normed_phrase;
	}
	
	
	private void loadNormMapping(String norm_file){
		
		try {
			int i = 0;
			FileInputStream fstream = new FileInputStream(norm_file);

			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			// System.out.println(br.read());
			while ((strLine = br.readLine()) != null) {
				//System.out.println(strLine);
				String items[] = strLine.split("\t");
				this.normalization_map.put(items[0], items[1]);
				i+=1;
			}
			//System.out.println(i);
		}
		catch(Exception e){
			System.out.println(e.toString());
		}
	}
	
	
	/**
	 * loads RX coding dictionary for identifying drug codes.
	 * 
	 * @param rx_code_file	name of dictionary file
	 * 
	 * @see MedTagger
	 */
	private void loadRXCoding(String rx_code_file) throws Exception {

		//System.out.println("** In loadRXCode");
		try {

			FileInputStream fstream = new FileInputStream(rx_code_file);

			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			int count = 0;
			
			int x = 0;
			
			// System.out.println(br.read());
			while ((strLine = br.readLine()) != null) {
				// System.out.println("** "+strLine);
				/*
				if(x%10000 == 0){
					System.out.println(x);
					
				}
				x++;
				*/
				String[] rx_codes = new String[7];
				String[] strLine_split = strLine.split("\t");

				for (int j = 0; j < 7; j++) {
					if (j <= strLine_split.length - 1) {
						rx_codes[j] = strLine_split[j].trim();
					} else {
						rx_codes[j] = "";
					}
					//System.out.println(rx_codes[j]);
				}
				//System.out.println("--------------");
				
				if(!rx_code_name_map.containsKey(rx_codes[2]) && !rx_codes[0].equals("null") && !rx_codes[0].equals("") && !rx_codes[2].equals("null")){
					
					rx_code_name_map.put(rx_codes[2], rx_codes[0]);
				}
				if(rx_code_name_map.containsKey(rx_codes[2]) && !rx_codes[0].equals("null") && !rx_codes[0].equals("") && !rx_codes[2].equals("null")){
					if(rx_codes[0].length() < rx_code_name_map.get(rx_codes[2]).length()){
						rx_code_name_map.put(rx_codes[2], rx_codes[0]);
					}
				}
				
				
				
				//for(int i=0; i<strLine_split.length;i++)
				//	System.out.println(strLine_split[i]);
				if (strLine_split.length >= 4) {
					String generic_name = rx_codes[3].trim();
					String brand_name = rx_codes[6].trim();
					
					if (!generic_name.equals(""))
						generic_name= utility.normalizeDrugName(generic_name);
					if (!brand_name.equals(""))
						brand_name = utility.normalizeDrugName(brand_name);
					String strength = rx_codes[4].trim();
					//System.out.println("strength:"+strength+"\n");
					String form = rx_codes[5].trim();
					String sent = rx_codes[0].trim();
					if(generic_name.endsWith(".") || brand_name.endsWith(".")){
						count++;
					}
					
					if(generic_name.trim().equals("")){
						generic_name = "NULL";
						
					}
					if(brand_name.trim().equals("")){
						brand_name = "NULL";
					}
					if(strength.trim().equals("")){
						strength = "NULL";
					}
					if(form.trim().equals("")){
						form = "NULL";
					}
					
					// String normed_generic_name =
					// getNormedPhrase(generic_name);
					// String normed_generic_name2 =
					// getNormedPhrase(rx_codes[6]);
					
					if (!generic_name.equals("NULL")) {
						/*
						if(generic_name.equals("1 ML Alprostadil")){
							System.out.println("fond it");
						}
						
						//deal with the case "3 ml heparin sodium, porcine"
						String[] items = generic_name.split(" ");
						if(items.length > 2){
							Pattern pattern = Pattern.compile("^(\\d+ .*|-\\d+ .*)");
							Matcher matcher = pattern.matcher(generic_name);
							if(matcher.matches()){
								for(int i=2; i<items.length;i++){
									generic_name += items[i]+" ";
								}
								generic_name = generic_name.trim();
							}
							
						}*/
						//generic_name = utility.normalizeDrugName(generic_name);
						//brand_name = utility.normalizeDrugName(brand_name);
						String value = generic_name + "\t" +strength + "\t" + form + "\t" + brand_name + "\t"+sent.toLowerCase()+"_"+rx_codes[1] +  "_" + rx_codes[2];
						/*
						if(rx_codes[2].equals("197446"))
							System.out.println("197446:"+generic_name);
						*/
						if (!rx_code_map.containsKey(generic_name)) {
							ArrayList<String> candidate_string_codes = new ArrayList<String>();
							candidate_string_codes.add(value);
							rx_code_map.put(generic_name, candidate_string_codes);
						}
						else{
							ArrayList<String> new_list = rx_code_map.get(generic_name);
							new_list.add(value);
							rx_code_map.put(generic_name, new_list);
						}
						
						/*
						if(rx_codes[2].equals("197446")){
							ArrayList<String> candidate_string2 = rx_code_map.get("carisoprodol");
							for(int i=0; i<candidate_string2.size(); i++){
								System.out.println("197446:      "+candidate_string2.get(i));
							}
						}*/
						
						
						String[] drug_words = generic_name.split(" ");
						if(drug_words.length > 1){
							if (!rx_code_map.containsKey(drug_words[0])) {
								ArrayList<String> candidate_string_codes = new ArrayList<String>();
								candidate_string_codes.add(value);
								rx_code_map.put(drug_words[0], candidate_string_codes);
							}
							else{
								ArrayList<String> new_list = rx_code_map.get(drug_words[0]);
								new_list.add(value);
								rx_code_map.put(drug_words[0], new_list);
							}
						}
						
						
						/*
						if(strLine.equals("acyclovir 0.03 mg/mg [zovirax]	C1598536	566108	acyclovir	0.03 mg/mg 		zovirax")){
							System.out.println(value);
						}
						if(generic_name.indexOf("dibasic potassium phosphate".toLowerCase()) >=0){// && generic_name.indexOf("35.7".toLowerCase()) >=0){
							System.out.println(value);
							
						}*/
						
					}
					
					if (!brand_name.equals("NULL")) {
						//brand_name = utility.normalizeDrugName(brand_name);
						String value = generic_name + "\t" + strength + "\t" + form + "\t" + brand_name +"\t"+sent.toLowerCase()+ "_"+rx_codes[1] + "_" + rx_codes[2];
						if(generic_name.equals("NULL"))
							value = brand_name + "\t" + strength + "\t" + form + "\t" + "NULL" +"\t"+sent.toLowerCase()+ "_"+rx_codes[1] + "_" + rx_codes[2];
						if (!rx_code_map.containsKey(brand_name)) {
							ArrayList<String> candidate_string_codes = new ArrayList<String>();
							candidate_string_codes.add(value);
							rx_code_map.put(brand_name, candidate_string_codes);
						}
						else{
							ArrayList<String> new_list = rx_code_map.get(brand_name);
							new_list.add(value);
							rx_code_map.put(brand_name, new_list);
						}
						/*
						if(strLine.equals("morphine sulfate 6.67 mg/ml oral suspension [mst continus]		103927	morphine sulfate	6.67 mg/ml	oral suspension	mst continus")){
							System.out.println(value);
							System.out.println(brand_name);
						}
						
						if(brand_name.indexOf("cardon".toLowerCase()) >=0 && brand_name.indexOf("beta".toLowerCase()) >=0){
							System.out.println("value:"+value);
							
						}*/
					}

						
					

				}
				
			}
			//System.out.println("count:"+count);
			//ArrayList<String> candidate_string2 = rx_code_map.get("carisoprodol");
			//for(int i=0; i<candidate_string2.size(); i++){
			//	System.out.println("197446"+candidate_string2.get(i));
			//}
			
			// Close the input stream
			in.close();
			//System.out.println("** finish loadRXCode");
		} catch (Exception e) {// Catch exception if any
			throw new Exception(e);
		}

		//System.out.println(rx_code_name_map.get("596"));
	}

	/**
	 * loads RX coding dictionary for identifying brand name of drug
	 * 
	 * @param rx_norm_file
	 *            name of dictionary file generated from RXNORM
	 * 
	 * @see MedTagger
	 */
	private void loadGeneric(String rx_generic_file) {

		// System.out.println("** In loadRXNorm");
		try {

			FileInputStream fstream = new FileInputStream(rx_generic_file);

			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			// System.out.println(br.read());
			while ((strLine = br.readLine()) != null) {

				// System.out.println("** "+strLine);
				String[] strLine_split = strLine.split("\t");
				if (strLine_split.length == 3) {
					// System.out.println(strLine_split[0]+
					// " "+strLine_split[1]);
					String rxcui = strLine_split[0].toLowerCase();
					String generic = strLine_split[1].toLowerCase();
					String generic_cui = strLine_split[2].toLowerCase();
					rx_generic_map.put(rxcui, generic.trim()+"_"+generic_cui);
					//rx_code_name_map.put(generic_cui, generic.trim());
					
				}

			}

			// Close the input stream
			in.close();
		} catch (Exception e) {// Catch exception if any
			e.printStackTrace();
		}

		// System.out.println(rx_generic_map.size());
	}

	/**
	 * Break the list of token-tags into a list of medication findings.
	 * 
	 * @param list
	 *            e.g., [[warfarin, DIN, 0, [[warfarin, 0], 0], 0], [10mg, DOSE,
	 *            1, [[mg, 2], 2], 1], [tid, FREQ, 3, [[tid, 3], 3], 2], [,, TK,
	 *            4, [[,, 4], 4], 3], [simvastatin, DIN, 5, [[simvastatin, 5],
	 *            5], 4], [5mg, DOSE, 6, [[mg, 7], 7], 5], [bid, FREQ, 8, [[bid,
	 *            8], 8], 6], [., TK, 9, [[., 9], 9], 7]]
	 * @return medication_finding_list e.g., [[[,, TK, 4, [[,, 4], 4], 3],
	 *         [simvastatin, DIN, 5, [[simvastatin, 5], 5], 4], [5mg, DOSE, 6,
	 *         [[mg, 7], 7], 5], [bid, FREQ, 8, [[bid, 8], 8], 6], [., TK, 9,
	 *         [[., 9], 9], 7]], [[warfarin, DIN, 0, [[warfarin, 0], 0], 0],
	 *         [10mg, DOSE, 1, [[mg, 2], 2], 1], [tid, FREQ, 3, [[tid, 3], 3],
	 *         2]]]
	 * 
	 * @see MedTagger
	 */
	private static ArrayList<ArrayList<Quintet<String, String, Integer, Pair<Pair<String, Integer>, Integer>, Integer>>> breakSentenceByMed(
			ArrayList<Quintet<String, String, Integer, Pair<Pair<String, Integer>, Integer>, Integer>> list) {
		ArrayList taglist = new ArrayList();
		taglist.add("DIN");
		taglist.add("DBN");
		taglist.add("DPN");

		ArrayList tokenlist = new ArrayList();
		tokenlist.add(",");
		tokenlist.add("and");
		tokenlist.add("or");
		tokenlist.add(";");
	

		ArrayList<ArrayList<Quintet<String, String, Integer, Pair<Pair<String, Integer>, Integer>, Integer>>> medList = new ArrayList();
		int start = list.size() - 1;

		while (rightFindTag(list, taglist, start) >= 0) {
			int currentMedIndex = rightFindTag(list, taglist, start);
			int connectIndex = rightFindToken(list, tokenlist,
					(currentMedIndex - 1));
			int nextMedIndex = rightFindTag(list, taglist, connectIndex);
			if (connectIndex >= 0 && nextMedIndex >= 0) {
				ArrayList<Quintet<String, String, Integer, Pair<Pair<String, Integer>, Integer>, Integer>> t = new ArrayList();
				for (int i = connectIndex; i < start + 1; i++)
					t.add(list.get(i));

				medList.add(t);
				start = connectIndex - 1;
			} else {
				ArrayList<Quintet<String, String, Integer, Pair<Pair<String, Integer>, Integer>, Integer>> t = new ArrayList();
				for (int i = 0; i < start + 1; i++)
					t.add(list.get(i));

				medList.add(t);
				start = -1;
			}
		}
		return medList;

	}

	/**
	 * Searches the token-tag list from the right, to find the index of a
	 * specified tag.
	 * 
	 * @param list
	 *            list of token-tag
	 * @param target
	 *            tags to be searched
	 * @param startpos
	 *            start offset
	 */
	private static int rightFindTag(
			ArrayList<Quintet<String, String, Integer, Pair<Pair<String, Integer>, Integer>, Integer>> list,
			ArrayList target, int startpos) {
		int index = startpos;
		int targetPos = -1;
		while (index >= 0) {
			if (target.contains(list.get(index).getValue1())) {
				targetPos = index;
				break;
			} else
				index = index - 1;
		}

		return targetPos;
	}

	/**
	 * Searches the token-tag list from the right, to find the index of a
	 * specified token.
	 * 
	 * @param list
	 *            list of drug signature
	 * @param target
	 *            tokens to be searched
	 * @return array of string contains UMLS code and RXNORM code
	 * @see MedTagger
	 */
	private static int rightFindToken(
			ArrayList<Quintet<String, String, Integer, Pair<Pair<String, Integer>, Integer>, Integer>> list,
			ArrayList target, int startpos) {
		int index = startpos;
		int targetPos = -1;
		while (index >= 0) {
			if (target.contains(list.get(index).getValue0())) {
				targetPos = index;
				break;
			} else
				index = index - 1;
		}

		return targetPos;
	}


	/**
	 * Output the result for drug tagging
	 * 
	 * @param index	 index of current sentence
	 * @param FStr_list	 list of identified drug signature information
	 * @return vector of DrugTag object to represent all the signatures
	 * 
	 * @see MedTagger
	 */
	private Vector<DrugTag> print_result(int index, String[] FStr_list, String sent_text, String original_text) throws IOException {
		drug = "";
		brand = "";
		dose_form = "";
		strength = "";
		dose_amt = "";
		route = "";
		frequency = "";
		duration = "";
		necessity = "";

		umls_code = "";
		rx_code = "";
		generic_code="";
		
		generic_name = "";
		Vector<DrugTag> sig = new Vector<DrugTag>();

		//System.out.println(StringUtils.join(FStr_list, "|"));
		//System.out.println(FStr_list[11]);
		if (FStr_list.length > 0) {
			// System.out.println("Drug|"+FStr_list[0]);
			String[] items = FStr_list[0].split("_PPP_");
			//System.out.println(StringUtils.join(items, "|"));
			if (items.length == 3) {
				
				drug = items[0]+"["+items[1]+","+items[2]+"]";
				if(if_offset_showed.equals("n")){
					drug = original_text.substring(Integer.parseInt(items[1]), Integer.parseInt(items[2]));
				}
				//writeLog("\nsent_txt:"+sent_text+"\n");
				writeLog("\noriginal drug:"+original_text.substring(Integer.parseInt(items[1]), Integer.parseInt(items[2]))+"\n"); 
				DrugTag tag = new DrugTag(Integer.parseInt(items[1]),
						Integer.parseInt(items[2]), TextSectionType.DRUG,
						items[0]);
				sig.add(tag);
			}
		}

		if (FStr_list.length > 1) {
			// System.out.println("Generic name|"+FStr_list[1]);
			String[] items = FStr_list[1].split("_PPP_");
			// System.out.println(items);
			if (items.length == 3) {
				brand = items[0]+"["+items[1]+","+items[2]+"]";
				if(if_offset_showed.equals("n")){
					brand = items[0];
				}
				// System.out.println(brand);
				DrugTag tag = new DrugTag(Integer.parseInt(items[1]),
						Integer.parseInt(items[2]), TextSectionType.BRAND,
						items[0]);
				sig.add(tag);
			}
		}

		if (FStr_list.length > 2) {
			// System.out.println("Dose Form|"+FStr_list[2]);

			String[] items = FStr_list[2].split("_PPP_");
			if (items.length == 3) {
				dose_form = items[0]+"["+items[1]+","+items[2]+"]";
				if(if_offset_showed.equals("n")){
					dose_form = items[0];
				}
				DrugTag tag = new DrugTag(Integer.parseInt(items[1]),
						Integer.parseInt(items[2]), TextSectionType.FORM,
						items[0]);
				sig.add(tag);
			}
		}

		if (FStr_list.length > 3) {
			// System.out.println("Strength|"+FStr_list[3]);

			String[] items = FStr_list[3].split("_PPP_");
			if (items.length == 3) {
				strength = items[0]+"["+items[1]+","+items[2]+"]";
				if(if_offset_showed.equals("n")){
					strength = items[0];
				}
				DrugTag tag = new DrugTag(Integer.parseInt(items[1]),
						Integer.parseInt(items[2]), TextSectionType.STRENGTH,
						items[0]);
				sig.add(tag);
			}
		}
		if (FStr_list.length > 4) {
			// System.out.println("Dose amt|"+FStr_list[4]);

			String[] items = FStr_list[4].split("_PPP_");
			if (items.length == 3) {
				dose_amt = items[0]+"["+items[1]+","+items[2]+"]";
				if(if_offset_showed.equals("n")){
					dose_amt = items[0];
				}
				DrugTag tag = new DrugTag(Integer.parseInt(items[1]),
						Integer.parseInt(items[2]), TextSectionType.DOSE,
						items[0]);
				sig.add(tag);
			}
		}
		if (FStr_list.length > 5) {
			// System.out.println("Route|"+FStr_list[5]);

			String[] items = FStr_list[5].split("_PPP_");
			if (items.length == 3) {
				route = items[0]+"["+items[1]+","+items[2]+"]";
				if(if_offset_showed.equals("n")){
					route = items[0];
				}
				DrugTag tag = new DrugTag(Integer.parseInt(items[1]),
						Integer.parseInt(items[2]), TextSectionType.ROUTE,
						items[0]);
				sig.add(tag);
			}
		}
		if (FStr_list.length > 6) {
			// System.out.println("Frequency|"+FStr_list[6]);

			String[] items = FStr_list[6].split("_PPP_");
			
			
			
			if (items.length == 3) {
				if(if_freq_norm.equals("n")){
					frequency =items[0]+"["+items[1]+","+items[2]+"]";
					if(if_offset_showed.equals("n")){
						frequency = items[0];
					}
				}
				else{
					ArrayList<MatchResult> result=null;
					result =  this.freq_norm_engine.extract(items[0].trim());
					
					String norm_freq="";
					//System.out.println("result size: "+result.size());
					for(MatchResult al:result){
						//System.out.println("al: "+al.match_str+" "+al.norm_str+" "+al.section);
						norm_freq=norm_freq+al.norm_str+"/";
					}
					if (norm_freq.length() > 0 ){
						norm_freq = norm_freq.substring(0,norm_freq.length()-1);
					}
					frequency = items[0] + "(" + norm_freq + ")"+ "["+items[1]+","+items[2]+"]";
					if(if_offset_showed.equals("n")){
						frequency = items[0] + "(" + norm_freq + ")";
					}
				}
				DrugTag tag = new DrugTag(Integer.parseInt(items[1]),
						Integer.parseInt(items[2]), TextSectionType.FREQUENCY,
						items[0]);
				sig.add(tag);
			}
		}
		if (FStr_list.length > 7) {
			// System.out.println("Unit|"+FStr_list[7]);

			String[] items = FStr_list[7].split("_PPP_");
			if (items.length == 3) {
				duration =items[0]+"["+items[1]+","+items[2]+"]";
				if(if_offset_showed.equals("n")){
					duration = items[0];
				}
				DrugTag tag = new DrugTag(Integer.parseInt(items[1]),
						Integer.parseInt(items[2]), TextSectionType.DURATION,
						items[0]);
				sig.add(tag);
			}
		}
		if (FStr_list.length > 8) {
			// System.out.println("Necessity|"+FStr_list[8]);

			String[] items = FStr_list[8].split("_PPP_");
			if (items.length == 3) {
				necessity = items[0]+"["+items[1]+","+items[2]+"]";
				if(if_offset_showed.equals("n")){
					necessity = items[0];
				}
				DrugTag tag = new DrugTag(Integer.parseInt(items[1]),
						Integer.parseInt(items[2]), TextSectionType.NECCESSITY,
						items[0]);
				sig.add(tag);
			}
		}

		if (FStr_list.length > 9) {
			// System.out.println("Necessity|"+FStr_list[8]);
			umls_code = FStr_list[9];
		}
		if (FStr_list.length > 10) {
			// System.out.println("Necessity|"+FStr_list[8]);
			rx_code = FStr_list[10];
		}
		if (FStr_list.length > 11) {
			// System.out.println("Necessity|"+FStr_list[8]);
			generic_code = FStr_list[11];
		}
		if (FStr_list.length > 12) {
			// System.out.println("Necessity|"+FStr_list[8]);
			generic_name = FStr_list[12];
		}
		
		
		
		// System.out.println(drug+"|"+generic_name+"|"+dose_form+"|"+strength+"|"+dose_amt+"|"+route+"|"+frequency+"|"+unit+"|"+necessity+"|"+extra_str);
		// System.out.println(rx_generic_map.get("11124"));
		// System.out.println(rx_code);
		/*
		if (rx_code != null && !rx_code.equals("") && !rx_code.isEmpty()) {
			// System.out.println(rx_code);
			if (rx_generic_map.containsKey(rx_code)) {
				generic_name = rx_generic_map.get(rx_code).split("_")[0];
			}
		}*/

		if (umls_code == "null") {
			umls_code = "";
		}
		if (rx_code == "null") {
			rx_code = "";
		}
		if (generic_name == "null") {
			generic_name = "";
		}
		out.write(index + 1 + "\t" + sent_text + "|" + drug + "|" + brand + "|"
				+ dose_form + "|" + strength + "|" + dose_amt + "|" + route
				+ "|" + frequency + "|" + duration + "|" + necessity + "|"
				+ umls_code + "|" + rx_code + "|" + generic_code +"|"+generic_name + "\n");

		writeLog("** Output print_result :\n");
		writeLog(index + 1 + "\t" + drug + "|" + brand + "|" + dose_form + "|"
				+ strength + "|" + dose_amt + "|" + route + "|" + frequency
				+ "|" + duration + "|" + necessity + "|" + umls_code + "|"
				+ rx_code +"|"+ generic_code + "|" + generic_name + "\n");
		return sig;

	}
}
