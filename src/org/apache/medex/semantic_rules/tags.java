package org.apache.medex.semantic_rules;

import org.javatuples.Pair;

/** This class contains the data structure for storing information of term-tag pair like
 * term, tag, start, end, index, pos
 * 
 * @author shaha2
 *
 */
public class tags {

	 private String term;
	 private String tag;
	 private int start;
	 private Pair<Pair<String, Integer>, Integer> end;
	 private int index;
	 private int pos;
	 private String final_tag;
	 private String prevTag;
	 private String prevTerm;

	    public tags(int index,int pos,String term,String tag,String prevTag, String prevTerm, int start, Pair<Pair<String, Integer>, Integer> pair) {

	    	this.index = index;
	    	this.pos = pos;
	    	this.term = term;
	    	this.tag = tag;
	    	this.start = start;
	    	this.end = pair;
	    	this.prevTag = prevTag;
	    	this.prevTerm = prevTerm;
	    }

		public String getTerm() {
			return term;
		}

		public void setTerm(String term) {
			this.term = term;
		}

		public String getTag() {
			return tag;
		}

		public void setTag(String tag) {
			this.tag = tag;
		}
		
		public String getPrevTag() {
			return prevTag;
		}

		public void setPrevTag(String prevTag) {
			this.prevTag = prevTag;
		}
		
		public String getPrevTerm() {
			return prevTerm;
		}

		public void setPrevTerm(String prevTerm) {
			this.prevTerm = prevTerm;
		}

		public int getStart() {
			return start;
		}

		public void setStart(int start) {
			this.start = start;
		}

		public Pair<Pair<String, Integer>, Integer> getEnd() {
			return end;
		}

		public void setEnd(Pair<Pair<String, Integer>, Integer> end) {
			this.end = end;
		}

		public String getFinal_tag() {
			return final_tag;
		}

		public void setFinal_tag(String final_tag) {
			this.final_tag = final_tag;
		}

		public int getIndex() {
			return index;
		}

		public void setIndex(int index) {
			this.index = index;
		}

		public int getPos() {
			return pos;
		}

		public void setPos(int pos) {
			this.pos = pos;
		}
}
