package org.apache.algorithms;
import java.io.*;
import org.apache.NLPTools.*;

/**
 * An interface of classifier
 * 
 * @author Min Jiang
 *
 */
public interface Classifier {
	
	public String generatefeature(Document doc);
	
	public void train(String trainingFile);
	
	public void predict(String testFile, String outputFile);
	
}
