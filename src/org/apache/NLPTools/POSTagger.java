package org.apache.NLPTools;

import opennlp.tools.postag.*;
import opennlp.tools.util.*;
import opennlp.tools.util.model.*;
import opennlp.tools.postag.POSDictionary;
import opennlp.tools.postag.TagDictionary;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import antlr.StringUtils;
public class POSTagger {

	/**
	 * @param args
	 */
	
	public String model_path =  "resources"+File.separator+"POS"+File.separator+ "discharge_mipacq.model";
	public String train_path = "resources"+File.separator+"POS"+File.separator+"train_sample.txt";
	
	public POSTagger(String train_path, String model_path){
		this.model_path = model_path;
		this.train_path = train_path;
	}
	
	public POSTagger(){

	}
	
	public POSDictionary createDictionary(){
		POSDictionary dictionary = null;
		try{
			boolean isCaseSensitive = true;
			InputStream dataIn = new FileInputStream(train_path);
			dictionary = new POSDictionary(isCaseSensitive);
			dictionary.create(dataIn);

				    
		}
		catch (IOException e) {
			  // Failed to read or parse training data, training failed
			  e.printStackTrace();
		}
		return dictionary;
	}
	
	public String[] tagging(String[] sent){
		String[] tags = new String[sent.length];
		try{
			InputStream  modelIn = new FileInputStream(model_path);
			POSModel trainmodel = new POSModel(modelIn);
			POSTaggerME tagger = new POSTaggerME(trainmodel);
			
			tags = tagger.tag(sent);
		}
		catch(IOException e){
			e.printStackTrace();
		}
		return tags;
	}
	
	public void train() {
		POSModel model = null;

		InputStream dataIn = null;
		try{
			 dataIn = new FileInputStream(train_path);
			 ObjectStream<String> lineStream = new PlainTextByLineStream(dataIn, "UTF-8");
			 ObjectStream<POSSample> sampleStream = new WordTagSampleStream(lineStream);
			 TrainingParameters trainParams = new TrainingParameters();
			 //tp.put(tp.ALGORITHM_PARAM, value)
			 trainParams.put("model", ModelType.MAXENT.name()); ;
			 model = POSTaggerME.train("en", sampleStream, trainParams, createDictionary(), null); 
			 OutputStream modelOut = new BufferedOutputStream(new FileOutputStream(model_path));
			 model.serialize(modelOut);
		}
		catch (IOException e) {
		  // Failed to read or parse training data, training failed
		  e.printStackTrace();
		}
		finally {
		  if (dataIn != null) {
		    try {
		      dataIn.close();
		    }
		    catch (IOException e) {
		      // Not an issue, training already finished.
		      // The exception should be logged and investigated
		      // if part of a production system.
		      e.printStackTrace();
		    }
		  }
		}
	}

}
