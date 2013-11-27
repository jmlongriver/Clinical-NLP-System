package org.apache.NLPTools.CFGparser;

import java.util.ArrayList;

/*represent a rule
 * P -> S + T
 * Head = P; introduction_sign = '->';
 */

public class Rule {
	protected EntryItem head;
	protected ArrayList<EntryItem> itemlist;
	public EntryItem getHead() {
		return head;
	}
	public void setHead(EntryItem head) {
		this.head = head;
	}
	public ArrayList<EntryItem> getItemlist() {
		return itemlist;
	}
	public void setItemlist(ArrayList<EntryItem> itemlist) {
		this.itemlist = itemlist;
	}
	
	
}
