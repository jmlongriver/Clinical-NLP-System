
/* First created by JCasGen Tue Oct 30 14:40:19 CDT 2012 */
package org.apache.UIMA.medex;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Thu Nov 15 12:17:28 CST 2012
 * @generated */
public class Neccessity_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Neccessity_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Neccessity_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Neccessity(addr, Neccessity_Type.this);
  			   Neccessity_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Neccessity(addr, Neccessity_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = Neccessity.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.apache.UIMA.medex.Neccessity");
 
  /** @generated */
  final Feature casFeat_semantic_type;
  /** @generated */
  final int     casFeatCode_semantic_type;
  /** @generated */ 
  public String getSemantic_type(int addr) {
        if (featOkTst && casFeat_semantic_type == null)
      jcas.throwFeatMissing("semantic_type", "org.apache.UIMA.medex.Neccessity");
    return ll_cas.ll_getStringValue(addr, casFeatCode_semantic_type);
  }
  /** @generated */    
  public void setSemantic_type(int addr, String v) {
        if (featOkTst && casFeat_semantic_type == null)
      jcas.throwFeatMissing("semantic_type", "org.apache.UIMA.medex.Neccessity");
    ll_cas.ll_setStringValue(addr, casFeatCode_semantic_type, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public Neccessity_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_semantic_type = jcas.getRequiredFeatureDE(casType, "semantic_type", "uima.cas.String", featOkTst);
    casFeatCode_semantic_type  = (null == casFeat_semantic_type) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_semantic_type).getCode();

  }
}



    