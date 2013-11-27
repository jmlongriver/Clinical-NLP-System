/**
 * 
 */
package org.apache.UIMA.CPE.medex;

import java.io.File;
import java.io.IOException;


import org.apache.uima.UIMAFramework;
import org.apache.uima.collection.CollectionProcessingEngine;
import org.apache.uima.collection.metadata.CpeDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.util.XMLInputSource;

/**
 * @author ywu4
 *
 */
public class MedexUIMACPE {

	/**
	 * 
	 */
	private static String desc_fname = System.getProperty("user.dir") + File.separator + "desc" + File.separator + "UIMAMedexCPE.xml";
	public MedexUIMACPE() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws InvalidXMLException 
	 * @throws ResourceInitializationException 
	 */
	public static void main(String[] args) throws InvalidXMLException, IOException, ResourceInitializationException {
		
		CpeDescription cpeDesc = UIMAFramework.getXMLParser().
		        parseCpeDescription(new XMLInputSource(desc_fname));
		      
		//instantiate CPE
		CollectionProcessingEngine mCPE = UIMAFramework.produceCollectionProcessingEngine(cpeDesc);
		
		mCPE.process();
	}

}
