package org.apache.NLPTools.CFGparser;
/*
 * represent an item in chart entry
 * S -> S + M
 * S.sign = "S"; S.isTerm = False
 */
public class EntryItem {
	protected String sign;
	//protected Boolean isTerm;
	
	/*protected String[] POSTAGS = {"CC","CD","DT","EX ","FW","IN", 
	                              "JJ","JJR","JJS","LS","MD","NN",
	                              "NNS","NNP","NNPS","PDT","POS",
	                              "PRP","PRP$","RB","RBR","RBS",
	                              "RP","SYM","TO","UH","VB","VBD",
	                              "VBG","VBP","VBZ","WDT","WP",
	                              "WP$","WRB"};*/
	                             
	                           
	public boolean equals(Object o){
		//System.out.println("call equals");
		if(o instanceof EntryItem){
			//System.out.println(this.getSign());
			//System.out.println(((EntryItem)o).getSign());
			
			if(sign.equals(((EntryItem)o).getSign())){
				//System.out.println(this.isterm());
				//System.out.println(((EntryItem)o).isterm());
				//if (this.isterm() == ((EntryItem)o).isterm()){
					//System.out.println("we are equal");
				return true;
				//}
			}
			
		}	
		return false;
	}
	public int hashCode(){
		//System.out.println("call hashCode");
		//System.out.println(isTerm.hashCode() + sign.hashCode());
		return 7;
		//return isTerm.hashCode() + sign.hashCode();
	}
	public String getSign(){
		return sign;
	}
	public void setSign(String sign){
		this.sign = sign;
	}
	/*
	public void setTerm(Boolean isTerm){
		this.isTerm = isTerm;
	}
	public boolean isterm(){
		return this.isTerm;
	}*/
	
}
