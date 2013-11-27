package org.apache.NLPTools.CFGparser;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Iterator;
import java.util.Set;

import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.javatuples.Quintet;

public class TreeNode{
	protected String label;
	protected boolean isLeaf;
	protected String leaf_label;
	protected String origtext;
	protected TreeNode parent;
	protected ArrayList<TreeNode> children;
	Pair<String,String> leaf_label_pair;
	ArrayList<Pair<String,String>> leaf_label_array = new ArrayList<Pair<String, String>>();
	public static ArrayList<String> drugList1;
	public static ArrayList<String> drugList1_dgmsig;
	public static ArrayList<String> drugList1_andsig;
	public static ArrayList<String> drugList1_dgssig;
	public static ArrayList<String> leaves_tag;
	public static int leaf_counter;
	public static String dglist_str;
	public static String dglist_str_dgmsig;
	public static String dglist_str_andsig;
	public static String dglist_str_thensig;
	
	
	
	public TreeNode(){
		children = new ArrayList<TreeNode>();
		parent = null;
		isLeaf = false;
		drugList1 = new ArrayList();
		drugList1_dgssig = new ArrayList();
		drugList1_dgmsig = new ArrayList();
		drugList1_andsig = new ArrayList();
		leaves_tag = new ArrayList<String>();
		leaf_counter = 0;
		dglist_str = "";
	}
	
	
	public void preOrder(int i){
		if(!isLeaf){
			for(int j=0; j<i; j++){
				System.out.print("  ");
			}
			System.out.println("(" + this.label);
		}
		else{
			for(int j=0; j<i; j++){
				System.out.print("  ");
			}
			
			System.out.print("(" + this.label);
			System.out.println(" "+this.leaf_label);
		}
		
		if(this.children.size() >0){
			for(int k= children.size()-1; k>=0;k--){
				children.get(k).preOrder(i+1);
			}
		}
		
		for(int j=0; j<i; j++){
			System.out.print("  ");
		}
		
		System.out.println(")");
		
	}
	
	public boolean isLeaf() {
		return isLeaf;
	}


	public void setLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}


	public String getLeaf_label() {
		return leaf_label;
	}


	public void setLeaf_label(String leaf_label) {
		this.leaf_label = leaf_label;
	}


	public TreeNode(String label, String origtext){
		this();
		this.label = label;
		this.origtext = origtext;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getOrigtext() {
		return origtext;
	}
	public void setOrigtext(String origtext) {
		this.origtext = origtext;
	}
	public TreeNode getParent() {
		return parent;
	}
	public void setParent(TreeNode parent) {
		this.parent = parent;
	}
	public ArrayList<TreeNode> getChildren() {
		return children;
	}
	public void setChildren(ArrayList<TreeNode> children) {
		this.children = children;
	}
	
	public void preOrder_tree_mapping(int i,ArrayList tags1){
		if(!isLeaf){
			for(int j=0; j<i; j++){
				System.out.print("  ");
			}
			System.out.println("(" + this.label);
		}
		else{
			for(int j=0; j<i; j++){
				System.out.print("  ");
			}
			
			System.out.print("(" + this.label);
			System.out.println(" "+this.leaf_label);
		}
		
		if(this.children.size() >0){
			for(int k= children.size()-1; k>=0;k--){
				children.get(k).preOrder(i+1);
			}
		}
		
		for(int j=0; j<i; j++){
			System.out.print("  ");
		}
		
		System.out.println(")");
	}
	
	public void tree_mapping(int i,ArrayList tags1){
		
		//System.out.println(" tree_mapping "+tags1.size());
		int count = 0;
			if(isLeaf){
				
					Pair<Quintet<String, String, Integer, Pair<Pair<String,Integer> , Integer>, Integer>, Integer> pair_b3  = (Pair<Quintet<String, String, Integer, Pair<Pair<String, Integer>, Integer>, Integer>, Integer>) tags1.get(leaf_counter);
					String tag = pair_b3.getValue0().getValue1();
					String term = pair_b3.getValue0().getValue0();
					
					this.leaf_label =  term;
					//System.out.println(leaf_counter+" "+this.label +"-"+this.leaf_label);
					count++;
					leaf_counter++;
			}
			
			
		
	if(this.children.size() >0){
		for(int k= children.size()-1; k>=0;k--){
			children.get(k).tree_mapping(i+1,tags1);
		}
	}
	
	
}
		public void preOrder_get_node_out(int i){
		
		//System.out.println("preOrder_get_node_out");
		
		
		if(!isLeaf){
			
			//System.out.println("Node label" + this.label);
			
		}
		
				
		if(this.children.size() >0){
			
			for(int k= children.size()-1; k>=0;k--){
				
				if(children.get(k).label.equals("DGSSIG"))
				{
					//System.out.println(" ** "+children.get(k).label);
					 children.get(k).preOrder_get_subtree(i+1);
					
				}
				else
				{
					//System.out.println(" ** "+children.get(k).label);
					
					children.get(k).preOrder_get_node_out(i+1);
				}
			}
		}
		//System.out.println("!! druglist1 size :"+drugList1.size());
		
}

	
public void preOrder_get_subtree(int i){
		
		//System.out.println("preOrder_get_subtree");
		if(isLeaf){
			for(int j=0; j<i; j++){
				System.out.print(" ");
			}
			//System.out.println("label" + this.label);
			//System.out.println("leaf_label"+this.leaf_label);
			this.drugList1.add(this.leaf_label);
		}
		
		
		if(this.children.size() >0){
			//System.out.println("children.size() "+children.size());
			for(int k= children.size()-1; k>=0;k--){
				
				//if(children.get(k).label.equals("DGSSIG"))
				{
					//System.out.println(" ** "+children.get(k).label);
					children.get(k).preOrder_get_subtree(i+1);
				}
			}
		}
		
		
} // end of preOrder_get_subtree
	
public void preOrder_test(int i){
	if(!isLeaf){
		for(int j=0; j<i; j++){
			System.out.print("  ");
		}
		System.out.println("(" + this.label);
	}
	/*else{
		for(int j=0; j<i; j++){
			System.out.print("  ");
		}
		
		System.out.print("(" + this.label);
		System.out.println(" "+this.leaf_label);
	}*/
	
	if(this.children.size() >0){
		for(int k= children.size()-1; k>=0;k--){
			children.get(k).preOrder(i+1);
		}
	}
	
	for(int j=0; j<i; j++){
		System.out.print("  ");
	}
	
	System.out.println(")");
	
}

public void traverse_tree(TreeNode treenode, int index)
{
	
	//System.out.println("subtree");
	ArrayList<TreeNode> node = treenode.getChildren();
	if(node.size() > 0)
	{
		//System.out.println(node.size());
		for(int i = 0; i< node.size(); i++)
		{
			
			//System.out.println(node.size());
			//String str = text+" "+node.get(i).getLabel();
			for(int j = 0;j<= index;j++)
				System.out.print(" ");
			
			System.out.println(node.get(i).getLabel());
			//System.out.println(node.get(i).g);
			
			TreeNode n = node.get(i);
			traverse_tree(n,index+1);
		}
		
	}
	
	
}

public void get_leaves(TreeNode treenode)
{
	//System.out.println("get_leaves");
	ArrayList<TreeNode> node = treenode.getChildren();
	//ArrayList<String> leaves_tag = new ArrayList<String>();
	
	if(node.size() > 0)
	{
		for(int i = 0; i< node.size(); i++)
		{
			if(node.get(i).getChildren().size() == 0)
			{
				//String leaf_label = node.get(i).leaf_label.split(" ")[1];
				
				
				//System.out.print(node.get(i).label+" FFF "+node.get(i).leaf_label+"\t");
				dglist_str = dglist_str + "\t"+node.get(i).label+" FFF "+node.get(i).leaf_label;
				//drugList1.add(node.get(i).label+" FFF "+leaf_label+"\t");
				leaves_tag.add(node.get(i).label);
			}
			
			TreeNode n = node.get(i);
			get_leaves(n);		
			
		}
	}
	
	//System.out.print(" dglist_str : "+ dglist_str);
	//return dglist_str;
	
}

public void map_leaves(ArrayList tags1)
	{
		Set<String> leaves_tag_set = new HashSet<String>(leaves_tag);
		

		for(int  i = 0; i< tags1.size(); i++)
		{
			Pair<Quintet<String, String, Integer, Pair<Pair<String,Integer> , Integer>, Integer>, Integer> pair_b3  = (Pair<Quintet<String, String, Integer, Pair<Pair<String, Integer>, Integer>, Integer>, Integer>) tags1.get(i);
			String tag = pair_b3.getValue0().getValue1();
			String term = pair_b3.getValue0().getValue0();
			
			//System.out.println("size :"+leaves_tag_set.size());
			Iterator iter = leaves_tag_set.iterator();
		    while (iter.hasNext()) {
		      //System.out.println("**"+iter.next());
		      
		      String tag_term = (String) iter.next();
		      String tag1 = tag_term.split("\t")[0];
		      String term1 = tag_term.split("\t")[1];
		      
		      if(tag.equals(tag1) && term.equals(term1))
		      {
		    	 // System.out.println("** "+tag+"\t"+term);
		    	  String t = tag + " FFF " + term;
		    	  drugList1.add(t);
		      }
		      
		    }
		}
		
			
			/*Pair<Quintet<String, String, Integer, Pair<Pair<String,Integer> , Integer>, Integer>, Integer> pair_b3  = (Pair<Quintet<String, String, Integer, Pair<Pair<String, Integer>, Integer>, Integer>, Integer>) tags1.get(i);
			String tag = pair_b3.getValue0().getValue1();
			String term = pair_b3.getValue0().getValue0();
			System.out.println("**"+leaves_tag.get(i)+"\t"+tag);
			
			//System.out.println("**"+leaves_tag.get(i));
			
			if(leaves_tag.get(i).equals(tag))
			{
				System.out.println("**"+leaves_tag.get(i)+"\t"+tag+"\t"+term);
			}*/
		}
	
public void getNodeOut(TreeNode treenode)
{
	
	//System.out.println(" getNodeOut ");
	ArrayList<TreeNode> node = treenode.getChildren();
	if(node.size() > 0)
	{
		//System.out.println(node.size());
		for(int i = 0; i< node.size(); i++)
		{
			TreeNode n = node.get(i);
			//System.out.println("** "+n.getLabel());
			if(n.getLabel().equals("DGSSIG"))
			{
				//System.out.println("in if");
				//System.out.println(node.get(i).getLabel());
				//System.out.println();
				//get_leaves(n,dglist_str);
				dglist_str = "";
				get_leaves(n);
				drugList1_dgssig.add(dglist_str.trim());
				//System.out.println("dglist_str "+dglist_str);
			} // end of DGSSIG
			
			if(n.getLabel().equals("DGMSIG"))
			{
				
				//System.out.println("in if");
				//System.out.println(node.get(i).getLabel());
				//System.out.println();
				//get_leaves(n,dglist_str);
				dglist_str = "";
				get_leaves(n);
				drugList1_dgmsig.add(dglist_str.trim());
				//System.out.println("drugList1 dgmsig:"+drugList1_dgmsig);

				} // end of DGMSIG
		
			if(n.getLabel().equals("ANDSIG"))
			{
				
				//System.out.println("in if");
				//System.out.println(node.get(i).getLabel());
				//System.out.println();
				//get_leaves(n,dglist_str);
				dglist_str = "";
				get_leaves(n);
				drugList1_andsig.add(dglist_str.trim());
				//System.out.println("drugList1 andsig:"+drugList1_andsig);
				/*dglist_str_andsig = "";

				for (String s : drugList1)
				{
					dglist_str_andsig += s + "|";
				}

				System.out.println("dglist_str_andsig "+dglist_str_andsig);*/
				
			} // end of ANDSIG	
			
			
			getNodeOut(n);
		} // end of for node.size()
				
	
			
	} // end of if node.size()

} // end getNodeOut method
		

	
}
