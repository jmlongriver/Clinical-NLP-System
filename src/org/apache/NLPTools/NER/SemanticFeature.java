package org.apache.NLPTools.NER;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import org.apache.NLPTools.Document;
import org.apache.NLPTools.Global;
import org.apache.NLPTools.Sentence;
import org.apache.NLPTools.Token;
import org.apache.NLPTools.Global.SuffixArrayResult;
import org.apache.NLPTools.Global.TextSectionType;
import org.apache.medex.DrugTag;
import org.apache.medex.Lexicon;
import org.apache.medex.Main;
import org.apache.algorithms.SuffixArray;
import org.apache.log4j.FileAppender;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import java.util.Properties;

public class SemanticFeature extends Feature {
	public Lexicon lex = null;
	private Logger logger;
	public SemanticFeature(Lexicon lex){
		//System.out.println("hello");
		this.lex = lex;
		logger = Logger.getLogger(SemanticFeature.class.getName());
		SimpleLayout layout = new SimpleLayout();    
	    try{
	    	FileAppender appender = new FileAppender(layout,"log_semantic.txt",false); 
	    	logger.addAppender(appender);
		    logger.setLevel(Level.DEBUG);
	    }
	    catch(Exception e){
	    	System.out.println(e.toString());
	    }
	}
	@Override
	
	/**
	 * create POS tag for each token in the given document 
	 * 
	 * @param doc an instance of Document
	 * @return a POS tag
	 * 
	 * @see POSFeature
	 */
	public String createFeatureInstance(Document doc, Properties prop){
		//System.out.println("enter into here");
		//System.out.println("size:"+lex.lex_list().size());
		HashMap<Integer, String> semantic_map = new HashMap<Integer, String>();
		
		//logger.debug(doc.original_txt() + "\n");
		//System.out.println("size:"+lex.lex_list().size());
			
		SuffixArray sa = new SuffixArray(doc, lex, '\t', Global.SuffixArrayMode.WORD,Global.SuffixArrayCaseMode.NON_SENSITIVE);
		Vector<SuffixArrayResult> result = sa.search();
		//System.out.println("here");
		for (int i = 0; i < result.size(); i++) {
			
			String str = doc.get_str_by_token(result.get(i).start_token,result.get(i).end_token);
			int start = result.get(i).start_token;
			int end = result.get(i).end_token;
			String semantic_type = result.get(i).semantic_type;
			//logger.debug(start+"\t"+end+"\t"+str+"\t"+semantic_type+"\n");
			
			//System.out.println(semantic_type);
			for(int j = start; j<=end; j++){
				
				if(j==start)
				{
					if(!semantic_map.containsKey(j)){
						semantic_map.put(j, prop.getProperty("BEGIN_PREFIX") + semantic_type );
					}
					
				}
				else
				{
					if(!semantic_map.containsKey(j)){
						semantic_map.put(j, prop.getProperty("INTER_PREFIX") + semantic_type);
					}
					
				}
			}

		}
		//System.out.println("load lexicon finished");
		//System.out.println(semantic_map.get(582));
		String output = "";
		Vector<Sentence> final_sents = doc.sentence();
		Vector<Token> token_list = doc.token_vct();
		for (int sent_index = 0; sent_index < final_sents.size(); sent_index++) {
			Sentence s = final_sents.get(sent_index);
			int start_token_index = s.startTokenIndex();
			int end_token_index = s.endTokenIndex();
			String[] token_strs = new String[end_token_index-start_token_index+1];
			
			for (int cur_token_index = start_token_index; cur_token_index <= end_token_index; cur_token_index++) {
				Token cur_token = token_list.get(cur_token_index);
				String Token_str = cur_token.str();
				if(semantic_map.containsKey(cur_token_index)){
					//logger.debug(cur_token_index+"\t"+semantic_map.get(cur_token_index));
					output += semantic_map.get(cur_token_index) + "\n";
				}
				else{
					output += "TK\n";
				}
			}
			output += "\n";
		}
		//System.out.println(output);
		return output;
	}
}