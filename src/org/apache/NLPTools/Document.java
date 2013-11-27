package org.apache.NLPTools;

import java.util.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.NLPTools.Sentence;
import org.apache.NLPTools.Token;
import org.apache.medex.DrugTag;

/**
 * Class: Document
 * This class corresponding to each document, all input documents will be represented as a 'Document' class, which will be passed to the other functional modules
 * @author yonghuiwu
 * 
 */

public class Document implements Global{
	//private members
	private boolean sbd_flag;// denoting whether the doc is processed by sentence boundary
	
	private String dir; //dir of the file
	private String fname; // the file name
	
	//default token ,sentence and map
	private Vector<Token> token_vct=null;//original token
	private Vector<Sentence> sentence=null;//original sentences
	private Map<Integer,Integer> start_map=null;
	private Map<Integer,Integer> end_map=null;
	private Map<Integer,Integer> token2start_map=null;
	private Map<Integer,Integer> token2end_map=null;
	
	//original token ,sentence and map
	private Vector<Token> original_token_vct=null;//original token
	private Vector<Sentence> original_sentence=null;//original sentences
	private Map<Integer,Integer> original_start_map=null;
	private Map<Integer,Integer> original_end_map=null;
	private Map<Integer,Integer> original_token2start_map;
	private Map<Integer,Integer> original_token2end_map;
	private String original_norm_str=null;
	
	//token, sentence, map after sentence boundary
	private Vector<Token> boundary_token_vct=null;//Tokens after sentence boundary
	private Vector<Sentence> boundary_sentence=null;//after sentence boundary
	//map position in boundary_norm_str to token index
	private Map<Integer,Integer> boundary_start_map=null;
	private Map<Integer,Integer> boundary_end_map=null;
	//map token index into position in boundary_norm_str
	private Map<Integer,Integer> boundary_token2start_map;
	private Map<Integer,Integer> boundary_token2end_map;
	private String boundary_norm_str=null;
	
	
	
	private String original_txt;
	private Vector<DrugTag> drug_tags;
	private Vector<DrugTag> filtered_drug_tags;
	private Vector<Vector<DrugTag>> signature;
	
	
	
	//for Drug taggers

	//constructor 
	/**
     * Constructor
     * The default constructor for class 'Document'. The following functions were implemented:
     * 1. Tokenization, split each token simply by space ' ' or '\n'. 
     * 2. Simple sentence boundary, split sentences by '\n'. The new sentence boundaries will be detected in class 'SentenceBoundary'
     * @param itxt    the input string
     * @return        N/A
     */
	public Document(String itxt,String fname){
		//System.out.println("Document initiate start");
		
		this.sbd_flag=false;
		this.fname=fname;
		this.dir="";
		
		//initiate original
		this.original_token_vct=new Vector<Token>();
		this.original_sentence=new Vector<Sentence>();
		this.original_start_map=new HashMap<Integer,Integer>();
		this.original_end_map=new HashMap<Integer,Integer>();
		this.original_token2start_map=new HashMap<Integer,Integer>();
		this.original_token2end_map=new HashMap<Integer,Integer>();
		this.original_norm_str="";

		//initiate boundary
		this.boundary_token_vct=new Vector<Token>();
		this.boundary_sentence=new Vector<Sentence>();
		this.boundary_start_map=new HashMap<Integer,Integer>();
		this.boundary_end_map=new HashMap<Integer,Integer>();
		this.boundary_token2start_map=new HashMap<Integer,Integer>();
		this.boundary_token2end_map=new HashMap<Integer,Integer>();
		this.boundary_norm_str="";
		
		//initiate tags
		this.drug_tags=new Vector<DrugTag>();
		this.filtered_drug_tags=new Vector<DrugTag>();
		this.signature=new Vector<Vector<DrugTag>>();
		
		//set default token, sentence and map
		this.set_default_TSM();
		
		
		if (itxt.length()==0){
			//System.out.println("empty string");
			return;
		}
		this.original_txt=itxt;
		String txt="";
		if(itxt.charAt(itxt.length()-1)=='\n'){
			txt=itxt;
		}
		else{
			txt=itxt+'\n';
		}
		
	
		
		//--- simple tokenization
		int token_ch_start=0; //set new start only when new token added
		int sent_token_start=0; // set new sent token start when new sentence added
		int sent_ch_start=0; //set new sent_ch_start when new sentence added
		int cur_pos=0;
		int llen=txt.length();
		while(cur_pos<llen & (txt.charAt(cur_pos)==' ' || txt.charAt(cur_pos)=='\n' || txt.charAt(cur_pos)=='\r') ){
			cur_pos=cur_pos+1;
		}
		token_ch_start=cur_pos;
		
		//System.out.println("start simple token, ch_start:"+token_ch_start+" cur_pos:"+cur_pos);
		
		//System.out.println("text:|"+txt+"|");
		while(cur_pos<llen){
			char ch=txt.charAt(cur_pos);
			//System.out.println("current token:|"+ch+"|");
			//current char is a separator
			/*
			if (Global.Util.is_sep(ch)){
				if(cur_pos>token_ch_start){// do not add empty symbols
					int spos=token_ch_start;
					int epos=cur_pos;
					add_original_vct(txt,spos,epos);
				}
				if (ch=='\n'){//hand new sentence, only change the token_start_index when new line comes
					Sentence sent=new Sentence();
					sent.setAbsStart(sent_ch_start);
					sent.setStartTokenIndex(sent_token_start);
					
					sent.setAbsEnd(this.original_token_vct.get(this.original_token_vct.size()-1).endPos());
					sent.setEndTokenIndex(this.original_token_vct.size()-1);
					//System.out.println("sent start:"+sent.absStart()+" : "+sent.absEnd());
					//System.out.println("new line for enter:|"+this.original_txt.substring(sent.absStart(),sent.absEnd())+"|");
					this.original_sentence.add(sent);
					
					cur_pos=cur_pos+1;
					while(cur_pos<llen &&(txt.charAt(cur_pos)==' ' || txt.charAt(cur_pos)== '\n')){
						cur_pos=cur_pos+1;
					}
					// new line added, set new sent_token_start and sent_ch_start
					sent_token_start=this.original_token_vct.size();
					sent_ch_start=cur_pos;
				}
				else{
					cur_pos=cur_pos+1;
					while(cur_pos<llen && txt.charAt(cur_pos)==' '){
						cur_pos=cur_pos+1;
					}
				}
				token_ch_start=cur_pos;
			
			}
			else{
				//current token is not a token boundary
				cur_pos=cur_pos+1;
			}
			
			*/
			if (Global.Util.is_sep(ch)){
				if(cur_pos>token_ch_start){// do not add empty symbols
					int spos=token_ch_start;
					int epos=cur_pos;
					add_original_vct(txt,spos,epos);
				}
								
				
				if (ch=='\n'){//hand new sentence, only change the token_start_index when new line comes
					Sentence sent=new Sentence();
					sent.setAbsStart(sent_ch_start);
					sent.setStartTokenIndex(sent_token_start);
					
					sent.setAbsEnd(this.original_token_vct.get(this.original_token_vct.size()-1).endPos());
					sent.setEndTokenIndex(this.original_token_vct.size()-1);
					//System.out.println("sent start:"+sent.absStart()+" : "+sent.absEnd());
					//System.out.println("new line for enter:|"+this.original_txt.substring(sent.absStart(),sent.absEnd())+"|");
					this.original_sentence.add(sent);
					
					cur_pos=cur_pos+1;
					while(cur_pos<llen &&(txt.charAt(cur_pos)==' ' || txt.charAt(cur_pos)== '\n')){
						cur_pos=cur_pos+1;
					}
					// new line added, set new sent_token_start and sent_ch_start
					sent_token_start=this.original_token_vct.size();
					sent_ch_start=cur_pos;
				}
				else{
					cur_pos=cur_pos+1;
					while(cur_pos<llen && txt.charAt(cur_pos)==' '){
						cur_pos=cur_pos+1;
					}
				}
				token_ch_start=cur_pos;
			}
			else if (Global.Util.is_punctuation(ch) || Global.Util.is_braces(ch)){
				//current token is a punctuation or braces
				if (cur_pos>token_ch_start){//current token length>1, split the punctuation
					int spos=token_ch_start;
					int epos=cur_pos;
					add_original_vct(txt,spos,epos);
					
				}
				//add current punctuation as token 
				int spos=cur_pos;
				int epos=cur_pos+1;
				add_original_vct(txt,spos,epos);
				
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
		//---simple tokenization end
		
		//System.out.println("total # of sentences:" + this.sentences.size());
		//System.out.println("Document initiate over\n");
		build_original_norm_str_map();
		
		
	}
	
	//set the default token, sentence and map to the original parameters, before sentence boundary
	private void set_default_TSM(){
		this.token_vct=this.original_token_vct;
		this.sentence=this.original_sentence;
		this.start_map=this.original_start_map;
		this.end_map=this.original_end_map;
		this.token2start_map=this.original_token2start_map;
		this.token2end_map=this.original_token2end_map;
	}
	
	//set the default token, sentence and map to the boundary parameters
	private void set_boundary_TSM(){
		this.token_vct=this.boundary_token_vct;
		this.sentence=this.boundary_sentence;
		this.start_map=this.boundary_start_map;
		this.end_map=this.boundary_end_map;
		this.token2start_map=this.boundary_token2start_map;
		this.token2end_map=this.boundary_token2end_map;
	}
	
	//private functions
	/**
	 * add token into vector
	 */
	private void add_original_vct(String txt,int spos,int epos){
		//separate '.'+word in '.warfarin'
		String str=txt.substring(spos, epos);
		//separate '.'+word in '.warfarin'
		//System.out.println(str+" flag:"+Global.Util.is_unit(str));
		
		if (Global.Util.is_dot_word(str)){
			str=".";
			Token ttok2=new Token(spos,spos+1,TextSectionType.TOKEN, str);
			this.original_token_vct.add(ttok2);
			str=txt.substring(spos+1,epos);
			Token ttok3=new Token(spos+1,epos,TextSectionType.TOKEN, str);
			this.original_token_vct.add(ttok3);
		}
		//separate number+units
		
		else if(Global.Util.is_unit(str)){
			int tpos=str.length()-1;
			while(!Character.isDigit(str.charAt(tpos))){
				tpos=tpos-1;
			}
			//System.out.println("tpos:"+tpos);
			str=txt.substring(spos,spos+tpos+1);
			Token ttok2=new Token(spos,spos+tpos+1,TextSectionType.TOKEN, str);
			this.original_token_vct.add(ttok2);
			str=txt.substring(spos+tpos+1,epos);
			Token ttok3=new Token(spos+tpos+1,epos,TextSectionType.TOKEN, str);
			this.original_token_vct.add(ttok3);
		}
		else{
			Token ttok2=new Token(spos,epos,TextSectionType.TOKEN, str);
			//System.out.println("###add token end by separator : "+ttok2.str());
			this.original_token_vct.add(ttok2);
		}
		
	}
	/**
	 * build original normalized string from the token list by join the tokens with a space
	 */
	private void build_original_norm_str_map(){
		StringBuffer result=new StringBuffer(); 
		for(int i=0;i<this.original_sentence.size();i++){
			for (int j=this.original_sentence.get(i).startTokenIndex();j<=this.original_sentence.get(i).endTokenIndex();j++){
				Token tok=this.original_token_vct.get(j);
				this.original_start_map.put(result.length(), j);
				this.original_end_map.put(result.length()+tok.str().length(), j);
				
				
				this.original_token2start_map.put(j, result.length());
				this.original_token2end_map.put(j, result.length()+tok.str().length());
				
				
				String word=tok.str();

				if (word.charAt(word.length()-1) == '.'){
					this.original_end_map.put(result.length()+tok.str().length()-1, j);
				}
				
				result.append(this.original_token_vct.get(j).str()+' ');
				
			}
			int len=result.length();
			result.setCharAt(len-1, '\n');
		}
		this.original_norm_str= result.toString();
	}
	/**
	 * build normalized string from the token list after detect sentence boundary
	 */
	private void build_boundary_norm_str(){
		StringBuffer result=new StringBuffer(); 
		for(int i=0;i<this.boundary_sentence.size();i++){
			for (int j=this.boundary_sentence.get(i).startTokenIndex();j<=this.boundary_sentence.get(i).endTokenIndex();j++){
				result.append(this.boundary_token_vct.get(j).str()+' ');
				
			}
			int len=result.length();
			result.setCharAt(len-1, '\n');
		}
		this.boundary_norm_str=result.toString();
	}
	
	
	
	//
	//public functions
	public String fname(){
		return this.fname;
	}
	
	// get the mapping file for original position to the new position in boundary_norm_str
	public String get_boundary_map_str(){
		StringBuffer buf=new StringBuffer();
		StringBuffer result=new StringBuffer(); 
		for(int i=0;i<this.boundary_sentence.size();i++){
			for (int j=this.boundary_sentence.get(i).startTokenIndex();j<=this.boundary_sentence.get(i).endTokenIndex();j++){

				buf.append(this.boundary_token_vct.get(j).str()+"\t"+result.length());
				
				result.append(this.boundary_token_vct.get(j).str()+' ');
				buf.append("\t"+(result.length()-1));
				buf.append("\t"+this.boundary_token_vct.get(j).startPos()+"\t"+this.boundary_token_vct.get(j).endPos()+"\n");
				
			}
			buf.append("\n");
		}
		
		return buf.toString();
	}
	
	public void set_sbd_flag(boolean flag){
		this.sbd_flag=flag;
		this.set_boundary_TSM();
	}
	
	public boolean sbd_flag(){
		return this.sbd_flag;
	}
	
	/**
	 * Break long sentences according the settings in Global, to solve the crashes caused by the rule engine
	 */
	
	public void break_long_sentence(){
		int llen=this.boundary_sentence.size();
		int cur_pos=0;
		int total_len=0;
		while(cur_pos<llen){
			Sentence sent=this.boundary_sentence.get(cur_pos);
			int sent_len=sent.absEnd()-sent.absStart();
			if (sent_len > Global.MAX_LENGTH_LINE){// too long ,break sentence
				//System.out.println("Sentence: "+cur_pos+" len:"+sent_len+" too long|"+this.original_txt.substring(sent.absStart(),sent.absEnd()));
				Sentence sent2=new Sentence();
				sent2.setAbsStart(sent.absStart());
				sent2.setStartTokenIndex(sent.startTokenIndex());
				int tmp=0;
				int new_sent_ct=0;
				for (int j=sent.startTokenIndex();j<=sent.endTokenIndex();j++){
					tmp=tmp+this.boundary_token_vct.get(j).str().length()+1;
					
					if (tmp > Global.MAX_LENGTH_LINE ){
						//System.out.println("over max, add word:|"+this.boundary_token_vct.get(j).str()+"|");
						//close current new sentence
						sent2.setAbsEnd(this.boundary_token_vct.get(j).endPos());
						sent2.setEndTokenIndex(j);
						new_sent_ct=new_sent_ct+1;
						this.boundary_sentence.insertElementAt(sent2, cur_pos+new_sent_ct);
						int pos=(Integer)this.boundary_token2end_map.get(j);
						//System.out.println("token 2 end:"+pos);
						StringBuffer tsb=new StringBuffer();
						//System.out.println("add new sentence, with start:"+sent2.absStart()+" end:"+sent2.absEnd());
						this.boundary_norm_str=this.boundary_norm_str.substring(0, pos)+'\n'+this.boundary_norm_str.substring(pos+1, this.boundary_norm_str.length());
						
						tmp=0;
						if (j+1<=sent.endTokenIndex()){
							sent2=new Sentence();
							sent2.setAbsStart(this.boundary_token_vct.get(j+1).startPos());
							sent2.setStartTokenIndex(j+1);
						}
						else{
							sent2=null;
						}
						
					}
				}
				
				//handle the end of sentence
				if (sent2 !=null){
					sent2.setAbsEnd(sent.absEnd());
					sent2.setEndTokenIndex(sent.endTokenIndex());
					new_sent_ct=new_sent_ct+1;
					this.boundary_sentence.insertElementAt(sent2, cur_pos+new_sent_ct);
					//System.out.println("handle end add new sentence, with start:"+sent2.absStart()+" end:"+sent2.absEnd());
				}
				
				//delete current sent
				this.boundary_sentence.remove(cur_pos);
				cur_pos=cur_pos+new_sent_ct;
				llen=llen+new_sent_ct-1;
			}
			else{// sentence length < MAX
				total_len=total_len+sent.str_lenth();
				cur_pos=cur_pos+1;
			}
			
		}
	}
	
	/**
	 * Filter the drug tags overlapped with each other.
	 */
	public void filter_overlapped_drug_tag(){
		if (this.drug_tags.size()==0){
			//System.out.println("empty drug tags");
			return;
		}
		else{
			//System.out.println("# tags: "+this.drug_tags.size());
			Global.Section[] secs= new Global.Section[this.drug_tags.size()];
			for (int i=0;i<this.drug_tags.size();i++){
				secs[i]=new Global.Section();
				secs[i].start=new Integer(0);
				secs[i].end=new Integer(0);
				//System.out.println("i:+"+i+" secs len: "+secs.length+" |"+this.drug_tags.get(i).startPos()+" |"+this.drug_tags.get(i).endPos()+" |"+this.drug_tags.get(i).str());
				secs[i].start=(Integer)this.drug_tags.get(i).startPos();
				secs[i].end=(Integer)this.drug_tags.get(i).endPos();
				secs[i].str=this.original_txt.substring(secs[i].start,secs[i].end);
			}
			Global.ArrayIndexComparator cmp=new Global.ArrayIndexComparator(secs);
			Integer[] indices=cmp.createIndexArray();
			Arrays.sort(indices,cmp);
			//System.out.println(Arrays.toString(indices));
		
			int pre=-1;
			for (int i=0;i<indices.length;i++){
				if(i==0){
					pre=0;
				}
				else{
					DrugTag dt1=this.drug_tags.get(indices[pre]);
					DrugTag dt2=this.drug_tags.get(indices[i]);
					if (this.over_lap(dt1,dt2)){
						if (dt2.str().length()>dt1.str().length()){
							pre=i;
						}
					}
					else{
						DrugTag ndt=new DrugTag(dt1);
						this.filtered_drug_tags.add(ndt);
						pre=i;
					}
				}
			}
			//add the last drugtag
			DrugTag dt1=this.drug_tags.get(indices[pre]);
			DrugTag ndt=new DrugTag(dt1);
			this.filtered_drug_tags.add(ndt);
			//System.out.println("\n after sort:"+ this.filtered_drug_tags.size());
			
			
		}
	}
	
	/**
	 * Judge if two section overlapped
	 */
	public boolean over_lap(DrugTag dt1,DrugTag dt2){
		boolean flag=false;
		if(dt2.startPos()<dt1.endPos()){
			flag=true;
		}
		return flag;
	}
	
	/**
	 * print the start map, used for debug
	 */
	public void print_start_map(){
		//System.out.println("\nStart Map:\n");
		Iterator it = this.start_map.entrySet().iterator();
		  while (it.hasNext()) {
		   Map.Entry entry = (Map.Entry) it.next();
		   Object key = entry.getKey();
		   Object value = entry.getValue();
		   //System.out.println("key=" + key + " value=" + value);
		  }
	}
	/**
	 * print end_map, used debug only
	 */
	public void print_end_map(){
		//System.out.println("\nEnd Map:"+this.sbd_flag);
		Iterator it = this.end_map.entrySet().iterator();
		  while (it.hasNext()) {
		   Map.Entry entry = (Map.Entry) it.next();
		   Object key = entry.getKey();
		   Object value = entry.getValue();
		   //System.out.println("key=" + key + " value=" + value);
		  }
	}
	/**
	 * debug only function
	 */
	public void print_boundary_start_map(){
		//System.out.println("\nBoundary Start Map:\n");
		Iterator it = this.boundary_start_map.entrySet().iterator();
		  while (it.hasNext()) {
		   Map.Entry entry = (Map.Entry) it.next();
		   Object key = entry.getKey();
		   Object value = entry.getValue();
		   //System.out.println("key=" + key + " value=" + value + " Token: "+this.boundary_token_vct.get(Integer.parseInt(value.toString())).str());
		  }
	}
	
	/**
	 * Add DrugTag class into vector 'drug_tags'
	 */
	public void add_drug_tag(DrugTag dt){
		this.drug_tags.add(dt);
	}
	
	/**
	 * Add the signature to Document class. The signature is generated after rule engine and parser.
	 */
	public void add_signature(Vector<DrugTag> dt){
		this.signature.add(dt);
	}
	
	public Vector<Vector<DrugTag>> signature(){
		return this.signature;
	}
	
	public String original_txt(){
		return this.original_txt;
	}
	
	public Vector<Sentence> sentence(){
		return this.sentence;
	}
	
	public Vector<Sentence> boundary_sentence(){
		return this.boundary_sentence;
	}
	
	public String norm_str(){
		if(this.sbd_flag){
			return this.boundary_norm_str;
		}
		else{
			return this.original_norm_str;
		}
			
	}
	
	public String boundary_norm_str(){
		return this.boundary_norm_str;
	
	}
	
	public void print(){
		for(int i=0;i<this.sentence.size();i++){
			//System.out.println("sentence: "+i);
			((Sentence) this.sentence.get(i)).print();
			
		}
	}
	
	
	
	public Map startMap(){
		return this.start_map;
	}
	
	public Map endMap(){
		return this.end_map;
	}
	
	public Map boundaryStartMap(){
		return this.boundary_start_map;
	}
	
	public Map boundaryEndMap(){
		return this.boundary_end_map;
	}
	
	public Vector<Token> token_vct(){
		return this.token_vct;
	}
	
	public Vector<Token> boundary_token_vct(){
		return this.boundary_token_vct;
	}
	
	public String get_str_by_token(int start,int end){
		return this.original_txt.substring(this.token_vct.get(start).startPos(),this.token_vct.get(end).endPos());
	}
	
	public String get_boundary_str_by_token(int start,int end){			
		return this.original_txt.substring(this.boundary_token_vct.get(start).startPos(),this.boundary_token_vct.get(end).endPos());		
	}
	
	/**
	 * Simply print all the drug names in Document instance
	 */
	public void print_all_drug_tag(){
		for (int i=0;i<this.drug_tags.size();i++){
			System.out.println("\nDrugTagger "+i+":");
			System.out.println("str: "+this.drug_tags.get(i).str());
			System.out.println("start pos: "+this.drug_tags.get(i).startPos());
			System.out.println("end pos: "+this.drug_tags.get(i).endPos());
			System.out.println("start token: "+this.drug_tags.get(i).start_token());
			System.out.println("end token: "+this.drug_tags.get(i).end_token());
			System.out.println("semantic type: "+this.drug_tags.get(i).semantic_tag());
			
		}
	}
	/**
	 * Print drug tags after filtering
	 */
	public void print_all_filtered_drug_tag(){
		for (int i=0;i<this.filtered_drug_tags.size();i++){
			System.out.println("\nFiltered DrugTagger "+i+":");
			System.out.println("str: -----"+this.filtered_drug_tags.get(i).str());
			System.out.println("start pos: "+this.filtered_drug_tags.get(i).startPos());
			System.out.println("end pos: "+this.filtered_drug_tags.get(i).endPos());
			System.out.println("start token: "+this.filtered_drug_tags.get(i).start_token());
			System.out.println("end token: "+this.filtered_drug_tags.get(i).end_token());
			System.out.println("semantic type: "+this.filtered_drug_tags.get(i).semantic_tag());
			
		}
	}
	
	/** Debug only function
	 * print the normaliszed string after sentence boundary, compared with the original sentences.
	 */
	public void print_boundary_str(){
		//System.out.println("\nstr after sentence boundary:\n");
		//System.out.println("# of sentences: "+this.boundary_sentence.size());
		StringBuffer sb=new StringBuffer();

		for (int i=0;i<this.boundary_sentence.size();i++){
			//System.out.println("sentence #: "+i);
			
			sb.setLength(0);
			Sentence sent=this.boundary_sentence.get(i);
			//System.out.println("start pos: "+sent.absStart()+" end pos: "+sent.absEnd());
			//System.out.println("start token index: "+sent.startTokenIndex()+" end token index: "+sent.endTokenIndex());
			//System.out.println("original:|"+this.original_txt.substring(sent.absStart(),sent.absEnd())+"|");
			for (int j=sent.startTokenIndex();j<=sent.endTokenIndex();j++){
				sb.append(this.boundary_token_vct.get(j).str()+' ');
			}
			sb.setCharAt(sb.length()-1, '|');
			//System.out.println("normed--:|"+sb.toString()+'\n');
			
		}
	}
	
	/** Debug only function
	 * Print all the tokens after sentence boundary
	 */
	public void print_all_boundary_tokens(){
		System.out.println("\nAll boundary tokens:\n");
		for(int i=0;i<this.boundary_token_vct.size();i++){
			System.out.print(this.boundary_token_vct.get(i).str()+'|');
		}
	}
	
	public Vector<DrugTag> drug_tag(){
		return this.drug_tags;
	}
	
	public Vector<DrugTag> filtered_drug_tag(){
		return this.filtered_drug_tags;
	}
	
	public void add_boundary_token(Token token){
		this.boundary_token_vct.add(token);
	}
	
	public void set_boundary_norm_str(String str){
		this.boundary_norm_str=str;
	}
	
	public void add_boundary_start_map(int key,int val){
		this.boundary_start_map.put(key, val);
	}
	
	public void add_boundary_token2start_map(int key,int val){
		this.boundary_token2start_map.put(key, val);
	}
	
	public void add_boundary_end_map(int key,int val){
		this.boundary_end_map.put(key, val);
	}
	public void add_boundary_token2end_map(int key,int val){
		this.boundary_token2end_map.put(key, val);
	}
}
