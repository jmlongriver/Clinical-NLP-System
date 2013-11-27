package org.apache.NLPTools.CFGparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
/**
* @author Min Jiang
* @version 1.0
* @since 2011-7-1
*/
public class EarleyChart {
	/**
	 * This class represent chart structure in the Earley parser.
	 * each chart contains a list of entries
	 * 
	 */
	
	protected int chart_index;
	protected String[] words; 
	public EarleyChart(){
		entrymap = new HashMap<Integer, EarleyEntry>();	
		entrylist = new ArrayList<EarleyEntry>();
	}
	public int getChart_index() {
		return chart_index;
	}
	public void setChart_index(int chart_index) {
		this.chart_index = chart_index;
	}
	
	//protected ArrayList<EarleyEntry> entrylist;
	protected HashMap<Integer, EarleyEntry> entrymap;
	protected ArrayList<EarleyEntry> entrylist;
	/*
	public String[] getWords() {
		return words;
	}
	public void setWords(String[] words) {
		this.words = words;
	}
	*/
	
	public ArrayList<EarleyEntry> getEntryList() {
		
		return entrylist;
	}
	public HashMap<Integer, EarleyEntry> getEntryMap() {
		return entrymap;
	}
	public void setEntryMap(HashMap<Integer, EarleyEntry> entrymap) {
		this.entrymap = entrymap;
	}
	public void setEntryList(ArrayList<EarleyEntry> entrylist){
		this.entrylist = entrylist;
	}
	public String toString(){
		String returned = "\n------------------------------\n";
		returned += "Chart " + String.valueOf(chart_index) + '\n';
		Iterator<EarleyEntry> iter = entrylist.iterator();
		while (iter.hasNext()) {
			returned += iter.next().toString() + '\n';
		}
		return returned;
	}
}
