package org.apache.NLPTools.CFGparser;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Iterator;

public class EarleyEntry {
	protected ArrayList<EntryItem> itemlist;
	protected int dot_pos; //where dot lies
	protected int start_pos;// where it start
	protected int end_pos;//where it end
	protected String operation; //predict/scan/complete
	protected ArrayList<EarleyEntry> ancestor;
	//protected String status; //start, predict, scan, complete
	public EarleyEntry(){
        itemlist = new ArrayList<EntryItem>();
		dot_pos = 2;//after head and ->
		ancestor = new ArrayList<EarleyEntry>();
		start_pos = 0;
		end_pos = 0;
		operation = "";
	}
	public int hashCode(){
		return this.toString2().hashCode();
	}
	public boolean equals(Object o){
		if(o instanceof EarleyEntry){
			ArrayList<EntryItem> itemlist2 = ((EarleyEntry)o).getItemlist();
			if(itemlist2.size() != itemlist.size()){
				return false;
			}
			else{
				/*
				Iterator<EntryItem> itr = itemlist.iterator();
				Iterator<EntryItem> itr2 = itemlist2.iterator();
				while(itr.hasNext()){
					if(itr.next() != itr2.next()){
						return false;
					}
				}
				*/
				for (int i=0; i<itemlist.size(); i++){
					if(!itemlist.get(i).equals(itemlist2.get(i))){
						return false;
					}
				}
				if(dot_pos != ((EarleyEntry)o).getDot_pos()){
					return false;
				}
				if(start_pos != ((EarleyEntry)o).getstart_pos()){
					return false;
				}
				if(end_pos != ((EarleyEntry)o).getend_pos()){
					return false;
				}
				if(itemlist2.size() != itemlist.size()){
					return false;
				}
				
			}
			return true;
		}
		return false;
	}
	/*
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}*/
	public String toString2(){
		String returned = "";
		for (int i=0; i<itemlist.size(); i++){
			if(dot_pos ==i){
				returned += " . ";
			}
			returned += itemlist.get(i).getSign() + " ";
			returned += "(" + String.valueOf(this.start_pos) + "," +String.valueOf(this.end_pos)+")";		
		}
		return returned;
	}
	public void adjustAncestor(Grammar grammar){
		LinkedList<EarleyEntry> entry_stack = new LinkedList<EarleyEntry>();
		for(int i=0; i<this.ancestor.size(); i++){
			entry_stack.add(this.ancestor.get(i));
		}
		
		while(!entry_stack.isEmpty()){
			EarleyEntry top = entry_stack.remove();
			this.ancestor.remove(top);
			if(!(top.ifComplete() || grammar.isPOSTAG(top.getHead().getSign()))){
				Iterator<EarleyEntry> s = top.getAncestor().iterator();
				while(s.hasNext()){
					entry_stack.add(s.next());
				}
			}
			else{
				this.ancestor.add(top);
			}
		}
	}
	public String toString(){
		String returned = "";
		for (int i=0; i<itemlist.size(); i++){
			if(dot_pos ==i){
				returned += " . ";
			}
			returned += itemlist.get(i).getSign() + " ";		
		}
		if(this.ifLastPosition()){
			returned += " . ";
		}
		returned += "\t" + "(" + String.valueOf(start_pos)+ "," + String.valueOf(end_pos) + ")";
		returned += "\t" + operation + "\t[";
		
		//for(int j=0; j<this.ancestor.size(); j++){
		//	EarleyEntry cur_ances = ancestor.get(j);
		
		for(int j=0; j<this.ancestor.size(); j++){
			returned += ancestor.get(j).toString2() + "\t";
		}
		returned += "]";
		
		return returned;
	}
	public boolean moveIndex(){
		if(dot_pos < itemlist.size()){
			dot_pos ++;
			return true;
		}
		else{
			return false;
		}
	}
	public EntryItem getHead(){
		return itemlist.get(0);
	}
	public String getOperation(){
		return operation;
	}
	public void setOperation(String operation){
		this.operation = operation;
	}
	public EarleyEntry clone(){
		EarleyEntry copy = new EarleyEntry();
		copy.setAncestor(this.ancestor);
		copy.setDot_pos(this.dot_pos);
		copy.setItemlist(this.itemlist);
		copy.setOperation(this.operation);
		copy.setStart_pos(this.start_pos);
		copy.setend_pos(this.end_pos);
		return copy;
	}
	public boolean ifComplete(){
		if(dot_pos == itemlist.size())
			return true;
		else
			return false;
	}
	public ArrayList<EntryItem> getItemlist() {
		return itemlist;
	}
	public void setItemlist(ArrayList<EntryItem> itemlist) {
		this.itemlist = itemlist;
	}
	public EntryItem getNextItem(){
		if(dot_pos < itemlist.size()) 
			return itemlist.get(dot_pos);
		else
			return null;
	}
	public void setItemlist(String phrase){
		String items[] = phrase.split(" ");
		
		for(int i=0; i<items.length;  i++ ){
			EntryItem rightItem = new EntryItem();
			/*
			if (ifTerm(items[i])){
				rightItem.setTerm(true);
			}
			else{
				rightItem.setTerm(false);
			}
			*/
			rightItem.setSign(items[i]);
			this.itemlist.add(rightItem);
		}
		
	}
	public boolean ifLastPosition(){
		if (dot_pos == itemlist.size()){
			return true;
		}
		else{
			return false;
		}
		
	}
	public static boolean ifTerm(String word){
		//evaluate if word is term
		if (Character.isUpperCase(word.charAt(0))){
			return false;
		}
		return true;
	}
	
	public int getDot_pos() {
		return dot_pos;
	}
	public void setDot_pos(int dot_pos) {
		this.dot_pos = dot_pos;
	}
	public int getstart_pos() {
		return start_pos;
	}
	public void setStart_pos(int start_pos) {
		this.start_pos = start_pos;
	}
	public int getend_pos() {
		return end_pos;
	}
	public void setend_pos(int end_pos) {
		this.end_pos = end_pos;
	}
	public ArrayList<EarleyEntry> getAncestor() {
		return ancestor;
	}
	public void setAncestor(ArrayList<EarleyEntry> ancestor) {
		this.ancestor = ancestor;
	}
	public Boolean equals(EarleyEntry entry2){
		if (entry2.getAncestor() == this.ancestor &&
		    entry2.getDot_pos() == this.getDot_pos() &&
		    entry2.getItemlist() == this.getItemlist()){
			return true;
		}
		return false;
	}
}
