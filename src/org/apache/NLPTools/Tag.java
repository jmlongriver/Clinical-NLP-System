package org.apache.NLPTools;

public class Tag {
	public Tag(int spos,int epos,String str, String type){
		this.absStartPos=spos;
		this.absEndPos=epos;
		this.str=str;
		this.type = type;
	}
	
	
	
	private int absStartPos=-1;
	private int absEndPos=-1;
	private String str="";
	private String type = "";
	
	
	//public functions
	public int startPos(){
		return absStartPos;
	}
	public int endPos(){
		return absEndPos;
	}
	public String str(){
		return this.str;
	}
	public String type(){
		return this.type;
	}
	
	
}
