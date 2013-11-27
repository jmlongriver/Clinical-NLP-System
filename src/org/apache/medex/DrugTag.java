package org.apache.medex;

import org.apache.NLPTools.Global;
import org.apache.NLPTools.TextSection;
import org.apache.NLPTools.Global.TextSectionType;

public class DrugTag extends TextSection implements Global {
	public DrugTag(int spos,int epos,TextSectionType type, String str){
		super(spos,epos,type,str);
	}
	
	public DrugTag(int spos,int epos,TextSectionType type, String str,int start_token,int end_token,String stype){
		super(spos,epos,type,str);
		this.start_token=start_token;
		this.end_token=end_token;
		this.semantic_tag=stype;
	}
	public DrugTag(DrugTag dt){ 
		super(dt.startPos(),dt.endPos(),dt.type(),dt.str());
		this.start_token=dt.start_token;
		this.end_token=dt.end_token;
		this.semantic_tag=dt.semantic_tag;
	}
	
	//new member specific for DrugTagger
	private int start_token=-1;
	private int end_token=-1;
	private String semantic_tag;
	
	public int start_token(){
		return this.start_token;
	}
	
	public int end_token(){
		return this.end_token;
	}
	
	public String semantic_tag(){
		return this.semantic_tag;
	}

}
