package org.apache.medex.semantic_rules;

/** This class is bean class for semantic rules which contains get and set methods
 *  for variables used.
 * 
 * @author shaha2
 *
 */
public class result_bean 
{
	private String final_tag;
	private String default_tag;
	private int final_index;
	private int default_index;
	private String final_token;
	private String default_token;
	
	public int getFinal_index() {
		return final_index;
	}

	public void setFinal_index(int final_index) {
		this.final_index = final_index;
		//System.out.println("set_final_index :"+this.final_index);
	}

	public int getDefault_index() {
		return default_index;
	}

	public void setDefault_index(int default_index) {
		this.default_index = default_index;
	}

	public String getFinal_token() {
		//System.out.println("getFinal_token :"+this.final_token);
		return this.final_token;
	}

	public void setFinal_token(String final_token) {
		
		//System.out.println("b4 setFinal_token :"+final_token);
		this.final_token = final_token;
		//System.out.println("after setFinal_token :"+this.final_token);
	}

	public String getDefault_token() {
		return default_token;
	}

	public void setDefault_token(String default_token) {
		this.default_token = default_token;
		
		//this.final_token  = default_token.split();
	}

	
	

	public String getFinal_tag() {
		//System.out.println("final_tag :"+this.final_tag);
		return this.final_tag;
	}

	public void setFinal_tag(String final_tag) {
		
		this.final_tag = final_tag;
		//System.out.println("final_tag :"+this.final_tag);
		
	}

	public String getDefault_tag() {
		//System.out.println("default_tag :"+this.default_tag);
		return this.default_tag;
	}

	public void setDefault_tag(String default_tag) {
		//System.out.println("default_tag :"+default_tag);
		this.final_tag = default_tag.split("-")[0];
		//System.out.println("final_tag :"+this.final_tag );
	}

}
