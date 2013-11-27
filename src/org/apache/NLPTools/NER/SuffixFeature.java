package org.apache.NLPTools.NER;

import java.util.Vector;

import org.apache.NLPTools.Document;
import org.apache.NLPTools.Sentence;
import org.apache.NLPTools.Token;
import java.util.Properties;

public class SuffixFeature extends Feature {


	@Override
	
	/**
	 * create suffix for given document 
	 * 
	 * @param doc an instance of Document
	 * @return a string contains suffix feature
	 * 
	 * @see SuffixFeature
	 */
	public String createFeatureInstance(Document doc, Properties prop){
		String result = "";
		Vector<Sentence> final_sents = doc.sentence();
		Vector<Token> token_list = doc.token_vct();
		for (int sent_index = 0; sent_index < final_sents.size(); sent_index++) {
			Sentence s = final_sents.get(sent_index);
			int start_token_index = s.startTokenIndex();
			int end_token_index = s.endTokenIndex();
			for (int cur_token_index = start_token_index; cur_token_index <= end_token_index; cur_token_index++) {
				Token cur_token = token_list.get(cur_token_index);
				String Token_str = cur_token.str();
				if(Token_str.length()>=3){
					result += Token_str.substring(Token_str.length()-3,Token_str.length()) + "\t"+Token_str.substring(Token_str.length()-2,Token_str.length()) + "\t"+Token_str.substring(Token_str.length()-1,Token_str.length())+"\n";
				}
				else if(Token_str.length() == 2){
					result += Token_str.substring(Token_str.length()-2,Token_str.length()) + "\t"+Token_str.substring(Token_str.length()-1,Token_str.length()) + "\t"+prop.getProperty("EMPTY_VALUE")+"\n";
				}
				else if(Token_str.length() == 1){
					result += Token_str.substring(Token_str.length()-1,Token_str.length()) + "\t"+prop.getProperty("EMPTY_VALUE")+"\t"+prop.getProperty("EMPTY_VALUE")+"\n";
				}
				else{
					result += prop.getProperty("EMPTY_VALUE")+"\t"+prop.getProperty("EMPTY_VALUE")+"\t"+prop.getProperty("EMPTY_VALUE")+"\n";
				}
			}
			result += "\n";
		}
		return result;
	}
}
