package org.apache.medex;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.NLPTools.Global;
import org.apache.NLPTools.Sentence;
import org.apache.NLPTools.Token;
import org.apache.NLPTools.Global.TextSectionType;

public class Lexicon {
	
	//members
	private Vector<String> lexicon_list;
	private Vector<String> semantic_list;

	public Lexicon(String fname) throws IOException{
		// TODO Auto-generated constructor stub
		lexicon_list=new Vector<String>();
		semantic_list=new Vector<String>();
		
		File f = new File(fname); 
		FileInputStream fis = new FileInputStream(f);
        BufferedInputStream bis = new BufferedInputStream(fis);
        DataInputStream dis = new DataInputStream(bis);
        String record = null;

        //System.out.println("while");
        //BufferedWriter log=new BufferedWriter(new FileWriter(System.getProperty("user.dir") + File.separator + "lex_log.txt"));
        while ( (record = dis.readLine()) != null ) {
        	//System.out.println("11"+record);
            String[] items = record.split("\t");
            //System.out.println(items);
            if(items.length == 2)
            {
            	String term = items[0].toLowerCase();
            	String sem = items[1].trim();
            	if(term.length()==0 ){
            		continue;
            	}
            	term=term.trim()+' ';
            	String norm_term=this.tokenization(term);
            	
            	this.lexicon_list.add(norm_term);
            	this.semantic_list.add(sem);
            	
            	//add cleaned term
            	String cterm=this.remove_special_symbol(norm_term);
            	
            	if (! cterm.equals(norm_term)){
            		this.lexicon_list.add(cterm);
                	this.semantic_list.add(sem);
            	}
            	//System.out.println("add");
            	
        	
            } 
        }// end of while
        //log.close();
        
       fis.close();
       //System.out.println("Lexicon inititate over from "+fname);
       //System.out.println("Total# "+this.lexicon_list.size()+'\n');
	}
	
	//private functions
	/**
     * Remove special symbols(+/-) at the beginning and end of the Lexicon.
     *
     * @param str    the input word from lexicon
     * @return       the trimmed string
     */
	private String remove_special_symbol(String str){
		int start_pos=0;
		int llen=str.length();
		while(start_pos<llen && (this.removable_symbol(str.charAt(start_pos)) || str.charAt(start_pos)==' ')){
			start_pos=start_pos+1;
		}
		
		int end_pos=llen-1;
		while(end_pos>=0 && (this.removable_symbol(str.charAt(end_pos)) || str.charAt(end_pos)==' ')){
			end_pos=end_pos-1;
		}
		
		if (end_pos>=start_pos){
			return str.substring(start_pos,end_pos+1);
		}
		else{
			return "";
		}
	}
	
	/**
     * Define special symbols to remove
     *
     * @param ch    the input ch
     * @return      boolean
     */
	private boolean removable_symbol(char ch){
		if(ch=='-' || ch=='+' || ch=='/' || ch=='\\' || ch=='@' || ch=='&'){
			return true;
		}
		else{
			return false;
		}
	}
	
	
	/**
     * Tokenize the lexicons; match the string consistent with the tokenization in the document
     *
     * @param txt    the input word from lexicon
     * @return       the tokenized string
     */
	private String tokenization(String txt){
		int llen=txt.length();
		int token_ch_start=0; //set new start only when new token added
		int cur_pos=0;
		
		StringBuffer norm_txt=new StringBuffer();
		

		while(cur_pos<llen & (txt.charAt(cur_pos)==' ' || txt.charAt(cur_pos)=='\n')){
			cur_pos=cur_pos+1;
		}
		token_ch_start=cur_pos;
		while(cur_pos<llen){
			char ch=txt.charAt(cur_pos);
			//System.out.println("current token:|"+ch+"|");
			//current char is a separator
			if (Global.Util.is_sep(ch)){
				if(cur_pos>token_ch_start){// do not add empty symbols
					int spos=token_ch_start;
					int epos=cur_pos;
					String str=txt.substring(spos, epos);
					norm_txt.append(str+' ');
				}
				
				cur_pos=cur_pos+1;					
				while(cur_pos<llen && txt.charAt(cur_pos)==' '){						
					cur_pos=cur_pos+1;					
				}
				
				token_ch_start=cur_pos;
			}
			else if (Global.Util.is_punctuation(ch) || Global.Util.is_braces(ch)){
				//current token is a punctuation or braces
				if (cur_pos>token_ch_start){//current token length>1, split the punctuation
					int spos=token_ch_start;
					int epos=cur_pos;
					String str=txt.substring(spos, epos);
					norm_txt.append(str+' ');
				}
				//add current punctuation as token
				norm_txt.append(String.valueOf(ch)+' ');
				cur_pos=cur_pos+1;
				
				while(cur_pos<llen && txt.charAt(cur_pos)==' '){
					cur_pos=cur_pos+1;
				}
				token_ch_start=cur_pos;
			}
			else{
				//current token is not a token boundary
				cur_pos=cur_pos+1;
			}
		}
		return norm_txt.toString().trim();
	}
	
	
	//public functions
	public Vector<String> lex_list(){
		return this.lexicon_list;
	}
	
	public Vector<String> sem_list(){
		return this.semantic_list;
	}
	
	

}
