

/* First created by JCasGen Tue Oct 30 14:40:19 CDT 2012 */
package org.apache.UIMA.medex;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Thu Nov 15 12:17:28 CST 2012
 * XML source: /Users/ywu4/coding/eclipse/MedEx_UIMA/desc/UIMAMedexDrugAnnotatorDescriptor.xml
 * @generated */
public class Neccessity extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(Neccessity.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Neccessity() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Neccessity(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Neccessity(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Neccessity(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {}
     
 
    
  //*--------------*
  //* Feature: semantic_type

  /** getter for semantic_type - gets 
   * @generated */
  public String getSemantic_type() {
    if (Neccessity_Type.featOkTst && ((Neccessity_Type)jcasType).casFeat_semantic_type == null)
      jcasType.jcas.throwFeatMissing("semantic_type", "org.apache.UIMA.medex.Neccessity");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Neccessity_Type)jcasType).casFeatCode_semantic_type);}
    
  /** setter for semantic_type - sets  
   * @generated */
  public void setSemantic_type(String v) {
    if (Neccessity_Type.featOkTst && ((Neccessity_Type)jcasType).casFeat_semantic_type == null)
      jcasType.jcas.throwFeatMissing("semantic_type", "org.apache.UIMA.medex.Neccessity");
    jcasType.ll_cas.ll_setStringValue(addr, ((Neccessity_Type)jcasType).casFeatCode_semantic_type, v);}    
  }

    