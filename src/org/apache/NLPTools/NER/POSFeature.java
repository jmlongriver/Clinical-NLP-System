package org.apache.NLPTools.NER;

import java.util.Vector;

import org.apache.NLPTools.Document;
import org.apache.NLPTools.Sentence;
import org.apache.NLPTools.Token;
import org.apache.NLPTools.POSTagger;
import java.util.Properties;

public class POSFeature extends Feature {
	

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
		POSTagger posTagger = new POSTagger();
		String result = "";
		Vector<Sentence> final_sents = doc.sentence();
		Vector<Token> token_list = doc.token_vct();
		for (int sent_index = 0; sent_index < final_sents.size(); sent_index++) {
			Sentence s = final_sents.get(sent_index);
			int start_token_index = s.startTokenIndex();
			int end_token_index = s.endTokenIndex();
			String[] token_strs = new String[end_token_index-start_token_index+1];
			int index = 0;
			for (int cur_token_index = start_token_index; cur_token_index <= end_token_index; cur_token_index++) {
				Token cur_token = token_list.get(cur_token_index);
				String Token_str = cur_token.str();
				token_strs[index] = Token_str;
				index ++;
							
			}
			String tags[] = posTagger.tagging(token_strs);
			for(int i =0; i<tags.length; i++){
				result += tags[i]+"\n";
			}
			result += "\n";
			
		}
		
		
		return result;
	}
}