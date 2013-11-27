/**
 * 
 */
package org.apache.RuleEngine;

/**
 * @author ywu4
 *
 */
public class MatchResult {
	public String section="";
	public String match_str="";
	public String norm_str="";
	public int start=-1;
	public int end=-1;

	/**
	 * 
	 */
	public MatchResult() {
		// TODO Auto-generated constructor stub
		section="";
		match_str="";
		norm_str="";
		start=-1;
		end=-1;
	}
	public MatchResult(MatchResult mr) {
		// TODO Auto-generated constructor stub
		this.section=mr.section;
		this.match_str=mr.match_str;
		this.norm_str=mr.norm_str;
		this.start=mr.start;
		this.end=mr.end;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
