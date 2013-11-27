package org.apache.medex;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.algorithms.VectorSpaceModel;
import org.apache.commons.lang.StringUtils;

public class Encoder {
	public HashMap<String, ArrayList<String>> rx_code_map;
	public HashMap<String, String> normalization_map;
	public HashMap<String, String> generic_map;
	public HashMap<String, String> code_name_map;
	public Util utility;
	public BufferedWriter log; 
	protected boolean ifLog = true;
	
	
	public Encoder(HashMap<String, ArrayList<String>> rx_code_map, HashMap<String, String> code_name_map, HashMap<String, String> normalization_map, HashMap<String, String> generic_map)
	{
		this.rx_code_map = rx_code_map;
		this.generic_map = generic_map;
		this.code_name_map = code_name_map;
		this.normalization_map = normalization_map;
		this.utility = new Util(normalization_map);
		
	}
	/**
	 * Search UMLS code and RXNORM code for the given drug signature
	 * 
	 * @param siglist	list of drug signature e.g. "{"Warfarin FFF DRUG", "1mg", "DOSE"}
	 * @return array of string contains UMLS code and RXNORM code	e.g.{"C1123825", "328830"}
	 * @see Encoder
	 */
	public String[] encode(String[] siglist, BufferedWriter log ) {
		 //long start = System.currentTimeMillis();
		//for (int sig_index = 0; sig_index < siglist.length; sig_index++) {
		//	System.out.println(siglist[sig_index]);
		//}
		//long t_start = System.nanoTime(); 
		String INGRE = "";
		String STRENGTH = "";
		String FORM = "";
		String ROUTE = "";
		String DOSE = "";
		this.log = log;
		//long t0 = System.nanoTime();
		for (int sig_index = 0; sig_index < siglist.length; sig_index++) {
			if (siglist[sig_index].equals("")) {
				continue;
			}
			writeLog("siglist: "+siglist[sig_index]);
			String[] terms = siglist[sig_index].split(" FFF ");

			// System.out.println(terms[0]+" "+terms[1]);
			String tag = terms[1];
			String token = terms[0];
			if (tag.equals(Util.DRUG_STRING)) {
				INGRE = token.toLowerCase();
			} 
			else if (tag.equals(Util.ROUTE_STRING)) {
				ROUTE = token.toLowerCase();
			} 
			else if(tag.equals(Util.FORM_STRING)){
				FORM = token.toLowerCase();
			}
			else if (tag.equals(Util.STRENGTH_STRING)) {
				STRENGTH = token.toLowerCase();
			} 
			else if (tag.equals(Util.AMOUNT_STRING)){
				DOSE = token.toLowerCase();
			}
			
			if(STRENGTH.trim().equals("")){
				STRENGTH = DOSE;
			}
			
		}
		
		
		//long t1 = System.nanoTime();
		//System.out.println("get data:" + (t1 - t0));
		//normalize drug form
		FORM = utility.normalizeForm(FORM.trim().toLowerCase());
		//long t2 = System.nanoTime();
		//System.out.println("norm form:" + (t2 - t1));
		ROUTE = utility.normalizeForm(ROUTE.trim().toLowerCase());
		//long t3 = System.nanoTime();
		//System.out.println("norm route:" + (t3 - t2));
		//normalize the drug name
		INGRE = utility.normalizeDrugName(INGRE);
		//long t4 = System.nanoTime();
		//System.out.println("norm drug:" + (t4 - t3));
		writeLog("\norg strength: "+STRENGTH+"\n");
		STRENGTH = utility.normalizeDose(STRENGTH);
		//long t5 = System.nanoTime();
		//System.out.println("norm strength:" + (t5 - t4));
		
		//make inference on form based on strength 
		if(STRENGTH.indexOf("tablet") >= 0){
			if(FORM.equals("")){
				FORM = "tablet";
			}
		}
		
		//if drug name contain form
		for(String str : utility.DRUG_FORMS){
			if(INGRE.endsWith(" "+str) && FORM.indexOf(str)<0){
				FORM = str + " "+FORM;
				INGRE = INGRE.substring(0, INGRE.length()-str.length()-1);
				//System.out.println(INGRE +"|");
			}
		}
		INGRE = INGRE.trim();
		STRENGTH = STRENGTH.trim();
		FORM = FORM.trim();
		ROUTE = ROUTE.trim();
		
		writeLog("\ndrug: "+INGRE+"\n");
		writeLog("strength: "+STRENGTH+"\n");
		writeLog("form: "+FORM+"\n");
		writeLog("route: "+ROUTE+"\n");
		
		
		//System.out.println(FORM);
		String[] codes = { "", "", "","" };
	
		
		//check if generic name is in the form like "generic(brand)" or "brand(generic)"
		int brand_start_offset = INGRE.indexOf('(');
		int brand_end_offset = INGRE.indexOf(')');
		//long t6 = System.nanoTime();
		//System.out.println("prepare data:" + (t6 - t5));
		if (brand_start_offset >= 0 && brand_end_offset >= 0 && brand_end_offset > brand_start_offset) {
						
			String s_generic = INGRE.substring(0, brand_start_offset).trim();
			String s_brand = INGRE.substring(brand_start_offset + 1, brand_end_offset).trim();
			
			
			codes = getCode(s_generic, STRENGTH, FORM, ROUTE, s_brand);
			if (!codes[0].equals("") || !codes[1].equals("")) {
				return codes;
			}
		}
		
		//long t7 = System.nanoTime();
		//System.out.println("getcode1:" + (t7 - t6));
		//System.out.println("hello");
		codes = getCode(INGRE, STRENGTH, FORM, ROUTE, "");		
		if (!codes[0].equals("") || !codes[1].equals("")) {
			return codes;
		}
		
		
		//long t8 = System.nanoTime();
		//System.out.println("getcode2:" + (t8 - t7));
		// System.out.println("cost:"+ (end-start));
		
		//System.out.println("all encode cost:"+ (t8-t_start));
		return codes;

	}
	
	/**
	 * Given generic name, and its related RXNORM information and UMLS CUI
	 * information, strength and form, return the RXCUI and UMLS CUI
	 * 
	 * @param drug_name	name of drug (generic or brand)
	 * @param strength	drug strength
	 * @param form	drug form
	 * @return array of string contains UMLS code and RXNORM code	e.g.{"C1123825", "328830"}
	 * @see Encoder
	 */
	private String[] getCode(String drug_name, String strength, String form, String route, String brand) {
		long t0 = System.nanoTime();
		
		//writeLog("\n-encoding--\n");
		writeLog(drug_name + "\n");
		writeLog(strength + "\n");
		writeLog(form + "\n");
		
		ArrayList<String> candidates = new ArrayList<String>();
		if(normalization_map.containsKey(drug_name)){
			drug_name = normalization_map.get(drug_name);
		}
		writeLog("drugname:"+drug_name+"|\n");
		String[] codes = { "", "","","" };
		writeLog(String.valueOf(rx_code_map.size())+"\n");
		writeLog(rx_code_map.containsKey(drug_name)+"\n");
	
		try{
			//get specific code
			
			
			
			
			ArrayList<String> can_codes = new ArrayList<String>();
			if (rx_code_map.containsKey(drug_name)) {
				//System.out.println("drug_name:"+drug_name);
				ArrayList<String> candidate_string = rx_code_map.get(drug_name); 
				/*
				for(int i=0; i<candidate_string.size(); i++){
		        	if(drug_name.equals("ciprofloxacin"))
		        		System.out.println(candidate_string.get(i));
		        }*/
				
				
				for(int i=0; i<candidate_string.size(); i++){
					candidates.add(Util.transformPercentage(candidate_string.get(i).substring(0, candidate_string.get(i).indexOf( "_" ) )));
					can_codes.add(candidate_string.get(i).substring(candidate_string.get(i).indexOf( "_" ), candidate_string.get(i).length() ));
				}
				
			}
			
			
			
			String items[] = drug_name.split(" ");
			if(items.length >=2){
				for (int i=0; i<items.length; i++){
					if(rx_code_map.containsKey(items[i])){
						ArrayList<String> candidate_string = rx_code_map.get(items[i]); 
						for(int j=0; j<candidate_string.size(); j++){
							candidates.add(Util.transformPercentage(candidate_string.get(j).substring(0, candidate_string.get(j).indexOf( "_" )) ));
							can_codes.add(candidate_string.get(j).substring(candidate_string.get(j).indexOf( "_" ), candidate_string.get(j).length() ));
						}
					}
				}
			}
			
			long t1 = System.nanoTime();
			System.out.println("candidate size:"+ candidates.size());
			System.out.println("get candidate:" + (t1 - t0));
			if(candidates.size() >0){
				
				for(int i=0; i<candidates.size(); i++){
					
					writeLog("candidate in rxcode: " + candidates.get(i)+"\t"+can_codes.get(i)+"\n");
				}
				
				int top_index = compareSimilarity(candidates, drug_name, strength, form, route, brand);
				long t2 = System.nanoTime();
				System.out.println("compare sim for full:" + (t2 - t1));
				//System.out.println("top_index:"+top_index);
				
				//System.out.println(candidates.get(top_index) + "\t"+can_codes.get(top_index));
				
				//VectorSpaceModel model = new VectorSpaceModel();
		        //model.setSimDistanceType( VectorSpaceModel.SimDistanceType.PartOfMatch);
		       
		        //ArrayList<Integer> index = model.getRankingTopN(search_str, candidates, 1);
		        //System.out.println("*********************************************");
		        //System.out.println(drug_name + " "+ strength+" " +form+" "+ route+" " +brand);
		        /*
		        for(int i=0; i<candidates.size(); i++){
		        	if(drug_name.equals("tyyroid(usp)"))
		        		System.out.println(candidates.get(i));
		        }*/
		        //System.out.println("------------------------------");
		        //System.out.println(search_str);
		        //for( Integer i: index ) {
		         //   System.out.println( i + ":" + candidates.get(i) + "\t"+can_codes.get(i) );
		            
		            
		        //}
		       
		        //String[] temp = can_codes.get(index.get(0)).split("_");
				String[] temp = can_codes.get(top_index).split("_");
		        codes[0] = temp[1];
		        codes[1] = temp[2];
		        //System.out.println(codes[0]);
		        //System.out.println(codes[1]);
		        
		      //get code for generic name
		        codes[2] = "";
		        codes[3] = "";
		    	if(generic_map.containsKey(codes[1])){
		    		codes[2] = generic_map.get(codes[1]).split("_")[1];
		    	}
		    	else{
		    		top_index = compareSimilarity(candidates, drug_name, "", "", "", "");
		    		temp = can_codes.get(top_index).split("_");
		    		writeLog("top_index of generic:"+top_index+"\n");
		    		writeLog("temp:"+candidates.get(top_index)+"\n");
		    		writeLog(temp[1]+"\n");
		    		writeLog(temp[2]+"\n");
		    	}
		    	
		    	long t3 = System.nanoTime();
				System.out.println("get generic code:" + (t3 - t2));
		    	
	    		String current_code = temp[2];
	    		String current_generic = "";
	    		String prev_code = current_code;
	    		while(generic_map.containsKey(current_code)){
	    			//writeLog("hello1\n");
	    			writeLog("current_code:"+current_code+"\n");
	    			writeLog("prev_code:"+prev_code+"\n");
	    			String generic_codes[] = generic_map.get(current_code) .split("_");
	    			//System.out.println(StringUtils.join(generic_codes,"\t"));
	    			current_code =generic_codes[1];
	    			current_generic = generic_codes[0];
	    			if (current_code.equals(prev_code)){
	    				break;
	    			}
	    			prev_code = current_code;
	    		}
	    		
	    		codes[2] = current_code;
	    		codes[3] = current_generic;
	    		
	    		if(codes[2].equals(codes[1])&&codes[3].equals("")){
	    			codes[3] = drug_name;
	    		}
	    		//System.out.println(codes[2]);
	    		//System.out.println(codes[3]);
	    		if(!codes[2].equals("") && codes[3].equals("")){
	    			if(code_name_map.containsKey(codes[2])){
	    				codes[3] = code_name_map.get(codes[2]);
	    			}
	    		}
	    		
	    		//System.out.println(codes[2]);
	    		//System.out.println(codes[3]);
	    		/*
	    		else{
	    			writeLog("hello2\n");
	    			codes[2] = temp[2];
	    			
	    		}*/
	    		writeLog("code 2:"+codes[2]+"\n");
		    	
		    	
		    	
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		long t8 = System.nanoTime();
		System.out.println("getcode cost:" + (t8 - t0));
        //System.out.println(codes[1]);
		return codes;

	}
	
	private int compareSimilarity(ArrayList<String> candidate_list, String drug_name, String strength, String form, String route, String brand){
		ArrayList<Double> scores = new ArrayList<Double>();
		int max_index = 0;
		double max_value = 0.0;
		ArrayList<String> sent_list = new ArrayList<String>();
		for(int i=0; i<candidate_list.size(); i++){
			writeLog("candidate:"+candidate_list.get(i)+"\n");
			String[] items = candidate_list.get(i).split("\t");
			//for(String str : items){
			//	writeLog("item:"+str+"\n");
			//}
			//System.out.println(candidate_list.get(i));
			String rxnorm_generic = items[0].trim();
			String rxnorm_strength = items[1].trim();
			String rxnorm_form = items[2].trim();
			String rxnorm_brand = items[3].trim();
			String sent = items[4].trim();
			String item = utility.stem(sent.replace("[","").replace("]","").replace("(","").replace(")","").replace(" %", "%").split(" "));
			//writeLog("before stem string: "+ sent.replace("[","").replace("]","").replace("(","").replace(")","").replace(" %", "%"));
			//writeLog("after stem:"+item+"\n");
			sent_list.add(item);
			String form_route = form + " "+ route.trim();
			if(form.equals("")){
				form_route = route;
			}
			if(route.equals("")){
				form_route = form;
			}
			if(form.equals("") && route.equals("")){
				form_route = "";
			}
			
			//vector space model for field
			/*
			double score = utility.calculateStrengthSimilarity(strength, rxnorm_strength) + 
					       utility.calculateFormSimilarity(form_route, rxnorm_form) + 
					       utility.calculateDrugSimilarity(drug_name,rxnorm_generic)+  
					       utility.calculateDrugSimilarity(brand,rxnorm_brand)+
					       utility.calculateDrugSimilarity(drug_name,rxnorm_brand);
			writeLog("***************************************************************************\n");
			writeLog(drug_name +"--"+rxnorm_generic+"--"+utility.calculateDrugSimilarity(drug_name,rxnorm_generic)+"\n");
			writeLog(strength +"--"+rxnorm_strength+"--"+utility.calculateStrengthSimilarity(strength, rxnorm_strength)+"\n");
			writeLog(form_route+"--"+rxnorm_form+"--"+utility.calculateFormSimilarity(form_route, rxnorm_form)+"\n");
			writeLog(brand + "--"+rxnorm_brand+"--"+utility.calculateDrugSimilarity(brand,rxnorm_brand)+"\n");
			writeLog(drug_name + "--"+rxnorm_brand+"--"+utility.calculateDrugSimilarity(drug_name,rxnorm_brand)+"\n");
			writeLog("Score:" + score+"\n\n");
			
			if(score > max_value){
				max_value = score;
				max_index = i;
			}
			scores.add(score);
			*/
			
		}
		//writeLog("query string: "+ drug_name+" "+strength+" "+form+" "+route+" "+brand+"\n");
		//writeLog("Most matched: "+ candidate_list.get(max_index)+"\n");
		//writeLog("Max score:"+ max_value+"\n");
		
		
		
		
		//vector space model for whole sent
		
		
		
		VectorSpaceModel vsm = new VectorSpaceModel();
		try{
			//System.out.println(strength)
			String[] doseUnit = utility.getdoseNumberUnit(strength);
			String query_string =  drug_name+" "+doseUnit[0]+" "+doseUnit[1].replace(" ", "")+" "+form+" "+route+" "+brand;
			writeLog("\n---------------------------------\n");
			
			
			max_index = vsm.getRankingTopN(utility.stem(query_string.split(" ")), sent_list, 10).get(0);
			writeLog("query string: "+query_string+"\n");
			writeLog("most matched string: "+sent_list.get(max_index)+"\n\n"); 	
			
			
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return max_index;
	}
	
	/**
	 * write log if the flag is set
	 * 
	 * @param msg
	 */
	public void writeLog(String msg) {
		if (ifLog) {
			try {
				log.write(msg);
			} catch (IOException e) {
				System.out.println(e.toString());
			}

		}
	}
}
