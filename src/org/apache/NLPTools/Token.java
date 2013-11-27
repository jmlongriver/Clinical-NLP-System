package org.apache.NLPTools;

public class Token extends TextSection implements Global{
	public Token(int spos,int epos,TextSectionType type, String str){
		super(spos,epos,type,str);
	}
	
	public Token(Token token){
		super(token.startPos(),token.endPos(),token.type(),token.str());
	}
	
}
