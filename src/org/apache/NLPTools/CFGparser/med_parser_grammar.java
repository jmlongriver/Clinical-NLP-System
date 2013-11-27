package org.apache.NLPTools.CFGparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;


public class med_parser_grammar extends Grammar {
	public static String ITEM_SEPERATOR = " ";
	public static String ENTRY_SEPERATOR = "\\|";
	public ArrayList<String> POSTAGSET;
	public String section;
	public int count_input_rule = 0;
	
	public med_parser_grammar(String GrammarFile){
		super(GrammarFile);
		POSTAGSET = new ArrayList<String>();
		POSTAGSET.add("DOSE");
		POSTAGSET.add("DOSEAMT");
		POSTAGSET.add("FREQ");
		POSTAGSET.add("RUT");
		POSTAGSET.add("DRT");
		POSTAGSET.add("MDBN");
		POSTAGSET.add("DIN");
		POSTAGSET.add("DBN");
		POSTAGSET.add("DDF");
		POSTAGSET.add("NEC");
		POSTAGSET.add("REFL");
		POSTAGSET.add("DISA");
		POSTAGSET.add("DDF");
		POSTAGSET.add("TOD");
		POSTAGSET.add("RUT");
		POSTAGSET.add("DSCDC");
		POSTAGSET.add("DSCDF");
		POSTAGSET.add("DSCD");
		POSTAGSET.add("DSBD");
		POSTAGSET.add("INDICATION");
		POSTAGSET.add("DSBDC");
		POSTAGSET.add("DSBDF");
		POSTAGSET.add("DPN");
			
		section = "";
		this.readGrammar();
		// Print rules
		//System.out.println(this.toString());
		
		///////////////////////////////////
		EntryItem headItem = new EntryItem();
		//headItem.setTerm(false);
		headItem.setSign("S");
		ArrayList<EarleyEntry> s = this.getAllEarleyEntries(headItem);
		
		//for(int i = 0; i < s.size(); i++)
		//	System.out.println(s.get(i));
		
	}
	@Override
	public boolean isPOSTAG(String sign){
		for (int i=0; i<POSTAGSET.size(); i++){
			if(POSTAGSET.get(i).equals(sign)){
				return true;
			}
		}
		return false;
	}
	@Override
	protected void readGrammar() {
		// TODO Auto-generated method stub
		String line;
        try
        {
        	File file = new File(this.grammarFile);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            while ((line = reader.readLine()) != null)
            {
                line = line.trim();
                if(isComment(line)){
                	if(line.toLowerCase().indexOf("lexicon entries") != -1){
                		section = "lexicon"; 
                		//System.out.println(line);
                	}
                }
                else{
	                if (line.length() > 0 ){
	                	addRule(line);
	                }
                }
            }
            reader.close();           
        }
        catch (FileNotFoundException ex)
        {
            ex.printStackTrace();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
	}
	private boolean isComment(String line){//check if current line is remark
		if(line.startsWith("#") || line.startsWith("%")){
			return true;
		}
		else{
			return false;
		}
	}
    private void addRule(String line){
    	//Rule rule = new Rule();
    	//System.out.println(line);
    	
		int index = line.indexOf(DEDUCT_SIGN);
		//EntryItem headItem = new EntryItem();
		//headItem.setTerm(false);
		String headStr = line.substring(0, index-1).trim();
		//System.out.println("headStr :"+headStr);
		if(section.equals("lexicon")){
			POSTAGSET.add(headStr);
		}
		//headItem.setSign(headStr);
		//System.out.println(line.substring(index+2).trim());
		String rem = line.substring(index+2).trim();
		String[] entries = rem.split(ENTRY_SEPERATOR);
		//System.out.println("entries :"+entries.length);
		count_input_rule += entries.length;
		for(int j=0; j<entries.length; j++){
			
			//System.out.println(entries[j]);
			String entry = entries[j].trim();
			String items[] = entry.split(ITEM_SEPERATOR);
		
			ArrayList<EntryItem> itemlist = new ArrayList<EntryItem>();
			for(int i=0; i<items.length; i++ ){
				//System.out.println(items[i]);
				EntryItem rightItem = new EntryItem();
				String temp = items[i];
				if(items[i].startsWith(TERM_SIGN) && items[i].endsWith(TERM_SIGN)){
					 temp = items[i].substring(1, items[i].length()-1);
				}
				//System.out.println(temp);
				/*
				if (EarleyEntry.ifTerm(temp)){
					rightItem.setTerm(true);
				}
				else{
					rightItem.setTerm(false);
				}*/
				rightItem.setSign(temp);
				itemlist.add(rightItem);
			}
		
			if (!this.rules.containsKey(headStr)){
				ArrayList<ArrayList<EntryItem>> newlist = new ArrayList<ArrayList<EntryItem>>(); 
				newlist.add(itemlist);
				this.rules.put(headStr, newlist);
				//System.out.println("newlist : "+newlist.toString());
				//System.out.println("headStr :"+headStr);
			}
			else{
				//System.out.println("same");
				ArrayList<ArrayList<EntryItem>>  origlist = this.rules.get(headStr);
				origlist.add(itemlist);
				this.rules.put(headStr, origlist);
				//System.out.println("origlist : "+origlist.toString());
				//System.out.println("headStr :"+headStr);
			}		
		}
    }
	
}
