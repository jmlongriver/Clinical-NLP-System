package org.apache.NLPTools;

import java.util.*;

import org.apache.NLPTools.Token;
/**
 * Class for recording the began position and end position of a sentence.
 * @author yonghuiwu
 *
 */

public class Sentence {
	//private members
	private int start_token_index;
	private int end_token_index;
	private int absStartPos=-1;
	private int absEndPos=-1;
	private Vector<TextSection> tokens;
	//Constructor
	public Sentence(){
		
	}
	
	public void setTokens(Vector<TextSection> tags){
		this.tokens = tags;
	}
	public Vector<TextSection> getTokens(){
		return tokens;
	}
	//public functions
	public void setAbsStart(int pos){
		this.absStartPos=pos; 
	}
	
	public void setAbsEnd(int pos){
		this.absEndPos=pos; 
	}
	
	public void setStartTokenIndex(int pos){
		this.start_token_index=pos; 
	}
	
	public int startTokenIndex(){
		return this.start_token_index; 
	}
	
	public int endTokenIndex(){
		return this.end_token_index; 
	}
	
	public void setEndTokenIndex(int pos){
		this.end_token_index=pos; 
	}
	
	
	public int absStart(){
		return this.absStartPos;
	}
	
	public int absEnd(){
		return this.absEndPos;
	}
	
	
	public int token_num(){
		return this.end_token_index-this.start_token_index+1;
	}
	
	public int str_lenth(){
		return this.absEndPos-this.absStartPos;
	}
	
	
	public void print(){
		for(int i=0;i<=this.end_token_index;i++){
			System.out.print(i+' ');
		}
		System.out.print('\n');
	}
	
}
