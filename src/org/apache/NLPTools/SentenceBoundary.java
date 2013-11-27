package org.apache.NLPTools;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

import org.apache.NLPTools.Document;
import org.apache.NLPTools.Global.TextSectionType;
import org.apache.medex.Lexicon;

/**
 * Sentence Boundary program 
 * @author yonghuiwu
 *
 */

public class SentenceBoundary implements Global{

	/**
	 * 
	 */
	
	//private members
	private Document doc;
	private Map<String,Integer> word_map;
	private Map<String,Integer> abbr_map;

	private StringBuffer sbuf;
	
	/**
	 * Constructor
	 */
	public SentenceBoundary(String word_fname,String abbr_fname) {
		// TODO Auto-generated constructor stub
		this.word_map=new HashMap<String,Integer>();
		this.abbr_map=new HashMap<String,Integer>();
		this.sbuf=new StringBuffer();
		
		
		try{
			//load english words from word_fname
			//System.out.println("load english word from " + word_fname);
			FileInputStream fstream = new FileInputStream(word_fname);
			  
			DataInputStream in = new DataInputStream(fstream);
			//System.out.println("here ");
			  
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			StringBuffer lines=new StringBuffer();  
			int count=0;
			while ((strLine = br.readLine()) != null)   {
				this.word_map.put(strLine.trim().toLowerCase(), count);
				count=count+1;
			}
			
			//System.out.println("load english words: "+this.word_map.size());
			
			//load abbreviation from abbr_fname
			//System.out.println("load abbreviation from " + abbr_fname);
			br.close();
			in.close();
			fstream.close();
			
			fstream = new FileInputStream(abbr_fname);
			  
			in = new DataInputStream(fstream);
			//System.out.println("here ");
			  
			br = new BufferedReader(new InputStreamReader(in));
			strLine="";
			lines.setLength(0);
			
			count=0;
			while ((strLine = br.readLine()) != null)   {
				this.abbr_map.put(strLine.trim().toLowerCase(), count);
				count=count+1;
			}
			
			//System.out.println("load abbreviations: "+this.abbr_map.size());
			
			br.close();
			in.close();
			fstream.close();
			
			
		}
		catch (Exception e){
			System.err.println("Error: " + e.getMessage());	 
		}
			
			
		
		
	}
	
	//private founctions
	
	private void add_token(Token tok){
		
		this.doc.add_boundary_token(tok);
		int tmp=this.doc.boundary_token_vct().size()-1;
		this.doc.add_boundary_start_map(this.sbuf.length(), tmp);
		this.doc.add_boundary_end_map(this.sbuf.length()+tok.str().length(), tmp);
		this.doc.add_boundary_token2start_map(tmp, this.sbuf.length());
		this.doc.add_boundary_token2end_map(tmp, this.sbuf.length()+tok.str().length());
		//Build map index for partially match. The partially match will consider the partially match correct, when there is only one '.' at the end
		//E.g., 'p.o' match for 'p.o.'
		//
		
		String word=tok.str();

		if (word.charAt(word.length()-1) == '.'){
			this.doc.add_boundary_end_map(this.sbuf.length()+tok.str().length()-1, tmp);
		}
		this.sbuf.append(tok.str()+' ');
		
	}
	
	private void add_boundary(){
		this.sbuf.setCharAt(this.sbuf.length()-1, '\n');
	}
	//public functions
	/**
	 * The main process to detect sentence boundaries.
	 */
	public void detect_boundaries(Document doc){
		//System.out.println("Start detect boundary");
		this.doc=doc;
		//System.out.println("set sbd");
		
		this.sbuf.setLength(0);
		String word="";
		
		Sentence sent=new Sentence();
		sent.setAbsStart(0);
		sent.setStartTokenIndex(0);
		
		//System.out.println("# sentence:"+this.doc.sentence().size());
		for(int i=0;i<this.doc.sentence().size();i++){
			//System.out.println("-----Sentence :"+i);
			Sentence sent2=this.doc.sentence().get(i);
			//System.out.println("abs start:"+sent2.absStart()+"abs end:"+sent2.absEnd());
			//System.out.println("original:"+this.doc.original_txt().substring(sent2.absStart(),sent2.absEnd()));
			for (int j=this.doc.sentence().get(i).startTokenIndex();j<=this.doc.sentence().get(i).endTokenIndex();j++){
				
				boolean sent_flag=false;
				
				Token token=this.doc.token_vct().get(j);
				//System.out.println("current token:"+token.str());
				
				Token next_token=null;
				word=token.str();
				String next_word="";
				if(j+1<this.doc.token_vct().size()){
					next_token=this.doc.token_vct().get(j+1);
					next_word=next_token.str();
				}
				
				if(next_token !=null){
					//System.out.println("next token:"+next_token.str());
				}
				
				
				//System.out.println(word+ " list : "+Global.Util.is_list(word));
				if (word == "." ||Global.Util.is_list(word)){// situations that keep the original token
					//System.out.println("token2: "+ttok2.str());
					Token tok=new Token(token);
					this.add_token(tok);
					if (word=="."){
						sent_flag=true;
					}
					else{
						//System.out.println("list:"+word);
					}
					
				}
				else{
					// handle the '.' within word
					int dot_num=Global.Util.dot_num(word);
					if (dot_num>0){
						//System.out.println(word+"-----has dot num:"+dot_num);
						
						if (dot_num==1){
							// dot is the first symbol of current word
							if (word.charAt(0)=='.'){
								//System.out.println("1 dot begining:");
								Token tok=new Token(token);
								this.add_token(tok);
							}
							else if (word.charAt(word.length()-1)=='.'){
								//System.out.println("1 dot at end: next word|"+next_word);
								
								
								if (!this.word_map.containsKey(word.substring(0, word.length()-1).toLowerCase()) && (next_token!=null) && !Global.Util.is_sentence_start(next_word) && (this.abbr_map.containsKey(word.toLowerCase()) || this.abbr_map.containsKey(word.substring(0, word.length()-1).toLowerCase()))   ){
									//System.out.println("part of abbr");
									Token tok=new Token(token);
									this.add_token(tok);
								}
								// dot is period, not part of abbreviation
								else {
									//System.out.println("period dot");
									Token tok=new Token(token.startPos(),token.endPos()-1,Global.TextSectionType.TOKEN,word.substring(0, word.length()-1));
									this.add_token(tok);
									
									Token tok2=new Token(token.endPos()-1,token.endPos(),Global.TextSectionType.TOKEN,".");
									this.add_token(tok2);
									
									sent_flag=true;
								}
							}
							else{ // dot is in the center of the token
								//System.out.println("-----dot in center:"+word);
								int pos=word.indexOf(".");
								String lword=word.substring(0,pos);
								String rword=word.substring(pos+1);
								//System.out.println("rword:"+rword);
								if(Character.isUpperCase(rword.charAt(0)) && (this.word_map.containsKey(rword.toLowerCase()) ) ){
									//split the token by dot
									//add left part word
									Token tok=new Token(token.startPos(),token.startPos()+pos,Global.TextSectionType.TOKEN,lword);
									this.add_token(tok);
									
									Token tok2=new Token(token.startPos()+pos,token.startPos()+pos+1,Global.TextSectionType.TOKEN,".");
									this.add_token(tok2);
									
									// add new sentence 
									this.add_boundary();
									//System.out.println("new sentence added!!");
									sent.setAbsEnd(token.endPos());
									sent.setEndTokenIndex(this.doc.boundary_token_vct().size()-1);
									this.doc.boundary_sentence().add(sent);
									sent=new Sentence();
									if (next_token!=null){
										sent.setAbsStart(next_token.startPos());
										sent.setStartTokenIndex(this.doc.boundary_token_vct().size());
									}
									
									//add right part word
									Token tok3=new Token(token.startPos()+pos+1,token.endPos(),Global.TextSectionType.TOKEN,rword);
									this.add_token(tok3);
									
								}
								else{
									Token tok=new Token(token);
									this.add_token(tok);
								}            
								
							}
						}
						else{// current token has more than one dot
							//System.out.println("more than one dot:"+word);
							if (word.charAt(word.length()-1)=='.'){
								String lword=word.substring(0,word.length()-1);
								if((Global.Util.is_digit(lword)) || (next_word.length()>0 && Character.isUpperCase(next_word.charAt(0)) )){
									Token tok=new Token(token.startPos(),token.startPos()+lword.length(),Global.TextSectionType.TOKEN,lword);
									this.add_token(tok);
									
									Token tok2=new Token(token.startPos()+lword.length(),token.startPos()+lword.length()+1,Global.TextSectionType.TOKEN,".");
									this.add_token(tok2);
									sent_flag=true;
								}
								else{
									Token tok=new Token(token);
									this.add_token(tok);
								}
								
							}
							else{
								Token tok=new Token(token);
								this.add_token(tok);
							}
						}
						
					}
					else{
						//System.out.println("normal word ,just add in:");
						Token tok=new Token(token);
						this.add_token(tok);
						if ( (i == this.doc.sentence().size()-1) && (j == this.doc.sentence().get(i).endTokenIndex())){
							sent_flag=true;
						}
					}
				}
				
				
				
				if (sent_flag){
					this.add_boundary();
					//System.out.println("new sentence added!!");
					sent.setAbsEnd(token.endPos());
					sent.setEndTokenIndex(this.doc.boundary_token_vct().size()-1);
					this.doc.boundary_sentence().add(sent);
					sent=new Sentence();
					if (next_token!=null){
						sent.setAbsStart(next_token.startPos());
						sent.setStartTokenIndex(this.doc.boundary_token_vct().size());
					}
					
				}
				
				
			}
			//Tokens in current original sentence is finished, handle the hard '\n' at the end of line
			//get the first word for next line
			//System.out.println("current setence ended. start:"+sent.absStart());
			if (this.doc.boundary_token_vct().get(this.doc.boundary_token_vct().size()-1).endPos() >sent.absStart() && sent.absStart()>=0){
				//
				Token next_token=null;
				String next_word="";
				if (i+1<this.doc.sentence().size()){
					next_token=this.doc.token_vct().get(this.doc.sentence().get(i+1).startTokenIndex());
					next_word=next_token.str();
				}
				
				if(next_word.length() >0 && (Character.isUpperCase(next_word.charAt(0)) || Global.Util.is_list(next_word))){
					this.add_boundary();
					sent.setAbsEnd(this.doc.boundary_token_vct().get(this.doc.boundary_token_vct().size()-1).endPos());
					sent.setEndTokenIndex(this.doc.boundary_token_vct().size()-1);
					//System.out.println("handle '/n' new sentence added!! "+sent.absStart()+" : "+sent.absEnd());
					this.doc.boundary_sentence().add(sent);
					sent=new Sentence();
					if (next_token!=null){
						sent.setAbsStart(next_token.startPos());
						sent.setStartTokenIndex(this.doc.boundary_token_vct().size());
					}
				}
			}
		
			
		}
		//System.out.println(""+this.doc.boundary_sentence().get(this.doc.boundary_sentence().size()-1).endTokenIndex() +":"+ this.doc.token_vct().size());
		
	
		if (this.doc.boundary_token_vct().get(this.doc.boundary_token_vct().size()-1).startPos() > sent.absStart() && sent.absStart()>=0){
			//System.out.println("last sentence added!!");
			sent.setAbsEnd(this.doc.boundary_token_vct().get(this.doc.boundary_token_vct().size()-1).endPos());
			sent.setEndTokenIndex(this.doc.boundary_token_vct().size()-1);
			this.doc.boundary_sentence().add(sent);
			
		}
		this.doc.set_boundary_norm_str(this.sbuf.toString());
		//System.out.println("boundary detect end");
		
		this.doc.set_sbd_flag(true);
	}

}
