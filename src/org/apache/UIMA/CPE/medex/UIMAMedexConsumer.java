/**
 * 
 */
package org.apache.UIMA.CPE.medex;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.examples.SourceDocumentInformation;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.XMLSerializer;
import org.xml.sax.SAXException;

/**
 * @author ywu4
 *
 */
public class UIMAMedexConsumer extends CasConsumer_ImplBase {
	public static final String PARAM_OUTPUTDIR = "OutputDirectory";

	  
	private File mOutputDir;

	  
	private int mDocNum;

	 
	public void initialize() throws ResourceInitializationException {
	    
		mDocNum = 0;
	    mOutputDir = new File((String) getConfigParameterValue(PARAM_OUTPUTDIR));
	    if (!mOutputDir.exists()) {
	      mOutputDir.mkdirs();
	    }
	  }

	/**
	 * 
	 */
	public UIMAMedexConsumer() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.apache.uima.collection.base_cpm.CasObjectProcessor#processCas(org.apache.uima.cas.CAS)
	 */
	public void processCas(CAS aCAS) throws ResourceProcessException {
		// TODO Auto-generated method stub
		String modelFileName = null;

	    JCas jcas;
	    try {
	      jcas = aCAS.getJCas();
	    } catch (CASException e) {
	      throw new ResourceProcessException(e);
	    }

	    // retreive the filename of the input file from the CAS
	    FSIterator it = jcas.getAnnotationIndex(SourceDocumentInformation.type).iterator();
	    File outFile = null;
	    if (it.hasNext()) {
	      SourceDocumentInformation fileLoc = (SourceDocumentInformation) it.next();
	      File inFile;
	      try {
	        inFile = new File(new URL(fileLoc.getUri()).getPath());
	        String outFileName = inFile.getName();
	        if (fileLoc.getOffsetInSource() > 0) {
	          outFileName += ("_" + fileLoc.getOffsetInSource());
	        }
	        outFileName += ".xmi";
	        outFile = new File(mOutputDir, outFileName);
	        modelFileName = mOutputDir.getAbsolutePath() + "/" + inFile.getName() + ".ecore";
	      } catch (MalformedURLException e1) {
	        // invalid URL, use default processing below
	      }
	    }
	    if (outFile == null) {
	      outFile = new File(mOutputDir, "doc" + mDocNum++ + ".xmi");     
	    }
	    // serialize XCAS and write to output file
	    try {
	      writeXmi(jcas.getCas(), outFile, modelFileName);
	    } catch (IOException e) {
	      throw new ResourceProcessException(e);
	    } catch (SAXException e) {
	      throw new ResourceProcessException(e);
	    }

	}
	
	 private void writeXmi(CAS aCas, File name, String modelFileName) throws IOException, SAXException {
		    FileOutputStream out = null;

		    try {
		      // write XMI
		      out = new FileOutputStream(name);
		      XmiCasSerializer ser = new XmiCasSerializer(aCas.getTypeSystem());
		      XMLSerializer xmlSer = new XMLSerializer(out, false);
		      ser.serialize(aCas, xmlSer.getContentHandler());
		    } finally {
		      if (out != null) {
		        out.close();
		      }
		    }
		  }

}
