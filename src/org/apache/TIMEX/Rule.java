package org.apache.TIMEX;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.apache.NLPTools.Global;
import org.apache.TIMEX.MatchResult;
import org.apache.TIMEX.NormPattern;
import org.apache.TIMEX.Patterns;
import org.apache.TIMEX.SingleRule;
import org.apache.medex.DrugTag;

public class Rule implements Global {

	// private member
	private Patterns _pattern = null;
	private NormPattern _norm_pattern = null;
	private Map<String, ArrayList<SingleRule>> _rules = new HashMap<String, ArrayList<SingleRule>>();

	// HashMap<type,<expression,val>>

	// public member

	

	// public function
	

	public Rule(String dirr, Patterns pt, NormPattern np) {
		this._norm_pattern = np;
		this._pattern = pt;
		// TODO Auto-generated constructor stub
		ArrayList<String> al = new ArrayList<String>();
		File input_path = new File(dirr);

		try {

			for (File child : input_path.listFiles()) {
				if (".".equals(child.getName()) || "..".equals(child.getName())
						|| child.getName().startsWith(".")
						|| child.getName().endsWith("~"))
					continue; // Ignore the self and parent aliases.
				if (child.isFile()) {
					al.clear();
					//System.out.println("Load rules from file "
							//+ child.toString());
					FileInputStream fstream = new FileInputStream(child);

					DataInputStream in = new DataInputStream(fstream);
					// System.out.println("here ");

					BufferedReader br = new BufferedReader(
							new InputStreamReader(in));

					String strLine;
					String section = "";

					int ct = 0;

					while ((strLine = br.readLine()) != null) {
						strLine = strLine.trim();
						ct = ct + 1;
						//System.out.println(ct + "\t" + strLine);
						if (strLine.startsWith("//") || strLine.length() == 0) { // remove
																					// commented
																					// line
							continue;
						}
						if (strLine.endsWith(":")) {
							// meet with new section, finish the current regular
							// expression for current section
							if (section.length() == 0) {
								section = strLine.substring(0,
										strLine.length() - 1).trim();
							} else {

								this._add_rule(al, section);
								section = strLine.substring(0,
										strLine.length() - 1).trim();
								al.clear();
							}

						} else {
							al.add(strLine);
						}

					}
					if (al.size() > 0) {
						this._add_rule(al, section);
					}
				}

			}
			
			Iterator it = this._rules.entrySet().iterator();
			//System.out.println("Load rules over, ");
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				String section = (String) entry.getKey();
				//System.out.println(section+"\t"+((ArrayList<SingleRule>)entry.getValue()).size());
			}

			

		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}
	
	public Patterns pattern() {
		return this._pattern;
	}

	public NormPattern norm_pattern() {
		return this._norm_pattern;
	}

	public void print() {
		//System.out.println("\nAll the rules:");
		Iterator it = this._rules.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			Object key = entry.getKey();
			ArrayList<SingleRule> value = (ArrayList<SingleRule>) entry
					.getValue();
			// System.out.println("key=" + key);
			//for (SingleRule sr:value){
			//	System.out.println(sr.str()+'\t'+sr.expand_rule());
			//}
		}
	}

	public Map<String, ArrayList<SingleRule>> rule() {
		return this._rules;
	}

	public ArrayList<MatchResult> apply_rule(String stt) {
		ArrayList<MatchResult> result_al = new ArrayList<MatchResult>();

		// iterate rules to find the match string
		Iterator it = this._rules.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String section = (String) entry.getKey();
			ArrayList<SingleRule> value = (ArrayList<SingleRule>) entry.getValue();
			//System.out.println("section=" + section);
			for (SingleRule sr : value) {
				Pattern p = Pattern.compile(sr.expression_exp());

				Matcher m = p.matcher(stt);
				while (m.find()) {
					// System.out.println("match expression: "+sr.expression_exp());
					// System.out.println("string: "+stt);
					int pos=m.start();
					if (pos >0){
						if (stt.charAt(pos-1) != ' '){
							continue;
						}
					}
					MatchResult result = new MatchResult();
					// System.out.println("matched group num:"+m.groupCount()+"\t"+m.group(0));

					String norm_val = sr.norm_val(m);

					result.section = section;
					result.match_str = m.group(0);
					result.norm_str = norm_val;
					result.start = m.start();
					result.end = m.end();
					result_al.add(result);

				}
			}
		}
		// filtering overlapped
		if (result_al.size() >= 2) {
			//System.out.println("start filtering, original result size: "+ result_al.size());
			int ii = 0;
			/*
			 * for (MatchResult mr:result_al){ ii=ii+1;
			 * System.out.println(""+ii+
			 * "\t"+mr.start+"\t"+mr.end+"\t"+mr.match_str); }
			 */
			ArrayList<MatchResult> filter_result = new ArrayList<MatchResult>();

			Global.Section[] secs = new Global.Section[result_al.size()];
			for (int i = 0; i < result_al.size(); i++) {
				secs[i] = new Global.Section();
				secs[i].start = result_al.get(i).start;
				secs[i].end = result_al.get(i).end;
			}
			Global.ArrayIndexComparator cmp = new Global.ArrayIndexComparator(
					secs);
			Integer[] indices = cmp.createIndexArray();
			Arrays.sort(indices, cmp);
			// System.out.println(Arrays.toString(indices));
			//System.out.println("after sort");
			ii = 0;
			for (int i = 0; i < result_al.size(); i++) {
				MatchResult mr = result_al.get(indices[i]);
				ii = ii + 1;
				// System.out.println(""+ii+"\t"+mr.start+"\t"+mr.end+"\t"+mr.match_str);
			}

			int pre = -1;
			for (int i = 0; i < indices.length; i++) {
				if (i == 0) {
					pre = 0;
				} else {
					MatchResult mr1 = result_al.get(indices[pre]);
					MatchResult mr2 = result_al.get(indices[i]);
					if (this.over_lap(mr1, mr2)) {
						if (mr2.match_str.length() > mr1.match_str.length()) {
							pre = i;
							//System.out.println("remove : " + mr1.match_str);
						}
					} else {
						MatchResult tmr = new MatchResult(mr1);
						filter_result.add(tmr);
						// System.out.println("add : "+tmr.match_str);
						pre = i;
					}
				}
			}
			// add the last drugtag
			MatchResult tmr = result_al.get(indices[pre]);
			MatchResult nmr = new MatchResult(tmr);
			filter_result.add(nmr);
			// System.out.println("end add : "+nmr.match_str);
			/*
			 * System.out.println("after filtering, original result size: "
			 * +filter_result.size()); ii=0; for (int
			 * i=0;i<filter_result.size();i++){ MatchResult
			 * mr=filter_result.get(i); ii=ii+1;
			 * System.out.println(""+ii+"\t"+mr
			 * .start+"\t"+mr.end+"\t"+mr.match_str); }
			 */
			return filter_result;
		} else {
			return result_al;
		}

	}

	public boolean over_lap(MatchResult mr1, MatchResult mr2) {
		boolean flag = false;
		if (mr2.start < mr1.end) {
			flag = true;
		}
		return flag;
	}
	
	// private function
		private void _add_rule(ArrayList<String> al, String section) {
			// expression="%FREQword",val="%NormFREQword(group(1))"
			ArrayList<SingleRule> tal = new ArrayList<SingleRule>();
			for (String st : al) {
				//System.out.println("add rule: " + st);
				SingleRule sr = new SingleRule(st, this);
				tal.add(sr);
			}
			this._rules.put(section, tal);
		}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
