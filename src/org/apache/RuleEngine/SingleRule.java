/**
 * 
 */
package org.apache.RuleEngine;

import org.apache.RuleEngine.Rule;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ywu4
 * 
 */
public class SingleRule {
	// private members
	private String _rule_str = "";

	private String _expression = "";
	private String _expression_expand = "";
	private ArrayList<String> _pattern_al = new ArrayList<String>();

	private String _val = "";
	// map norm strings to the rank id in groups < norm_str, 0 >. Then the norm
	// val can be derived by looking up the norm pattern map using the group(0)
	// as key
	private ArrayList<ArrayList<String>> _val_al = new ArrayList<ArrayList<String>>();

	private Rule _rule = null;

	/**
	 * 
	 */
	// private functions
	private String _extend_pattern(String exp) {
		//System.out.println("-----extend pattern\t" + exp);
		String result = exp;
		String regex = "(%\\w+)";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(exp);

		while (m.find()) {
			String tpt = m.group(0);
			// System.out.println("SingleRule find expression: "+tpt);
			this._pattern_al.add(tpt.substring(1));
			String ext = this._rule.pattern().expand(tpt.substring(1));
			// System.out.println("SingleRule extend into : "+ext);
			result = result.replace(tpt, "(" + ext + ")");
			// result=result+m.group(i++);

		}

		//System.out.println(exp + "\t" + result);

		return result;
	}

	// public functions
	public SingleRule(String rule_str, Rule rule) {
		// TODO Auto-generated constructor stub
		this._rule = rule;

		this._rule_str = rule_str;
		String[] secs = rule_str.split("\",");
		for (String sec : secs) {
			String[] words = sec.trim().split("=\"");
			if (words.length != 2) {
				System.out.println("rules error\n" + rule_str);
			}
			String right = words[0].trim();
			String left = words[1].trim();

			// expression="%FREQword",val="%NormFREQword(group(1))"
			if (right.equals("expression")) {
				this._expression = left;
				this._expression_expand = this._extend_pattern(left);
			} else if (right.equals("val")) {
				if (left.endsWith("\"")) {
					left = left.substring(0, left.length() - 1);
				}

				this._val = left;

				// pass the val for normalization
				// val="%NormFREQword(group(1))"
				String regx = "%(\\w+)\\(group\\((\\d+)\\)\\)";
				Pattern p = Pattern.compile(regx);
				Matcher m = p.matcher(left);

				String npattern = "";
				String sub_pattern = "";
				String rank = "";

				while (m.find()) {
					sub_pattern = m.group(0);
					npattern = m.group(1);
					rank = m.group(2);
					ArrayList<String> tal = new ArrayList<String>();
					tal.add(sub_pattern);
					tal.add(npattern);
					tal.add(rank);
					this._val_al.add(tal);
					// System.out.println("#####"+sub_pattern+"\t"+npattern+"\t"+rank);

				}

			}
		}
	}

	public String str() {
		return this._rule_str + '\t' + this._expression + '\t' + this._val;
	}

	public String expand_rule() {
		return this._expression_expand;
	}

	public String get_pattern(int i) {
		return this._pattern_al.get(i);
	}

	public String expression() {
		return this._expression;
	}

	public String expression_exp() {
		return this._expression_expand;
	}

	public String val() {
		return this._val;
	}

	public String norm_val(Matcher m) {
		/*
		 * for(int i=0;i<=m.groupCount();i++){
		 * System.out.println("group "+i+"\t"+m.group(i)); }
		 */
		//System.out.println("--------start norm_val " + m.groupCount());
		String result = this._val;
		String match_stt = "";
		for (ArrayList<String> tal : this._val_al) {
			int rank = Integer.parseInt(tal.get(2));
			String section = tal.get(1);
			// System.out.println("section: "+section + " rank: "+rank);

			match_stt = m.group(rank);
			// System.out.println("match_stt:"+match_stt);
			String match_val = this._rule.norm_pattern().norm(section,
					match_stt);
			// System.out.println("match_val:"+match_val);
			result = result.replace(tal.get(0), match_val);
		}

		String regx = "group\\((\\d+)\\)";
		Pattern p = Pattern.compile(regx);
		// System.out.println("result: "+result);
		Matcher mm = p.matcher(result);
		String sub_str = "";
		while (mm.find()) {
			sub_str = mm.group();
			int rank = Integer.parseInt(mm.group(1));
			result = result.replace(sub_str, m.group(rank));
		}

		result = this._post_processing(result);
		return result;
	}

	private String _post_processing(String stt) {
		//System.out.println("Post processing");
		String result = stt;
		// FREQ_DAYUNIT|2|1|D
		if (result.startsWith("FREQ_DAYUNIT")) {
			String[] secs = result.split("\\|");
			System.out.println(secs[0] + "\t" + secs[1] + "\t" + secs[2]);
			int freq = Integer.parseInt(secs[1]);
			int freq_unit = Integer.parseInt(secs[2]);
			int ave = freq_unit / freq;
			int remain = freq_unit % freq;
			if (ave > 0 && remain == 0) {
				result = "R1P" + ave + secs[3];
			} else {
				if (secs[3].equals("D")) {
					float ave2 = (float) (freq_unit * 24) / freq;
					int tmp = freq_unit * 24;
					if (tmp % freq == 0) {
						result = "R1P" + (tmp / freq) + "H";
					} else {
						result = "R1P" + ((float) (tmp) / freq) + "H";
					}
				} else {
					result = "R1P" + ((float) (freq_unit) / freq) + secs[3];
				}
			}
		}
		return result;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
