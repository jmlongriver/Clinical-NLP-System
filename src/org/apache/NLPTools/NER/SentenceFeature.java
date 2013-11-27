package org.apache.NLPTools.NER;

import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.NLPTools.Document;
import org.apache.NLPTools.Sentence;
import org.apache.NLPTools.Token;
import java.util.Properties;

public class SentenceFeature extends Feature {


	@Override
	
	/**
	 * create sentence level features for given document which include:
	 * 1> sentence length >5 or not
	 * 2> if sentence starts with number
	 * 3> if sentence ends with colon
	 * 4> length of each word  
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
			String lengthFeature;
			String startFeature = prop.getProperty("EMPTY_VALUE");
			String endFeature = prop.getProperty("EMPTY_VALUE");
			String wordLenFeature;
			Sentence s = final_sents.get(sent_index);
			int start_token_index = s.startTokenIndex();
			int end_token_index = s.endTokenIndex();
			int sent_length = end_token_index - start_token_index + 1;
			if(sent_length > 5){
				lengthFeature = "5+";
			}
			else{
				lengthFeature = "5-";
			}
			String firstTokenStr = token_list.get(start_token_index).str();
			String endTokenStr = token_list.get(end_token_index).str();
			Pattern p_start_num = Pattern.compile("^[0-9]");
		    if(p_start_num.matcher(firstTokenStr).find()){
		    	startFeature = "ENUM_START";
		    }
		    if(endTokenStr.endsWith(":")){
		    	endFeature = "COLON_END";
		    }
			for (int cur_token_index = start_token_index; cur_token_index <= end_token_index; cur_token_index++) {
				Token cur_token = token_list.get(cur_token_index);
				String Token_str = cur_token.str();
				wordLenFeature = Integer.toString(Token_str.length());
				result += lengthFeature + "\t" + startFeature + "\t" + endFeature + "\t" + wordLenFeature + "\n";
			}
			result += "\n";
		}
		return result;
	}
}