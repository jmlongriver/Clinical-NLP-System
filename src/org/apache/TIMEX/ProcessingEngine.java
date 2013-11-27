package org.apache.TIMEX;

import org.apache.TIMEX.MatchResult;
import org.apache.TIMEX.NormPattern;
import org.apache.TIMEX.Patterns;
import org.apache.TIMEX.Rule;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcessingEngine {
	//private members
	Rule _rule=null;
	Patterns _pattern=null;
	NormPattern _norm_pattern=null;

	public ProcessingEngine() {
		// TODO Auto-generated constructor stub
		String location = ProcessingEngine.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        //System.out.println(location);
        
        String dirr=location+".."+File.separator+"resources"+File.separator+"TIMEX"+File.separator;
        String pattern_dir=dirr+"patterns";
        String npattern_dir=dirr+"norm_patterns";
        String rule_dir=dirr+"rules";
        
        this._pattern=new Patterns(pattern_dir);
        //this._pattern.print();
        
        this._norm_pattern=new NormPattern(npattern_dir);
        //this._norm_pattern.print();
        
        this._rule=new Rule(rule_dir,this._pattern,this._norm_pattern);
        //this._rule.print();
	}
	
	
	public ArrayList<MatchResult> extract(String stt){
		ArrayList<MatchResult> result=this._rule.apply_rule(stt);
		ArrayList<MatchResult> nresult=new ArrayList<MatchResult>();
		if (result.size()>1){
			for (MatchResult mr:result){
				MatchResult tmr=new MatchResult(mr);
				if (mr.norm_str.equals("R")){
					continue;
				}
				nresult.add(tmr);
			}
			return nresult;
		}
		else{
			return result;
		}
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
        
        

	}

}
