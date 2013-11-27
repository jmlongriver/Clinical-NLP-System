package org.apache.NLPTools.CFGparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class Grammar {
	protected abstract void readGrammar();
	public HashMap<String, ArrayList<ArrayList<EntryItem>>> rules;
	public static final String DEDUCT_SIGN = "->";
	public static final String TERM_SIGN = "\"";
	protected String grammarFile;
	public abstract boolean isPOSTAG(String sign);
	public int count_rule_read = 0;
	
	public Grammar(String GrammarFile){
		this.grammarFile = GrammarFile; //path of grammar file
		this.rules = new HashMap<String, ArrayList<ArrayList<EntryItem>>>();
		//rules read from grammar file are stored in this data structure
	}
	
	public ArrayList<ArrayList<EntryItem>> getAllProductions(EntryItem head){
		// given head, get list of production rules starting with it
			return rules.get(head);
	}
	
	public ArrayList<EarleyEntry> getAllEarleyEntries(EntryItem head){
		
		//System.out.println(" Start of getAllEarleyEntries");
		long start = System.currentTimeMillis();

		
		// given head, get list of Earley entries which composed of head and production rules 
		ArrayList<EarleyEntry> newList = new ArrayList<EarleyEntry>();
		ArrayList<ArrayList<EntryItem>> itemlists = rules.get(head.getSign());
		
		
		if(itemlists != null){
			for (int i=0; i<itemlists.size(); i++){
				ArrayList<EntryItem> items = new ArrayList<EntryItem>();
				items.add(head);
				EntryItem deduct_item = new EntryItem();
				deduct_item.setSign(DEDUCT_SIGN);
				//deduct_item.setTerm(true);
				items.add(deduct_item);
				items.addAll(itemlists.get(i));
				EarleyEntry entry = new EarleyEntry();
				entry.setItemlist(items);
				newList.add(entry);
			}
		}
		//else
			//System.out.println("No rule is found");
		// Get elapsed time in seconds
		//System.out.print("GetEntries totally consumes ");
		//System.out.println((System.currentTimeMillis() - start)/1000F);
		return newList;
	}
	public String toString(){
		Iterator it = rules.entrySet().iterator();
		String returned = "";
		while (it.hasNext()) { 
			 Map.Entry pairs = (Map.Entry)it.next();
			 String head = (String)pairs.getKey();		 
			 ArrayList<ArrayList<EntryItem>> entryItem = (ArrayList<ArrayList<EntryItem>>)pairs.getValue();
			 returned += head + " -> ";
			 count_rule_read += entryItem.size();
			 for (int i= 0; i<entryItem.size(); i++){
				 
				 String rule = "";				 
				 ArrayList<EntryItem> x = entryItem.get(i);
				 for (int j= 0; j<x.size(); j++){
					 rule += x.get(j).getSign() + " "; 
				 }
				 returned += rule + "\t" + "|" + "\t";
				 
			 }
			 returned += "\n";
		}
		return returned;

	}
	
}
