package org.apache.NLPTools.NER;

import java.util.Arrays;
import java.util.Vector;

import org.apache.NLPTools.Document;
import org.apache.NLPTools.Sentence;
import org.apache.NLPTools.Token;
import org.apache.uima.pear.util.StringUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Properties;

public class OrthoFeature extends Feature {

	public static final String[] Comma = {","};
	public static final String[] FullStop = {"."};
	public static final String[] Percent = {"%"};
	public static final String[] Hyphon = {"-"};
	public static final String[] Backslash = {"/"};
	public static final String[] OpenSquare = {"{"};
	public static final String[] CloseSquare = {"}"};
	public static final String[] Colon = {":"};
	public static final String[] SemiColon = {";"};
	public static final String[] OpenParen = {"("};
	public static final String[] CloseParen = {")"};
	public static final String[] Determiner = {"the"};
	public static final String[] Conjunction = {"and"};
	public static final String[] Other = {"*","+","#"};
	public static final String[] Greek = {"alpha","beta","lamda","omega","theta","tetra"};
		    
	@Override
	
	/**
	 * create orthographic feature for given document 
	 * 
	 * @param doc an instance of Document
	 * @return a string contains orthographic feature
	 * 
	 * @see OrthoFeature
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
				//System.out.println(Token_str);
				result += getOrthoFeature(Token_str)+"\n";
				
			}
			result += "\n";
			
		}
		
		
		return result;
	}
	
	/**
	 * the fuction generate the orthographic feature for a given word
	 * 
	 * @param doc an instance of Document
	 * @return a string contains orthographic feature
	 * 
	 * @see CRF_classifier
	 */
	protected String getOrthoFeature(String word){
		String word_feature = "";
		
		Pattern p_dig = Pattern.compile("^\\d+$");
		if(p_dig.matcher(word).find()){
	        word_feature = "DigitNumber";
		}
		
		Pattern p_InitCap = Pattern.compile("^[A-Z][a-z]");
	    if(p_InitCap.matcher(word).find()){
	        word_feature = "InitCap";
	    }
	    
	    Pattern p_let_dig = Pattern.compile("^[aA-zZ][0-9]");
	    if(p_let_dig.matcher(word).find()){
	        word_feature = "LettersAndDigit";
	    }
	    
	    Pattern p_percent = Pattern.compile("^[\\d.]+\\%");
	    if(p_percent.matcher(word).find()){
	        word_feature = "DigitAndPercent";
	    }
	    Pattern p_greek = Pattern.compile("(alpha)|(beta)|(gama)|(lamda)|(omega)|(theta)|(tetra)");
	    
	    if(p_greek.matcher(word).find()){
	        word_feature = "GreekLetter";
	    }
	    if(StringUtil.isLowerCase(word)){
	        word_feature = "LowerCase";
	    }
	    if(StringUtil.isUpperCase(word)){
	        word_feature = "UpperCase";
	    }
	    if(Arrays.asList(Comma).contains(word)){
	    	word_feature = "Comma";
	    }
	     
	    if(Arrays.asList(FullStop).contains(word)){
	        word_feature = "FullStop";
	    }
	    if(Arrays.asList(Percent).contains(word)){
	        word_feature = "Percent";
	    }
	    if(Arrays.asList(Hyphon).contains(word)){
	        word_feature = "Hyphon";
	    }
	    if(Arrays.asList(Backslash).contains(word)){
	        word_feature = "Backslash";
	    }
	    if(Arrays.asList(OpenSquare).contains(word)){
	        word_feature = "OpenSquare";
	    }
	    if(Arrays.asList(CloseSquare).contains(word)){
	        word_feature = "CloseSquare";
	    }
	    if(Arrays.asList(Colon).contains(word)){
	        word_feature = "Colon";
	    }
	    if(Arrays.asList(SemiColon).contains(word)){
	        word_feature = "SemiColon";
	    }
	    if(Arrays.asList(OpenParen).contains(word)){
	        word_feature = "OpenParen";
	    }
	    if(Arrays.asList(CloseParen).contains(word)){
	        word_feature = "CloseParen";
	    }
	    if(Arrays.asList(Determiner).contains(word)){
	        word_feature = "Determiner";
	    }
	    if(Arrays.asList(Conjunction).contains(word)){
	        word_feature = "Conjunction";
	    }
	    
	    if(word_feature.isEmpty() && word.length() > 0){
	        word_feature = "Other";
	    }
	    return word_feature;
	}
}
