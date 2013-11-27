package org.apache.medex.semantic_rules;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.javatuples.Pair;
import org.javatuples.Quartet;

/**
 * This class implements a simple rule-based semantic transformation engine
 */
public class Tag_rule_engine {

	ArrayList<Quartet<String,String,Integer,Pair<Pair<String, Integer>, Integer> >> tags;
	String section;
	
	public ArrayList<Quartet<String,String,Integer,Pair<Pair<String, Integer>, Integer> >> get_tag_result(){
		return tags;
	}
	
	public Tag_rule_engine(ArrayList<Quartet<String,String,Integer,Pair<Pair<String, Integer>, Integer> >> all_tags, String section)
	{
		 this.tags = all_tags;
		 this.section = section;
	}
	
	private String getPrevNTag(int i, int N){
		if(i-N>=0){
			return tags.get(i-N).getValue1();
		}
		else{
			return "ERROR";
		}
	}
	
	private String getPrevNToken(int i, int N){
		if(i>0){
			return tags.get(i-N).getValue1();
		}
		else{
			return "ERROR";
		}
	}
	
	private String getNextNTag(int i, int N){
		if(i+N<tags.size()){
			return tags.get(i+N).getValue1();
		}
		else{
			return "ERROR";
		}
	}
	
	private String getNextNToken(int i, int N){
		if(i+N<tags.size()){
			return tags.get(i+N).getValue0();
		}
		else{
			return "ERROR";
		}
	}
	private int setTag(int i, String newTag){
		if(i<0 || i>=tags.size()){
			return -1;
		}
		Quartet<String,String,Integer,Pair<Pair<String, Integer>, Integer> > temp = Quartet.with(tags.get(i).getValue0(), newTag, tags.get(i).getValue2(), tags.get(i).getValue3());
    	tags.set(i,temp);
    	return 1;
	}
	
	private int setToken(int i, String newToken){
		if(i<0 || i>=tags.size()){
			return -1;
		}
		Quartet<String,String,Integer,Pair<Pair<String, Integer>, Integer> > temp = Quartet.with(newToken, tags.get(i).getValue1(), tags.get(i).getValue2(), tags.get(i).getValue3());
    	tags.set(i,temp);
    	return 1;
	}
	
	
	private String getTag(int i){
		return tags.get(i).getValue1();
	}
	
	private String getToken(int i){
		return tags.get(i).getValue0();
	}
	
	public void disambiguation(){
		//System.out.println("enter_disam");
		 int index = 0;
		 while(index < tags.size())
	     {
	        	//System.out.println("index"+"--"+index);
	        	String token = tags.get(index).getValue0();
	        	String tag = tags.get(index).getValue1();
	        	int off1 = tags.get(index).getValue2();
	        	Pair<Pair<String,Integer>,Integer>  off2 = tags.get(index).getValue3();
	        	
	        	
	        	Pattern r = Pattern.compile("-");
	        	Matcher m = r.matcher(tag);
	        	
	        	//System.out.println(tag+"--"+token);
	        	
	        	/// ****************  NEED to separate transformation rules as they will be executed only if tag has "-" *************************//
	        	if(getToken(index).equals("pt") && section.equals("RUT")){
        			setTag(index, "RUT");
        		}
	        	
	        	
	        	//System.out.println(getTag(index));
	        	if(m.find())
	        	{	
	        		//System.out.println("find_disam");
	        		
	        		//if previous is NUM, DDF-DOSEUNIT --> DOSEUNIT
	        		if(getTag(index).equals("DDF-DOSEUNIT")){
	        			//System.out.println("prevtag: " +getPrevNTag(index, 1));
	        			if(getPrevNTag(index, 1).equals("NUM")){  				
	        				setTag(index, "DOSEUNIT");
	        			}
	        			else
	        				if(!getPrevNTag(index, 1).equals("NUM") && !getPrevNTag(index, 1).equals("ERROR")){
	        					setTag(index, "DDF");
	        				}
	        		}
	        		
	        		//if previous is NUM, TOD-UNIT --> UNIT, such as "cc"
	        		else
	        		if(getTag(index).equals("TOD-UNIT")){
	        			if(getTag(index).equals("NUM")){
	        				setTag(index, "UNIT");
	        			}
	        			else
	        				if(!getTag(index).equals("NUM")){
	        					setTag(index, "TOD");
	        				}
	        		}

	        		
	        		// if it is DOSEUNIT and ROUTE: neb, num + neb --> DOSE,
	        		else
	        		if(getTag(index).equals("DOSEUNIT-RUT")){
	        			if(getPrevNTag(index, 1).equals("NUM")){
	        				setTag(index, "DOSEUNIT");
	        			}
	        			else
	        				if(!getTag(index).equals("NUM")){
	        					setTag(index, "RUT");
	        				}
	        		}
	        		
	        		else{
	        			String[] tags = getTag(index).split("-");
	        		    setTag(index, tags[0]);
	        		}

	        	}
	        	index += 1;
	     }
	}
	
	public void transform_1(){
		ArrayList<Quartet<String,String,Integer,Pair<Pair<String, Integer>, Integer> >> new_tags = new ArrayList<Quartet<String,String,Integer,Pair<Pair<String, Integer>, Integer> >>();
		int index = 0;
		
		while(index < tags.size())
	    {
			
			
			
			String token = getToken(index);
			String tag = getTag(index);
			//System.out.println("token:" +token);
			//System.out.println("tag:"+tag);
			
			//System.out.println(getNextNTag(index, 1));
			//System.out.println(getNextNTag(index, 2));
			//System.out.println(getNextNTag(index, 3));
			//System.out.println(getNextNTag(index, 4));
			// compositional Dose: NUM + UNIT + '/' + NUM + UNIT -> DOSE
			if(tag.equals("NUM") && getNextNTag(index, 1).equals("UNIT") && getNextNToken(index, 2).equals("/") && getNextNTag(index, 3).equals("NUM") && getNextNTag(index, 4).equals("UNIT")){
				tag = "DOSE";
				token = token + " "+getNextNToken(index, 1) + " " +getNextNToken(index, 2) + " " +getNextNToken(index, 3) + " " + getNextNToken(index, 4);
				
				index += 4;
			}
			
			// e.g.    35 in the morning and 25 at night
			else
				if(tag.equals("NUM") && 
				   (getNextNTag(index, 1).equals("FREQ") || getNextNTag(index, 1).equals("TOD") || getNextNTag(index, 1).equals("RUT")) &&
				   (getNextNTag(index, 2).equals("and") || getNextNTag(index, 2).equals(".")) &&
				   getNextNTag(index, 3).equals("NUM") &&
				   (getNextNTag(index, 4).equals("FREQ") || getNextNTag(index, 4).equals("TOD") || getNextNTag(index, 4).equals("RUT"))
				   )
				{
					tag = "DOSE";
				}
			
			else
				if(tag.equals("NUM") && 
				   (getPrevNTag(index, 3).equals("FREQ") || getPrevNTag(index, 1).equals("TOD") || getPrevNTag(index, 1).equals("RUT")) &&
				   (getPrevNTag(index, 1).equals("and") || getPrevNTag(index, 1).equals(".")) &&
				   getNextNTag(index, 3).equals("NUM") &&
				   (getNextNTag(index, 1).equals("FREQ") || getNextNTag(index, 1).equals("TOD") || getNextNTag(index, 1).equals("RUT"))
				   )
				{
					tag = "DOSE";
				}
			
			// DOSE : NUM + UNIT -> DOSE
			else
				if(tag.equals("NUM") && 
				   (getNextNTag(index, 1).equals("UNIT"))
				   )
				{
					tag = "DOSE";
					token = token  + getNextNToken(index, 1);
					index += 1;
				}				
			 
			// DOSE : NUM + '.' + UNIT -> DOSE
			else
				if(tag.equals("NUM") && 
				   (getNextNToken(index, 1).equals(".")) &&
				   (getNextNTag(index, 2).equals("UNIT"))
				   )
				{
					tag = "DOSE";
					token = token  + getNextNToken(index, 1) + getNextNToken(index, 2);
					index += 2;
				}
			
			//DOSE : NUM -> DOSE if it is after a DIN/DBN/DSCDF and not followed by UNIT/DOSEUNIT/TUNIT, and it is not "one", "two", or "1", "2"
			else
				if(tag.equals("NUM") && 
				   (getPrevNTag(index, 1).equals("DIN") || getPrevNTag(index, 1).equals("DPN") || getPrevNTag(index, 1).equals("DBN")) &&
				   (!getNextNTag(index, 1).equals("UNIT") && !getNextNTag(index, 1).equals("DOSEUNIT") && !getNextNTag(index, 1).equals("TUNIT")) &&
				   (!token.equals("one to two") && !token.equals("one") && !token.equals("two") && !token.equals("1 - 2") && !token.equals("1") &&!token.equals("2"))
				   )
				{
					tag = "DOSE";
				}
			
			
			// DOSEAMT : NUM + DOSEUNIT -> DOSEAMT
			else
				if(tag.equals("NUM") && 
				   (getNextNTag(index, 1).equals("DOSEUNIT"))
				   )
				{
					tag = "DOSEAMT";
					token = token + " "+ getNextNToken(index, 1);
					index += 1;
				}
			
			//DOSEAMT : '(' + NUM + ')' + DOSEUNIT -> DOSEAMT 
			else
				if(tag.equals("(") &&
				   getNextNTag(index, 1).equals("NUM") &&
				   getNextNToken(index, 2).equals(")") &&
				   getNextNTag(index, 3).equals("DOSEUNIT")
				   )
				{
					tag = "DOSEAMT";
					token = token +getNextNToken(index, 1) + getNextNToken(index, 2) + " "+  getNextNToken(index, 3);
					index += 3;
				}
			
			//DRT : NUM + TUNIT -> DRT
			else
				if(tag.equals("NUM") && 
				   (getNextNTag(index, 1).equals("TUNIT"))
				   )
				{
					tag = "DRT";
					token = token + " "+ getNextNToken(index, 1);
					index += 1;
				}
			
			//DRT : NUM + "-"+TUNIT -> DRT
			else
				if(tag.equals("NUM") && 
				   (getNextNTag(index, 1).equals("TUNIT"))
				   )
				{
					tag = "DRT";
					token = token  + getNextNToken(index, 1) + getNextNToken(index, 2);
					index += 2;
				}
			
			//dispense amount : (NOT DIN/DBN/RVS) # + NUM -> DISA
			else
				if(token.equals("#") && 
				   getNextNTag(index, 1).equals("NUM") &&
				   (!getPrevNTag(index, 1).equals("DIN") && !getPrevNTag(index, 1).equals("DBN") && !getPrevNTag(index, 1).equals("DPN")) &&
				   (!getPrevNToken(index, 1).equals("rvs") && !getPrevNToken(index, 1).equals("regen")&& !getPrevNToken(index, 1).equals("c"))
				   )
				{
					tag = "DISA";
					token =  token + getNextNToken(index, 1); 
					index += 1;
				}
			
			//dispense amount : NUM in number -> DISA
			else
				if(tag.equals("NUM") && 
				   (getNextNToken(index, 1).equals("in")) &&
				   (getNextNToken(index, 2).equals("number"))
				   )
				{
					tag = "DISA";
					
				
				}
			
			// dispense amount : Amount : NUM  -> DISA
			else
				if(tag.equals("NUM") && 
				   (getPrevNToken(index, 1).equals(":")) &&
				   (getPrevNToken(index, 2).equals("amount"))
				   )
				{
					tag = "DISA";
				}
			
			else
				if(token.equals("na") && 
				   (getPrevNToken(index, 1).equals("spray"))
				   )
				{
					tag = "RUT";
				}
			
			else
				if((tag.equals("DIN") || tag.equals("DPN") || tag.equals("DBN") || tag.equals("DSCD") ||tag.equals("DSCDF") || tag.equals("DSCDC") ) && 
				   getNextNToken(index, 1).equals("w") &&
				   getNextNToken(index, 2).equals("/") &&
				   (getNextNTag(index, 3).equals("DIN") || getNextNTag(index, 3).equals("DPN") || getNextNTag(index, 3).equals("DBN") || getNextNTag(index, 3).equals("DSCD") ||getNextNTag(index, 3).equals("DSCDF") || getNextNTag(index, 3).equals("DSCDC") )
				   )
				{
					tag = "DIN";
					token = token + " " + getNextNToken(index, 1) + getNextNToken(index, 2) +  getNextNToken(index, 3);
					index += 3;
				}
				
			//REfill : no/TK + refills/TK -> no/REFL; 2 refills; without refill
			else
				if((token.equals("0") || token.equals("1") || token.equals("2") || token.equals("3") ||token.equals("4") || token.equals("5") || token.equals("no") || token.equals("without") ) && 
				   (getNextNToken(index, 1).equals("refill") || getNextNToken(index, 1).equals("refill") || getNextNToken(index, 1).equals("rf's") || getNextNToken(index, 1).equals("rf") )
				   )
				{
					tag = "REFL";
				}
			
			//refill : refill/TK :/TK 2/NUM -> refill/TK :/TK 2/REFL 
			else
				if((tag.equals("NUM") && 
				    getPrevNToken(index, 1).equals(":") &&
				    getPrevNToken(index, 2).equals("refill") || getNextNToken(index, 1).equals("refills")  )
				   )
				{
					tag = "REFL";
				}
			
			//RUT : iv/RUT first now infusion/RUT -> iv first now infusion/RUT
			else
				if( tag.equals("RUT") && 
				    getNextNToken(index, 1).equals("1") &&
				    getNextNToken(index, 2).equals("st") &&
				    getNextNToken(index, 3).equals("now") &&
				    getNextNTag(index, 4).equals("RUT")
				   )
				{
					tag = "RUT";
					token = token + " "+getNextNToken(index, 1) + " " +getNextNToken(index, 2) + " " +getNextNToken(index, 3) + " " + getNextNToken(index, 4);
					
					index += 4;
				}
			
			else
				if( tag.equals("RUT") && 
				    getNextNToken(index, 1).equals("first") &&
				    getNextNToken(index, 2).equals("now") &&
				    getNextNTag(index, 3).equals("RUT")
				   )
				{
					tag = "RUT";
					token = token + " "+getNextNToken(index, 1) + " " +getNextNToken(index, 2) + " " +getNextNToken(index, 3) ;
					index += 3;
				}
	        
			else
				if( tag.equals("RUT") && 
				    getNextNToken(index, 1).equals("now") &&
				    getNextNTag(index, 2).equals("RUT")
				   )
				{
					tag = "RUT";
					token = token + " "+getNextNToken(index, 1) + " " +getNextNToken(index, 2);
					index += 2;
				}
			else
				if( tag.equals("RUT") && 
				    getNextNToken(index, 1).equals("/") &&
				    getNextNTag(index, 2).equals("RUT")
				   )
				{
					tag = "RUT";
					token = token + " "+getNextNToken(index, 1) + " " +getNextNToken(index, 2);
					index += 2;
				}
	        
			
			
		    new_tags.add(Quartet.with(token, tag, tags.get(index).getValue2(), tags.get(index).getValue3()));
			index += 1;
	    }
		this.tags = new_tags;
	}
	public void printTagToken(){
		int index = 0;
		while(index < tags.size())
	    {
			String token = getToken(index);
			String tag = getTag(index);
			System.out.println("token:"+ token);
			System.out.println("tag:"+ tag);
			index++;
	    }
	}
	public void transform_2(){
		ArrayList<Quartet<String,String,Integer,Pair<Pair<String, Integer>, Integer> >> new_tags = new ArrayList<Quartet<String,String,Integer,Pair<Pair<String, Integer>, Integer> >>();
		int index = 0;
		
		while(index < tags.size())
	    {
			String token = getToken(index);
			String tag = getTag(index);
			// compositional Dose: NUM + UNIT + '/' + NUM + UNIT -> DOSE
			if(tag.equals("NUM") || tag.equals("UNIT") && tag.equals("TUNIT") && tag.equals("DOSEUNIT")){
				if(tag.equals("NUM") && 
				   (getPrevNTag(index, 1).equals("DBN") || getPrevNTag(index, 1).equals("DIN") || getPrevNTag(index, 1).equals("DPN") || getPrevNTag(index, 1).equals("DOSE") || getPrevNTag(index, 1).equals("UNIT") || getPrevNToken(index, 1).equals(",")) &&
				   (getNextNTag(index, 1).equals("FREQ") || getNextNTag(index, 1).equals("RUT")))
				   {
						tag = "DOSEAMT";
				   }
				
				else{
					tag = "TK";
				}
			}
			
			//combined dose :Dose + " to " + Dose -> Dose , e.g., "0.25 mg to 0.5 mg"
			else
				if(tag.equals("DOSE") && 
				   (getNextNToken(index,1).equals("to") || getNextNToken(index,1).equals("-") || getNextNToken(index,1).equals("~")) &&
				   (getNextNTag(index, 2).equals("DOSE"))
				   )
				{
					tag = "DOSE";
					token = token  + getNextNToken(index, 1) + getNextNToken(index, 2);
					index += 2;
				}
			
			//combined drug name : DIN + ":" or "/" + DIN/DBN -> DIN; DIN + w + / + DIN/DBN - RUN IT ONE MORE TIME TO solve "senna/docusate: senokot s 2 tab po bid"
			else
				if((tag.equals("DIN") || tag.equals("DPN") || tag.equals("DBN") || tag.equals("DSCD") ||tag.equals("DSCDF") || tag.equals("DSCDC") ) && 
				   (getNextNToken(index, 1).equals("w") || getNextNToken(index, 1).equals("/") || getNextNToken(index, 1).equals(":") || getNextNToken(index, 1).equals("+") ||getNextNToken(index, 1).equals("with")) &&
				   (getNextNTag(index, 2).equals("DIN") || getNextNTag(index, 3).equals("DPN") || getNextNTag(index, 3).equals("DBN") || getNextNTag(index, 3).equals("DSCD") ||getNextNTag(index, 3).equals("DSCDF") || getNextNTag(index, 3).equals("DSCDC") )
				   )
				{
					tag = "DIN";
					token = token + " " + getNextNToken(index, 1) + " "+getNextNToken(index, 2);
					index += 2;
				}
			
			// combined drug name : DIN + "(" + DIN/DBN + "and" + DIN/DBN + ")"-> DIN; example: COMBIVENT ( IPRATROPIUM AND ALBUTEROL SULFATE ) 
			else
				if((tag.equals("DIN") || tag.equals("DPN") || tag.equals("DBN") ) && 
				   (getNextNToken(index, 1).equals("(")) &&
				   (getNextNTag(index, 1).equals("DIN") || getNextNTag(index, 1).equals("DBN") || getNextNToken(index, 1).equals("DPN")) &&
				   (getNextNToken(index, 3).equals("and") || getNextNTag(index, 3).equals(",")) &&
				   (getNextNTag(index, 4).equals("DIN") || getNextNTag(index, 4).equals("DPN") || getNextNTag(index, 4).equals("DBN")) &&
				   (getNextNToken(index, 5).equals(")"))
				   )
				{
					tag = "DIN";
					token = token + " " + getNextNToken(index, 1) + " "+getNextNToken(index, 2) + " " + getNextNToken(index, 3) + " "+getNextNToken(index, 4) +" "+getNextNToken(index, 5);
					index += 5;
				}
			

	        // combined drug name : DIN + "(" + DIN/DBN + ")"-> DIN; example: COMBIVENT ( IPRATROPIUM AND ALBUTEROL SULFATE ) 
			else
				if((tag.equals("DIN") || tag.equals("DPN") || tag.equals("DBN") ) && 
				   (getNextNToken(index, 1).equals("(")) &&
				   (getNextNTag(index, 2).equals("DIN") || getNextNTag(index, 2).equals("DBN") || getNextNToken(index, 2).equals("DPN")) &&
				   (getNextNToken(index, 3).equals(")"))
				   )
				{
					tag = "DIN";
					token = token + " " + getNextNToken(index, 1) + " "+getNextNToken(index, 2) + " " + getNextNToken(index, 3);
					index += 3;
				}
		
	            
	        // combined drug name : "(" + DBN + ")" -> MDBN - RUN IT ONE MORE TIME TO solve "Tacrolimus Oral capsule 1 mg (Prograf) 2 capsules"
			else
				if((tag.equals("DIN") || tag.equals("DBN") ) && 
				   (getPrevNToken(index, 1).equals("(")) &&
				   (getPrevNToken(index, 2).equals("antibiotics") || getPrevNToken(index, 2).equals("abx") || getPrevNToken(index, 2).equals("medications") || getPrevNToken(index, 2).equals("meds"))
				   )
				{
					tag = "MDBN";
				}
			
			
			new_tags.add(Quartet.with(token, tag, tags.get(index).getValue2(), tags.get(index).getValue3()));
			index += 1;
	    }
		this.tags = new_tags;
	}
}
