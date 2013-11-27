package org.apache.NLPTools.NER;

import java.io.File;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		//identify the current operating system and choose the command accordingly
		String location;
		
		if(System.getProperty("os.name").startsWith("Windows")){
			location = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath().replace("/", "\\");
			location = location.substring(1, location.length()-4);
		}
		else
		{
			location = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			location = location.substring(1, location.length()-4);
		}
		
		//read configuration file
		Properties prop = new Properties();
        try
        {
            // the configuration file name
            String fileName = location +File.separator+ "resources" + File.separator + "CRF" + File.separator +"para.config";;            
            InputStream is = new FileInputStream(fileName);
            // load the properties file
            prop.load(is);

        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
		
		
		//create an instance of CRF classifier
		String template_file = location +File.separator+ "resources" + File.separator + "CRF" + File.separator +"template_test.txt";
		CRF_classifier crf = new CRF_classifier(System.getProperty("os.name"), location, template_file, prop);
		
		//set input and output folders
		//String training_file = location + "resources"+ File.separator + "CRF" + File.separator +"train_feature.txt";
		String test_dir = location +File.separator+"test_ner_input";
		String output_dir =  location + "chem_output";
		String training_file =  location+ "\\resources"+ File.separator + "CRF" + File.separator +"chem_feature_2.txt";
		
		//generate I2b2 training corpus
		//System.out.println("Generate training corpus ...");
		//crf.generateI2b2Training("C:\\vandy\\Project\\I2B2_2010\\DATA\\all\\txt\\", location+ "\\resources"+ File.separator + "CRF" + File.separator +"train_feature.txt");
		//crf.generateChemTraining2("C:\\working\\CHEM_NER\\test_sent\\test_sent2\\", location+ "\\resources"+ File.separator + "CRF" + File.separator +"chem_test_feature.txt");
		//train based on training corpus
		//System.out.println("Start training ...");
		//crf.train(training_file);
		
		//predict on test data
		System.out.println("Start prediction ...");
		crf.predict(test_dir, output_dir);
	}

}
