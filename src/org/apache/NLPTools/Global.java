package org.apache.NLPTools;

import java.util.Comparator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for some global settings and parameter values. Any class need this global settings only need to implement this interface
 */

public interface Global {
	//Global configure parameters
	static String input_dir="";
	static String output_dir="";
	static boolean sentence_boundary=true;
	
	
	enum TextSectionType{NA,TOKEN,DRUG,DOSE,FREQUENCY,ROUTE,NECCESSITY,STRENGTH,BRAND,DURATION,FORM};
	enum SuffixArrayMode{ALL,WORD};
	enum SuffixArrayCaseMode{SENSITIVE,NON_SENSITIVE};
	final int MAX_LENGTH_LINE=300;
	
	public class SuffixArrayNode{
		public int father;
		public Vector<Integer> son;
		public int start;
		public int end;
		public Vector<Integer> pos;
		
		public SuffixArrayNode(){
			this.father=-1;
			this.son=new Vector<Integer>();
			this.start=-1;
			this.end=-1;
			this.pos=new Vector<Integer>();
		}
	}
	
	public class SuffixArrayResult{
		public int start_token=-1;
		public int end_token=-1;
		public int start_pos=-1;
		public int end_pos=-1;
		public String semantic_type="";
	}
	
	//sort vector, get indices
	public class Section{
		public Integer start=new Integer(0);
		public Integer end=new Integer(0);
		String str="";
	}
	/**
	 * Class for ranking a list of (start, end) pairs.
	 * Return a ranked position, just like the 'sort' function in python
	 */
	public class ArrayIndexComparator implements Comparator{
		private Section[] secs;
		public ArrayIndexComparator(Section[] secs){
			this.secs=secs;
		}
		
		public Integer[] createIndexArray(){
			Integer[] indices=new Integer[this.secs.length];
			for (int i=0;i<indices.length;i++){
				indices[i]=i;
			}
			return indices;
		}
		
	    public int compare(Object o1,Object o2){
	    	Integer index1=(Integer)o1;
	    	Integer index2=(Integer)o2;
	    	
	    	Integer start1=new Integer(secs[index1].start);
	    	Integer end1=new Integer(secs[index1].end);
	    	
	    	Integer start2=new Integer(secs[index2].start);
	    	Integer end2=new Integer(secs[index2].end);
	    	
	    	int flag=start1.compareTo(start2);
	    	if (flag==0){
	    		flag=end1.compareTo(end2);
	    	}
			return flag;
			
		}
		
	}
	/**
	 * This class should be used with the privious one
	 */
	public class CharacterComparator implements Comparator{
		private Vector<Character> vct;
		public CharacterComparator(Vector<Character> vct){
			this.vct=vct;
		}
		
		public Integer[] createIndexArray(){
			Integer[] indices=new Integer[this.vct.size()];
			for (int i=0;i<indices.length;i++){
				indices[i]=i;
			}
			return indices;
		}
		
	    public int compare(Object o1,Object o2){
	    	return this.vct.get((Integer) o1).compareTo(this.vct.get((Integer) o2));
	    	
			
		}
		
	}
	
	//some useful functions can be used global
	
	public class Util {
		//define separatable char
		public static boolean is_sep(char c){
			boolean flag=false;
			if (c==' ' || c=='\n' ){
				flag=true;
			}
			return flag;
		}
		
		//define seperatable punctuation, '.' is tricky, do not handle here
		public static boolean is_punctuation(char c){
			boolean flag=false;
			if (c == '\t' || c==',' || c=='?' || c=='!'|| c==';' || c==':'|| c=='^' || c=='`' || c=='"' || c=='\'' || c=='/' || c=='\\'|| c=='%' || c=='#' || c=='-' || c=='+' || c=='_' || c=='&' || c=='*' || c=='@' || c=='~'){
				flag=true;
			}
			return flag;
		}
		/**
		 * Define various of symbols like braces
		 */
		public static boolean is_braces(char c){
			boolean flag=false;
			if(c=='(' || c==')' || c=='[' || c==']' || c=='{' || c=='}' || c=='<' || c=='>'){
				flag=true;
			}
			return flag;
		}
		
		/**
		 * Jude whether the token composed of dot and num 
		 */
		public static int dot_num(String word){
			int result=0;
			int pos=word.indexOf('.');
			while (pos>=0){
				result=result+1;
				pos=word.indexOf('.',pos+1);
			}
			return result;
		}
		
		/**
		 * Return whether the input word is digit
		 */
		public static boolean is_digit(String word){
			Pattern pattern = Pattern.compile("\\d+$"); 
			Matcher matcher = pattern.matcher(word);
			boolean b= matcher.matches();
			return b;
		}
		/**
		 * Return whether the input word is '.'+word
		 */
		public static boolean is_dot_word(String word){
			Pattern pattern = Pattern.compile("\\.[\\D]+$"); 
			Matcher matcher = pattern.matcher(word);
			boolean b= matcher.matches();
			return b;
		}
		
		/**
		 * Return whether the input word num+unit
		 */
		public static boolean is_unit(String word){
			Pattern pattern = Pattern.compile("[+-]?\\d*[\\.]?\\d+(mg|lb|lbs|kg|mm|cm|m|doz|am|pm|mph|oz|ml|l|mb|h|hr|hrs|hour|hours|min|minutes|day|days|week|weeks|month|months|year|years|unit|units|tab|tabs|tablet|tablets|puff|puffs|drop|drops|pill|pills|cap|caps|capsule|capsules|spray|sprays)$",Pattern.CASE_INSENSITIVE); 
			Matcher matcher = pattern.matcher(word);
			boolean b= matcher.matches();
			return b;
		}
		
		/**
		 * Define different format of lis headers.
		 */
		public static boolean is_list(String str){
			//System.out.println(str);
			//#1. Percocet 5/325 one p.o.
			Pattern pattern = Pattern.compile("^\\d+\\.$|^\\-"); 
			Matcher matcher = pattern.matcher(str);
			boolean b= matcher.matches();
			return b;
		}
		
		/**
		 * Judge whether the current symbol start with an uppercase -- a sign of sentence start
		 */
		public static boolean is_sentence_start(String word){
			boolean flag=false;
			if (Character.isUpperCase(word.charAt(0))){
				flag=true;
			}
			return flag;
		}

	}
}



