
/***************************************************************************************************************************
 * This software package is developed by Center for Computational Biomedicine at UTHealth which is directed by Dr. Hua Xu. *
 * The participants of development include Hua Xu, Min Jiang, Yonghui Wu, Anushi Shah							           *
 * Version:  1.0                                                                                                           *
 * Date: 01/30/2012                                                                                                        *
 * Copyright belongs to Dr. Hua Xu , all right reserved                                                                    *
 ***************************************************************************************************************************/
 

package org.apache.medex;


import java.awt.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.javatuples.Pair;
import org.javatuples.Quintet;
/**
* This class implements a regular expression based semantic parser
*/
public class RegexParser {

	/*
	String[] s1 = {"DIN","DBN","DPN","DSCDC","DSCDF","DSCD"}; 
	String[] s2 = {"DOSE","DDF","DOSEAMT","FREQ","RUT","TOD","DISA","REFL","DRT","NEC","MDBN"};
	String[] s3 = {"DOSE","DDF","DOSEAMT","FREQ","RUT","TOD","DISA","REFL","DRT","NEC","MDBN"};
	String[] s4 = {"DIN","DBN","DPN","DSCDC","DSCDF","DSCD"};
	String[] s5 = {"DOSE","DDF","DOSEAMT","FREQ","RUT","TOD","DISA","REFL","DRT","NEC","MDBN"};
	String[] s6 = {"DIN","DBN","DPN","DSCDC","DSCDF","DSCD"};
	String[] s7 = {"DOSE","DDF","DOSEAMT","FREQ","RUT","TOD","DISA","REFL","DRT","NEC","MDBN"};
	*/
	String tag_concat = "";
	String term_concat = "";
	boolean matcher = false;
	public static int count;
	
	/**
	 * This method contains the main logic of the parser
	 * @param  sent_semterms  	signature list need to be parsed
	 * @return	parsed signature structure 	 		
	 * @see  RegexParser  
	*/
	public ArrayList<ArrayList<String>> parse(ArrayList<Pair<Quintet<String,String,Integer,Pair<Pair<String, Integer>, Integer>,Integer>, Integer>> sent_semterms)
	{
		ArrayList<ArrayList<String>> return_tag_term_list = new ArrayList<ArrayList<String>> ();
		//String pat1 = "(DIN|DBN|DPN|DSCDC|DSCDF|DSCD)(DOSEAMT|DOSE|DDF|FREQ|RUT|TOD|DISA|REFL|DRT|NEC|MDBN| )+";
		//String pat2 = "(DOSEAMT|DOSE|DDF|FREQ|RUT|TOD|DISA|REFL|DRT|NEC|MDBN| )+ (DIN|DBN|DPN|DSCDC|DSCDF|DSCD)";
		//String pat3 = "(DOSEAMT|DOSE|DDF|FREQ|RUT|TOD|DISA|REFL|DRT|NEC|MDBN| )+ (DIN|DBN|DPN|DSCDC|DSCDF|DSCD) (DOSEAMT|DOSE|DDF|FREQ|RUT|TOD|DISA|REFL|DRT|NEC|MDBN| )+";
		String pat1a = "((DIN|DBN|DPN|DSCDC|DSCDF|DSCD)(DOSEAMT|DOSE|DDF|FREQ|RUT|TOD|DISA|REFL|DRT|NEC|MDBN| )+)|((DOSEAMT|DOSE|DDF|FREQ|RUT|TOD|DISA|REFL|DRT|NEC|MDBN| )+ (DIN|DBN|DPN|DSCDC|DSCDF|DSCD))|((DOSEAMT|DOSE|DDF|FREQ|RUT|TOD|DISA|REFL|DRT|NEC|MDBN| )+ (DIN|DBN|DPN|DSCDC|DSCDF|DSCD) (DOSEAMT|DOSE|DDF|FREQ|RUT|TOD|DISA|REFL|DRT|NEC|MDBN| )+)";
		
		//String test1 = "DOSE RUT DBN DOSE FREQ DIN";
		
		//String pata = "(DOSE|DDF|DOSEAMT|FREQ|RUT|TOD|DISA|REFL|DRT|NEC|MDBN)";
		//String patb = "^(every|q|qdaily) (m|mon|t|tue|tues|w|wed|r|thu|thur|f|fri|sat|sun|monday|tuesday|wednesday|thursday|friday|saturday|sunday|,| |and)+( |$)";
		//String test2 = "every mon tue wed";
		
		ArrayList<Pair<Quintet<String,String,Integer,Pair<Pair<String, Integer>, Integer>,Integer>, Integer>> p = sent_semterms;
		ArrayList<String> tag_list = new ArrayList<String>();
		ArrayList<String> term_list = new ArrayList<String>();
		ArrayList<Pair<String,String>> tag_term_pairs = new ArrayList<Pair<String,String>>();
		
		for(int i =0 ; i< p.size(); i++)
		{
			Pair<Quintet<String,String,Integer,Pair<Pair<String, Integer>, Integer>,Integer>, Integer> p1 = p.get(i);
			Quintet<String,String,Integer,Pair<Pair<String, Integer>, Integer>,Integer> q = p1.getValue0();
			String tag = q.getValue1();
			String term = q.getValue0();
			tag_list.add(tag);
			term_list.add(term);
			tag_term_pairs.add(Pair.with(tag,term));
		}
		
		for(int i =0 ; i< tag_list.size(); i++)
		{
			tag_concat = tag_concat + " " +tag_list.get(i);
		}
		
		//System.out.println("tag_concat"+tag_concat.trim());
		tag_concat = tag_concat.trim();
		
		Pattern pattern1 = Pattern.compile(pat1a);
	    Matcher matcher1 = pattern1.matcher(tag_concat);
	    
	    //Pattern pattern2 = Pattern.compile(pat2);
	    //Matcher matcher2 = pattern2.matcher(tag_concat);
	    
	    //Pattern pattern3 = Pattern.compile(pat3);
	    //Matcher matcher3 = pattern3.matcher(tag_concat);
	    
	    if(matcher == false)
    	{
	    	while (matcher1.find()) 
	    	{
		        // Get the matching string
	    		matcher = true;
	    		//System.out.println("matcher 1");
		        String match = matcher1.group();
		        //System.out.println("match:"+match);
		        return_tag_term_list.add(get_tag_term_pairs(match,tag_term_pairs));
		     }
    	}
	    
	    //System.out.println("return_tag_term_list --"+return_tag_term_list);
	   return return_tag_term_list;
	}
	
	
	
	/**
	 * 
	 * @param match	i.e. "RUT DBN"
	 * @param tag_term_pairs	"[[RUT, Pt], [DBN, Asacol], [TOD, 9am], [TOD, morning]]"
	 * @return	"RUT FFF Pt	DBN FFF Asacol"	
	 */
	public static ArrayList<String>  get_tag_term_pairs(String match,ArrayList<Pair<String,String>> tag_term_pairs)
	{
		
		ArrayList<String> return_tag_term_list = new ArrayList<String>();
		//return_tag_term_list.add("abc");
		
		String[] matchtag_array = match.split(" ");
		ArrayList<String> matchtag_list = new ArrayList<String>();
		ArrayList<Integer> tag_pos = new ArrayList<Integer>();
		
		for(int i = 0; i< matchtag_array.length; i++)
		{
			if(!matchtag_array[i].equals(""))
				matchtag_list.add(matchtag_array[i]);
		}
		//System.out.println("** matchtag_list ** "+matchtag_list);
        int match_size = matchtag_array.length;
        
		for(int i =0 ; i< tag_term_pairs.size(); i++)
        {
        	
        	ArrayList<Pair<String,String>> final_tag_term_list = new ArrayList<Pair<String,String>> ();
        	//for(int j=0; j < matchtag_list.size(); j++)
        	//{
        		//System.out.println("!! matchtag_list.get(j)"+matchtag_list.get(j));
        		if(tag_term_pairs.get(i).getValue0().equals(matchtag_list.get(0)))
        		{
        			
        			//System.out.println("---"+i+" "+tag_term_pairs.get(i));
        			tag_pos.add(i);
        		}
        		
        }
		
		//System.out.println("** tag_pos ** "+tag_pos);
		
		for(int i =0 ; i< tag_pos.size(); i++)
        {
			//System.out.println("---"+i+" "+tag_pos.get(i));
			int start = tag_pos.get(i);
			int match_len = 0;
			String tag_term = "";
			ArrayList<String> temp_tag_term_list = new ArrayList<String> ();
			if((start + matchtag_list.size()) <= tag_term_pairs.size())
			{
				//System.out.println("****************");
				int loop_ctr = 0;
				while(loop_ctr < matchtag_list.size())
				{
					//System.out.println("start "+start+" "+tag_term_pairs.get(start));
					if(matchtag_list.get(loop_ctr).equals(tag_term_pairs.get(start).getValue0()))
					{
						//System.out.println("start "+start+" "+tag_term_pairs.get(start));
						String tag = tag_term_pairs.get(start).getValue0();
						String term = tag_term_pairs.get(start).getValue1();
						
						tag_term = tag+" "+"FFF"+" "+term;
						temp_tag_term_list.add(tag_term);
						
						match_len++;
					}
					start++;
					loop_ctr++;
				}
				
		        
			}// end of if
			
			if(match_len == matchtag_list.size())
			{
				//System.out.println("match_len :"+match_len+"--"+temp_tag_term_list);
				String str = "";
				for(int j = 0;j < temp_tag_term_list.size(); j++)
				{
					str = str + temp_tag_term_list.get(j) +"\t";
				}
				return_tag_term_list.add(str);
			}
			
			
			
        }
        	
		
		return return_tag_term_list;
        //System.out.println(return_tag_term_list);
	}
}
	
/*
regrammar = r"""
S:  {<DG>+}
DG: {(<DIN|DBN|DPN|DSCDC|DSCDF|DSCD><DOSE|DDF|DOSEAMT|FREQ|RUT|TOD|DISA|REFL|DRT|NEC|MDBN>*)|(<DOSE|DDF|DOSEAMT|FREQ|RUT|TOD|DISA|REFL|DRT|NEC|MDBN>*<DIN|DBN|DPN|DSCDC|DSCDF|DSCD>)}
DG: {<DOSE|DDF|DOSEAMT|FREQ|RUT|TOD|DISA|REFL|DRT|NEC|MDBN>*<DIN|DBN|DPN|DSCDC|DSCDF|DSCD><DOSE|DDF|DOSEAMT|FREQ|RUT|TOD|DISA|REFL|DRT|NEC|MDBN>*}
"""
*/
