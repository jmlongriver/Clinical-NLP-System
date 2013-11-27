/**
 * 
 */
package org.apache.UIMA.medex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.Vector;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;


import org.apache.NLPTools.Document;
import org.apache.NLPTools.Global;
import org.apache.NLPTools.SentenceBoundary;
import org.apache.NLPTools.Global.SuffixArrayResult;
import org.apache.NLPTools.Global.TextSectionType;
import org.apache.algorithms.SuffixArray;
import org.apache.medex.DrugTag;
import org.apache.medex.Lexicon;

import org.apache.medex.MedTagger;

public class DrugAnnotator extends JCasAnnotator_ImplBase {

	private String rxnorm_file ;
	private String code_file;
	private String input_dir;
	private String output_dir;
	private String grammar_file;		
	private String generic_file;	
	private String word_file;
	private String abbr_file;
	private String lex_file;
	private String norm_file;
	
	//private Lexicon lex;
	//private SentenceBoundary sb;
	private MedTagger med;

	/**
	 * 
	 * 
	 */
	public DrugAnnotator() {
		// TODO Auto-generated constructor stub
		this.rxnorm_file = System.getProperty("user.dir") + File.separator + "resources" + File.separator + "brand_generic.cfg";
		this.code_file = System.getProperty("user.dir") + File.separator + "resources" + File.separator + "code.cfg";
		this.generic_file = System.getProperty("user.dir") + File.separator + "resources" + File.separator + "rxcui_generic.cfg";
		this.input_dir = System.getProperty("user.dir") + File.separator + "input";
		this.output_dir = System.getProperty("user.dir") + File.separator + "output";
		this.grammar_file = System.getProperty("user.dir") + File.separator + "resources" + File.separator + "grammar.txt";		
		
		this.word_file = System.getProperty("user.dir") + File.separator + "resources" + File.separator + "word.txt";
		this.abbr_file = System.getProperty("user.dir") + File.separator + "resources" + File.separator + "abbr.txt";
		this.lex_file = System.getProperty("user.dir") + File.separator + "resources" + File.separator + "lexicon.cfg";
		this.norm_file = System.getProperty("user.dir") + File.separator + "resources" + File.separator + "norm.cfg";
		
		try{
			//this.lex=new Lexicon(lex_file);
			//this.sb=new SentenceBoundary(this.word_file,this.abbr_file);
			this.med = new MedTagger(lex_file, rxnorm_file, code_file, generic_file, input_dir, output_dir, word_file, abbr_file, grammar_file, "y", norm_file, "n","y","y");
		}
		catch (Exception e){
			System.err.println("Error: " + e.getMessage());	 
		}
		
		
	}

	/* (non-Javadoc)
	 * @see org.apache.uima.analysis_component.JCasAnnotator_ImplBase#process(org.apache.uima.jcas.JCas)
	 */
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		// TODO Auto-generated method stub
		
	    int count=0;
	    try{
	    	
	    	String docText = aJCas.getDocumentText();
		    
	    	//docText = docText.replace("\\p{Cntrl}", "");
		    
			
			
		
	        
			Document doc=new Document(docText,"uima_file_"+count);
			
			med.medtagging(doc);
			
			
			for(int i=0;i<doc.signature().size();i++){
				Vector<DrugTag> dt_vct=doc.signature().get(i);
				int begain=Integer.MAX_VALUE;
				int end=-1;
				for (int j=0;j<dt_vct.size();j++){
					DrugTag dt=dt_vct.get(j);
					
					if (dt.startPos() <begain){
						begain=dt.startPos();
					}
					if(dt.endPos()>end){
						end=dt.endPos();
					}
					if(dt.type()==Global.TextSectionType.DRUG){
						Drug annotate=new Drug(aJCas);
			    		annotate.setBegin(dt.startPos());		    
			    		annotate.setEnd(dt.endPos());	
			    		annotate.setSemantic_type("");
			    		annotate.addToIndexes();
					}
					else if(dt.type()==Global.TextSectionType.FREQUENCY){
						Frequency annotate=new Frequency(aJCas);
			    		//System.out.println("add freq");
			    		annotate.setBegin(dt.startPos());		    
			    		annotate.setEnd(dt.endPos());	
			    		annotate.setSemantic_type("");
			    		annotate.addToIndexes();
					}
					else if(dt.type()==Global.TextSectionType.BRAND){
						BrandName annotate=new BrandName(aJCas);
			    		///System.out.println("add brand name");
			    		annotate.setBegin(dt.startPos());		    
			    		annotate.setEnd(dt.endPos());	
			    		annotate.setSemantic_type("");
			    		annotate.addToIndexes();
					}
					else if(dt.type()==Global.TextSectionType.DOSE){
						DoseAmount annotate=new DoseAmount(aJCas);
			    		//System.out.println("add dose amount");
			    		annotate.setBegin(dt.startPos());		    
			    		annotate.setEnd(dt.endPos());	
			    		annotate.setSemantic_type("");
			    		annotate.addToIndexes();
					}
					else if(dt.type()==Global.TextSectionType.DURATION){
						Duration annotate=new Duration(aJCas);
			    		//System.out.println("add duration");
			    		annotate.setBegin(dt.startPos());		    
			    		annotate.setEnd(dt.endPos());	
			    		annotate.setSemantic_type("");
			    		annotate.addToIndexes();
					}
					else if(dt.type()==Global.TextSectionType.FORM){
						Form annotate=new Form(aJCas);
			    		//System.out.println("add form");
			    		annotate.setBegin(dt.startPos());		    
			    		annotate.setEnd(dt.endPos());	
			    		annotate.setSemantic_type("");
			    		annotate.addToIndexes();
					}
					else if(dt.type()==Global.TextSectionType.NECCESSITY){
						Neccessity annotate=new Neccessity(aJCas);
			    		//System.out.println("add neccessity");
			    		annotate.setBegin(dt.startPos());		    
			    		annotate.setEnd(dt.endPos());	
			    		annotate.setSemantic_type("");
			    		annotate.addToIndexes();
					}
					else if(dt.type()==Global.TextSectionType.ROUTE){
						Route annotate=new Route(aJCas);
			    		//System.out.println("add route");
			    		annotate.setBegin(dt.startPos());		    
			    		annotate.setEnd(dt.endPos());	
			    		annotate.setSemantic_type("");
			    		annotate.addToIndexes();
					}
					else if(dt.type()==Global.TextSectionType.STRENGTH){
						Strength annotate=new Strength(aJCas);
			    		//System.out.println("add strenthe");
			    		annotate.setBegin(dt.startPos());		    
			    		annotate.setEnd(dt.endPos());	
			    		annotate.setSemantic_type("");
			    		annotate.addToIndexes();
					}
				}
				
				
				
			}
			
		    
	    }
	    catch (Exception e){
			System.err.println("Error: " + e.getMessage());	 
		}
	    
	    
	    
	    

	}

}
