package org.apache.NLPTools.NER;
import org.apache.NLPTools.Document;
import java.util.Properties;
/**
 * An abstract class to represent features in CRF,
 * extend this class to create new features
 * 
 * @author Min Jiang
 */

public abstract class Feature {
	public abstract String createFeatureInstance(Document doc, Properties prop);
}
