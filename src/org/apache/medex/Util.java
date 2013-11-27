package org.apache.medex;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.NLPTools.Stemmer;

import org.apache.algorithms.VectorSpaceModel;
import org.apache.commons.lang.StringUtils;

public class Util {
	public HashMap<String, String> normalization_map;
	public Util(HashMap<String, String> normalization_map)
	{
		this.normalization_map = normalization_map;
	}
	
	public static final String DRUG_STRING = "DRUG";
	public static final String BRAND_STRING = "BRAND";
	public static final String FORM_STRING = "FORM";
	public static final String STRENGTH_STRING = "STRENGTH";
	public static final String AMOUNT_STRING = "DOSEAMOUNT";
	public static final String ROUTE_STRING = "ROUTE";
	public static final String FREQUENCY_STRING = "FREQUENCY";
	public static final String DURATION_STRING = "DURATION";
	public static final String NECESSITY_STRING = "NECESSITY";
	public static final String[] DRUG_FORMS = {"ophthalmic", "topical","ointment","rectal","oral solution",
    	                                         "otic","oral","irrigation","intraperitoneal",
    	                                         "inhalant","pill","sublingual","ointment","chewable",
    	                                         "nasal","dental","injectable","irrigation","cream"};			

	public static final String[] SIGNATURE_STRING = { DRUG_STRING,BRAND_STRING, FORM_STRING, STRENGTH_STRING, AMOUNT_STRING,
			ROUTE_STRING, FREQUENCY_STRING, DURATION_STRING, NECESSITY_STRING };
	
	/**
	 * Normalize drug form
	 * 
	 * @param field "form" in the output e.g. "tab","tabs"
	 * @return normalized output in RXNOMR e.g. "oral tablet"
	 * @see Util
	 */
	
	public String normalizeDrugName(String drugname) {
				
		if(normalization_map.containsKey(drugname)){
			return normalization_map.get(drugname);
		}
		//Pattern p = Pattern.compile("[ ]*[\\-,\\/][ ]*");
	    // get a matcher object
	    //Matcher m = p.matcher(drugname); 
	    String normalized_drugname = drugname;
	    
	    //System.out.println(drugname);
	    
	    
	    normalized_drugname = normalized_drugname.replaceAll("[ ]*\\/[ ]*"," \\/ ");
	    normalized_drugname = normalized_drugname.replaceAll("[ ]*\\-[ ]*","\\-");
	    normalized_drugname = normalized_drugname.replaceAll("[ ]*\\,[ ]*","\\, ");
	    normalized_drugname = normalized_drugname.replaceAll("[ ]*\\'[ ]*","\\'");
	    normalized_drugname = normalized_drugname.replaceAll("[ ]*\\_[ ]*"," \\_ ");
	    normalized_drugname = normalized_drugname.replaceAll("[ ]*\\([ ]*"," \\(");
	    //System.out.println(normalized_drugname);
	    normalized_drugname = normalized_drugname.replaceAll("[ ]*\\)[ ]*","\\) ");
	    normalized_drugname = normalized_drugname.replaceAll("[ ]*\\:[ ]*","\\:");
	    normalized_drugname = normalized_drugname.replaceAll("[ ]*\\+[ ]*","\\+");
	    normalized_drugname = normalized_drugname.replaceAll("[ ]*\\%[ ]*"," \\%");
	    normalized_drugname = normalized_drugname.replaceAll("[ ]*\\&[ ]*"," \\& ");
	    normalized_drugname = normalized_drugname.replaceAll("[ ]*\\![ ]*","\\! ");
	    //System.out.println(normalized_drugname);
	    //System.out.println(normalized_drugname);
	   
	    
	    /*
		String[] items = drugname.split("- ,/");
		String normalized_drug = "";
		for(String item : items){
			if(!item.trim().equals("")){
				normalized_drug+= item.trim() + " ";
			}
		}
		
		String[] single_drugs = drugname.split("/");
		
		if(single_drugs.length >= 2){
			for(int i=0; i<single_drugs.length; i++){
				single_drugs[i] =  single_drugs[i].trim();
			}
			return StringUtils.join(single_drugs, "/");
		}
		
		
		drugname  = drugname.replace(" - ", "-").replace(" ,",",");
		*/
	    /*if (drugname.equals("beta-cardone")){
	    	System.out.println("normalized_drugname:"+normalized_drugname);
	    }*/
	   
	    
		return normalized_drugname;
	}
	
	/**
	 * Transform "3-15-20" to "3 15 20"
	 * 
	 * @param field "input"
	 * @return tranformed output
	 * @see Util
	 */
	public static String transformMutiNumber(String input){	
		Pattern p = Pattern.compile("(\\d*\\.?\\d+[\\-\\/])");
		Matcher m = p.matcher(input);
	
		if(m.find()){
			String part = m.group(1).substring(0, m.group(1).length()-1);
			input = m.replaceAll(part+" ");
			
		}
		return input;
	}
	
	
	
	/**
	 * Transform "x%" to "0.xx"
	 * 
	 * @param field "input"
	 * @return tranformed output
	 * @see Util
	 */
	public static String transformPercentage(String input){	
		Pattern p = Pattern.compile("(\\d*\\.?\\d*[ ]*%)");
		Matcher m = p.matcher(input);
		java.text.DecimalFormat df=new java.text.DecimalFormat("##.######");
		if(m.find()){
			String perc = m.group(1).substring(0, m.group(1).length()-1);
			if(isNumeric(perc))
				input = m.replaceAll(df.format(Float.parseFloat(perc)/100));
			
		}
		return input;
	}
	
	
	/**
	 * Normalize drug form
	 * 
	 * @param field "form" in the output e.g. "tab","tabs"
	 * @return normalized output in RXNOMR e.g. "oral tablet"
	 * @see Util
	 */

	public String normalizeForm(String form) {
		if(form.trim().equals(""))
			return "";
		if(normalization_map.containsKey(form)){
			return normalization_map.get(form);
		}
		
		return form;
	}
	
	public static boolean isNumeric(String str)  
	{  
	  try  
	  {  
	    double d = Double.parseDouble(str);  
	  }  
	  catch(NumberFormatException nfe)  
	  {  
	    return false;  
	  }  
	  return true;  
	}
	
	/**
	 * Normalize dose
	 * 
	 * @param field "form" in the output e.g. "1,200 mg"
	 * @return normalized output in RXNOMR e.g. "1200mg"
	 * @see Util
	 */

	public static String normalizeDose(String dose) {
		
		if(dose.trim().equals(""))
			return "";
		//System.out.println("dose:"+dose);
		dose = dose.replace("milligrams", "mg").replace("milligram", "mg");
		dose = dose.replaceAll("[ ]*\\,[ ]*", "");	
		
		java.text.DecimalFormat df=new java.text.DecimalFormat("##.######");
		//transfer mcg to mg
		Pattern p = Pattern.compile("\\d*\\.?\\d*mcg");
		Matcher m = p.matcher(dose);
		//System.out.println(dose);
		if (m.find()) {
			//if(dose.startsWith("50"))
			//	System.out.println(m.group(0));
			String dose_mcg = m.group(0).substring(0, m.group(0).length()-3).trim();
			if(isNumeric(dose_mcg)){
				
				String dd =df.format(Float.parseFloat(dose_mcg)*0.001);
				dose = dose.replace(m.group(0), dd+"mg");
			}
			//if(dose.startsWith("50")){
			//	System.out.println(dose_mcg);
			//	System.out.println(norm_dose);
			//}
		}
		
		/*
		if(dose.matches("-?\\d+(\\.\\d+)?")){
			dose = dose.trim()+"mg";
		}*/
		
		if(dose.matches("\\d*\\.?\\d*%")){		
			String perc = dose.trim().substring(0,dose.trim().length()-1);
			if(isNumeric(perc)){
				//java.text.DecimalFormat df=new java.text.DecimalFormat("##.######");
				return df.format(Float.parseFloat(perc)/100);
				
			}
		}
		//System.out.println("dose"+dose);
		
		return dose;
	}
	
	
	/**
	 * Split dose number and unit
	 * 
	 * @param field "dose" in the output e.g. "1,200 mg"
	 * @return [dose_num, dose_unit] e.g. ["1200", "mg"]
	 * @see Util
	 */
	public String[] getdoseNumberUnit(String dose) throws Exception{
		try{
			
			String[] items = dose.split("/");
			if(items.length != 2){
				return getSingledoseNumberUnit(dose);
			}
			
			//"120mg/5ml -> ["24", "mg/ml"]
			String doseA[] = getSingledoseNumberUnit(items[0].trim());
			String doseB[] = getSingledoseNumberUnit(items[1].trim());
			String return_string[] = new String[2];
			//System.out.println("doseb:"+doseB[0]+"|");
			if(doseB[0].trim().equals("")  || !doseB[0].matches("\\d*\\.?\\d+") || doseA[0].trim().equals("")  || !doseA[0].matches("\\d*\\.?\\d+")){
				return getSingledoseNumberUnit(dose);
			}
			if(isNumeric(doseA[0]) && isNumeric(doseB[0])){
				return_string[0] = String.valueOf((int)(Float.parseFloat(doseA[0])/Float.parseFloat(doseB[0])));
				return_string[1] = doseA[1]+"/"+doseB[1];
				return return_string;
			}
			else{
				return getSingledoseNumberUnit(dose);
			}
		}
		catch(Exception e){
			throw new Exception(e);
		}
	
	}
	
	
	public String[] getSingledoseNumberUnit(String dose){
		//System.out.println("enter single dose");
		Pattern p = Pattern.compile("\\d*\\.?\\d*[ ]*\\-?[ ]*\\d*\\.?\\d+\\%?");
		Matcher m = p.matcher(dose);
		String dose_number = "";
		String dose_unit = "";
		if (m.find()) {
			//System.out.println("have number");
		    dose_number = m.group(0);
		    dose_unit = dose.substring(m.end(), dose.length());
		    if(normalization_map.containsKey(dose_unit)){
		    	dose_unit = normalization_map.get(dose_unit);
			}
		}
		//System.out.println("dose:"+dose);
		//System.out.println("dose_number:"+dose_number);
		//System.out.println("dose_unit:"+dose_unit);
		String return_string[] = new String[2];
		return_string[0] = dose_number;
		return_string[1] = dose_unit;
		//System.out.println("input: "+dose);
		//System.out.println("single dose reutrn:" + return_string[0]+"\t"+return_string[1]);
		return return_string;
		
	}
	public double getSimilarity(String[] string1, String[] string2){
		VectorSpaceModel vsm = new VectorSpaceModel();
		return vsm.getJaccardSimilarity(string1, string2);
	}
	
	public String stem(String[] strs){
		String return_string = ""; 
		for( String word: strs ) {
	            Stemmer stemmer = new Stemmer();    
	            for( int i = 0; i < word.length(); i++ ) {
	                stemmer.add( word.charAt( i ) );
	            }
	            stemmer.stem();
	            return_string += stemmer.toString().toLowerCase()+" ";
	        }
		
		return return_string.trim();
	
	}
	
	public double calculateStrengthSimilarity(String system_Strength, String rxnorm_Strength){
		if(rxnorm_Strength == "NULL"){
			rxnorm_Strength = "";
		}
		double score = 0.0;
		try{
			String temp1[] = getdoseNumberUnit(system_Strength);
			String temp2[] = getdoseNumberUnit(rxnorm_Strength);
			
			if(temp1[0].equals(temp2[0])){
				score += 1.6;
			}
			if(temp1[1].replace(" ", "").equals((temp2[1]).replace(" ","")) && !temp1[1].equals("")){
				score += 0.4;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return score;
	}
	
	
	
	public double calculateFormSimilarity(String system_Form, String rxnorm_Form){
		if(rxnorm_Form.equals("NULL")){
			rxnorm_Form = "";
		}
		if(system_Form.equals(rxnorm_Form)){
			if(!system_Form.equals("")){
				return 1.0 * getSimilarity(system_Form.split(" "), rxnorm_Form.split(" "));
			}
			else{
				return 0.8;
			}
		}
		else
			if((system_Form.indexOf(rxnorm_Form) >=0 || rxnorm_Form.indexOf(system_Form)>=0) && !system_Form.equals("") && !rxnorm_Form.equals("")){
				return 0.5;
			}
			return 0.0;
	}
	
	public double calculateDrugSimilarity(String system_drug, String rxnorm_drug){
		if(rxnorm_drug.equals("NULL")){
			rxnorm_drug = "";
		}
		if(system_drug.equals(rxnorm_drug)){
			if(!system_drug.equals("")){
				return 1.0 * getSimilarity(system_drug.split(" "), rxnorm_drug.split(" "));
			}
			else{
				return 0.8;
			}
		}
		else
			return 0.0;
	}
}
