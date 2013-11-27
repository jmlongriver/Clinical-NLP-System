package org.apache.NLPTools;


public class Util {
	public static boolean is_sep(char c){
		boolean flag=false;
		if (c==' ' || c=='\n' ){
			flag=true;
		}
		return flag;
	}
	
	public static boolean is_punctuation(char c){
		boolean flag=false;
		if (c==',' || c=='?' || c=='!'|| c==';'||c=='.'){
			flag=true;
		}
		return flag;
	}

}
