package org.apache.NLPTools;

/**
 * 
 * Abstract class for Token, Tagger
 * 
 * @author yonghuiwu
 *
 */

public abstract class TextSection implements Global{
	/**
	 * Constructor
	 */
	public TextSection(){
		
	}
	public TextSection(int spos,int epos,TextSectionType type,String str){
		this.absStartPos=spos;
		this.absEndPos=epos;
		this.type=type;
		this.str=str;
	}

	//members
	
	private int absStartPos=-1;
	private int absEndPos=-1;
	private String str="";
	private TextSectionType type=TextSectionType.NA;
	
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
	
	public TextSectionType type(){
		return this.type;
	}
	
	
	//abstract functions
	//abstract void merge();

}
