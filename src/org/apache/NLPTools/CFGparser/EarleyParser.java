package org.apache.NLPTools.CFGparser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.HashMap;

import org.javatuples.Pair;

/**
* @author Min Jiang
* @version 1.0
* @since 2011-7-1
*/
public class EarleyParser {
   /**
	 * This class contains the main logic of Earley parser
	 *
	 */
	protected String[] words; 
	protected ArrayList<EarleyChart> charts;
	//protected Queue processingEntryList;
	protected EarleyEntry startEntry;
	protected String startRule;
	protected Grammar grammar;
	/**
	  * Constructor of class EarleyParser with three parameters 
	  * @param sentence	sentence need to be parsed
	  * @param grammar	name of grammar file	 
	  * @param startRule	start production rule of grammar
	  */
	public EarleyParser(String sentence, Grammar grammar, String startRule){
		startEntry = new EarleyEntry();
		charts = new ArrayList<EarleyChart>();
		//processingEntryList = new LinkedList<EarleyEntry>();
		this.startRule = startRule;
		//System.out.print("call parser");
		//grammar = new TestGrammar(GrammarFile);
		this.grammar = grammar;
		this.words = sentence.split(" ");
	}
	
	/**
	  * Initialize procedure of parser: 
	  * 1> build the chart structure in the parser 
	  * 2> append the start rule into the first chart 
	  * @param startRule	start production rule of grammar
	  */
	protected void init(String startRule){
		//create the first chart
		EarleyChart chart = new EarleyChart();
		chart.setChart_index(0);
		//set start entry
		//startEntry.setDot_pos(0);
		startEntry.setItemlist(startRule);	
		HashMap<Integer, EarleyEntry> entrymap = new HashMap<Integer, EarleyEntry>();
		entrymap.put(startEntry.toString2().hashCode(), startEntry);
		chart.setEntryMap(entrymap);
		ArrayList<EarleyEntry> entrylist = new ArrayList<EarleyEntry>();
		entrylist.add(startEntry);
		chart.setEntryList(entrylist);
		charts.add(chart);
		
		//System.out.println("Charts size "+charts.size());
		//processingEntryList.add(startEntry);
	}
	
	/**
	  * build parseTree based on the chart during the parsing 
	  * @return root of parsed tree
	  */
	public TreeNode buildParsedTree(){
		//build parsed tree based on charts
		EarleyChart lastChart = charts.get(charts.size()-1);
		ArrayList<EarleyEntry> entrylist = lastChart.getEntryList(); 
		Iterator<EarleyEntry> itr = entrylist.iterator();
		TreeNode root = new TreeNode("S", "");
		
		while(itr.hasNext()){			
			EarleyEntry cur_entry = itr.next();
			if(cur_entry.getstart_pos()==0 &&
					cur_entry.getend_pos() == words.length &&
					cur_entry.getHead().getSign().equals("S")){
				
				LinkedList<EarleyEntry> entry_stack = new LinkedList<EarleyEntry>();
				LinkedList<TreeNode> node_stack = new LinkedList<TreeNode>();
				entry_stack.add(cur_entry);
				node_stack.add(root);
				
				while(!entry_stack.isEmpty() && !node_stack.isEmpty() ){
					EarleyEntry top = entry_stack.remove();
					TreeNode cur_node = node_stack.remove();
					//System.out.println(top.toString2());
					top.adjustAncestor(grammar);
					ArrayList<EarleyEntry> ac = top.getAncestor();
					Iterator<EarleyEntry> acitr = ac.iterator();
					ArrayList<TreeNode> childlist = new ArrayList<TreeNode>();				
					while(acitr.hasNext()){
						EarleyEntry ac_entry = acitr.next();
						//System.out.println(ac_entry.toString2() + ac_entry.ifBottom());
						TreeNode node = new TreeNode(ac_entry.getHead().getSign(), "");
						
						if(grammar.isPOSTAG(ac_entry.getHead().getSign())){//if current entry is at the bottom of the parse tree
							node.setLeaf(true);
							node.setLeaf_label(ac_entry.getItemlist().get(2).getSign());
						}
						
						childlist.add(node);
						node.setParent(cur_node);
				    	entry_stack.add(ac_entry);
				    	node_stack.add(node);
						
					}
					cur_node.setChildren(childlist);	
				}	
			}
			
		}
		return root;
	}
	
	/**
	  * main function of parse, call "predict", "scan", and "complete"
	  * to build chart 
	  * 1> build the chart structure in the parser 
	  * 2> append the start rule into the first chart 
	  */
	public void parse(){
		
		
		init(this.startRule);
		//printChart();
		//System.out.println("charts.size()" + charts.size());
		//System.out.println("words.length" + words.length);
		
		int chart_index = 0;
		//for each state
		long start = System.currentTimeMillis();
		long eachParseStart = start;
		long eachParseEnd;
		for (int i=0; i<=words.length; i++)
		{		
			//System.out.println("charts.size() :"+charts.size() +"-- i = "+i );
			if(charts.size() <= i){
				//if there is no new entry generated from previous chart, then exit
				//System.out.println("The sentence cannot be parsed!");
				return;
			}
			EarleyChart curChart = charts.get(i);
			ArrayList<EarleyEntry> entry_list = curChart.getEntryList();
			//int num_entry = entry_list.size();
			
			int j = 0;//index of current processing entry in the chart
			while(true){
				EarleyEntry cur_entry = entry_list.get(j);
				/*
				if(cur_entry.toString2().indexOf("what") >= 0){
					System.out.println(cur_entry.toString2());
				}*/
				if(!cur_entry.ifComplete()){
					//if current entry has not been processed yet 
					EntryItem nextItem = cur_entry.getNextItem();
					//System.out.println(nextItem.getSign());
					if (!grammar.isPOSTAG(nextItem.getSign())){
						//if nextitem is postag, call predict					
						long predictStart = System.currentTimeMillis();						
						predict(cur_entry, i);
						/*
						if((System.currentTimeMillis() - predictStart) >=15){
							System.out.print("The predict consumes ");
							
							System.out.println((System.currentTimeMillis() - predictStart));
							
						}*/
					    //printChart();
					}
					else{//else call scan method
						/*
						if(cur_entry.toString2().indexOf("what") >= 0){
							System.out.println(cur_entry.toString2());
						}*/
						//System.out.println(i);
						long scanStart = System.currentTimeMillis();
						scan(cur_entry, i);
						/*
						if(System.currentTimeMillis() - scanStart >= 15){ 
							System.out.print("The scan consumes ");
							System.out.println((System.currentTimeMillis() - scanStart));
						}*/
						//printChart();
					}
				}
				else{//otherwise call complete method
					long completeStart = System.currentTimeMillis();
					complete(cur_entry, i);
					/*
					if(System.currentTimeMillis() - completeStart >= 15){
						System.out.print("The complete consumes ");
						System.out.println((System.currentTimeMillis() - completeStart));
						//printChart();
					}*/
				}
				entry_list = curChart.getEntryList();
				int size = entry_list.size();
				if(j<size -1){
				//if there're still some entries to be processed 
					j++;
					continue;
				}
				
				break;	
			}
			//eachParseEnd = System.currentTimeMillis();
			
			//System.out.print("The " + String.valueOf(i) + "th round consumes ");
			//System.out.println((eachParseEnd - eachParseStart)/1000F);
			//eachParseStart = eachParseEnd;
			//printChart();
			//System.out.println(i);
		}
		
		long parseEnd = System.currentTimeMillis();

		// Get elapsed time in seconds
		//System.out.print("The parse totally consumes ");
		//System.out.println((parseEnd - start)/1000F);
		
		//build tree after parse complete
		/*TreeNode root = buildParsedTree();
		
		root.preOrder_tree_mapping(0,tags1);
		//root.preOrder(0);
		root.preOrder_get_node_out(0);
		
		ArrayList DGList = root.drugList1;
		formatDruglist(DGList);*/
		
	}
	
	/**
	 * Check if sentence can be parsed or not
	 * @param tags1	list of tags
	 * @return the result 
	 */
	public String check_parse(ArrayList tags1){
		
		init(this.startRule);
		//printChart();
		//System.out.println("charts.size()" + charts.size());
		//System.out.println("words.length" + words.length);
		
		int chart_index = 0;
		//for each state
		long start = System.currentTimeMillis();
		long eachParseStart = start;
		long eachParseEnd;
		for (int i=0; i<=words.length; i++)
		{		
			//System.out.println("charts.size() :"+charts.size() +"-- i = "+i );
			if(charts.size() <= i){
				//if there is no new entry generated from previous chart, then exit
				System.out.println("The sentence cannot be parsed!");
				return "The sentence cannot be parsed!";
			}
			else
				return "The sentence can be parsed!";
			
		}
		
		return "words length empty";
	}
	
	
	/**
	  * print out the content of chart for the purpose of debug  
	  */
	protected void printChart(){
		for (int i = 0; i<charts.size(); i++){
			System.out.println(charts.get(i).toString());
				
		}

}
	
	/**
	  * append newly created entry into the chart if it is not existing
	  * @param entry	entry generated during the parse
	  * @param chart	current chart
	  */
	protected void enqueue(EarleyEntry entry, EarleyChart chart){	
		if(chart.getEntryList() != null){
			HashMap<Integer, EarleyEntry> curmap = chart.getEntryMap();
			long start1 = System.currentTimeMillis();

			// Get elapsed time in seconds
			//System.out.println(chart.getEntryMap().keySet().size());
			/*
			Object temp[] = (Integer[])(curmap.keySet().toArray());
			for (int i=0; i<temp.length; i++){
				System.out.println(temp[i]);
			}
			*/
			//boolean isin = curmap.containsKey(entry.hashCode());
			boolean isin = curmap.keySet().contains(entry.hashCode());
			//if(System.currentTimeMillis() - start1 > 5){
				//long duration = System.currentTimeMillis() - start1;
				//System.out.print("contains consumes ");
				//System.out.println(duration);			
			//}
			if(!isin){
				HashMap<Integer, EarleyEntry> entryMap = chart.getEntryMap();
				//System.out.println(entry.toString2().hashCode());
				entryMap.put(entry.toString2().hashCode(),entry);
				ArrayList<EarleyEntry> entryList = chart.getEntryList();
				entryList.add(entry);
				chart.setEntryMap(entryMap);
				chart.setEntryList(entryList);
			}
		}
	}
	
	/**
	  * this rule is applied to entries that has non-terminal immediately
	  * to the right of its dot when the non-terminal is not a POS, it create
	  * a new entry representing top-down expectations. 
	  * @param entry	entry generated during the parse
	  * @param chart_index	index of current chart
	  */
	protected void predict(EarleyEntry entry, int chart_index){
		//get the item next to the dot in the entry
		EntryItem nextItem = entry.getNextItem();
		
		//return all of the rules based on the grammar
		ArrayList<EarleyEntry> entries = grammar.getAllEarleyEntries(nextItem);
		//System.out.println(entries.size());
		
		//create EarleyEntry instance for each rule and append them to the chart
		for (int i= 0; i<entries.size(); i++){
			//set where it start
			//System.out.println(chart_index);
			entries.get(i).setStart_pos((entry.getend_pos()));
			entries.get(i).setend_pos((entry.getend_pos()));
			entries.get(i).setOperation("predict");
			long equeueStart = System.currentTimeMillis();
			enqueue(entries.get(i), charts.get(chart_index));
			/*
			if((System.currentTimeMillis() - equeueStart) > 5){
				System.out.print("The enqueue procedure consumes ");
				System.out.println((System.currentTimeMillis() - equeueStart));
			}*/
		}
				
	}
	
	/**
	  * When a entry has a POS to the right of the dot, this function
	  * is called to examine the input and incorporate into the chart a entry 
	  * corresponding to the prediction of current word with a particular POS.
	  * @param entry	entry generated during the parse
	  * @param chart_index	index of current chart
	  */
	protected void scan(EarleyEntry entry, int chart_index){
		EntryItem nextItem = entry.getNextItem();
		int end = entry.getend_pos();
		//if(entry.toString2().indexOf("what") >= 0){
			//System.out.println(entry.toString2());
		//}
		ArrayList<EarleyEntry> entries = grammar.getAllEarleyEntries(nextItem);
		
		for (int i= 0; i<entries.size(); i++){
			
			//if new chart[chart_index+1] not exist, then create a new chart,
			//then put the new created entry into the new chart
			String word = entries.get(i).itemlist.get(2).getSign();
			
			if(end <words.length && word.equals(this.words[end]))
			{			
				if(chart_index+1 >= charts.size()){
					EarleyChart chart = new EarleyChart();
					chart.setChart_index(chart_index+1);
					charts.add(chart);
				}
				entries.get(i).setDot_pos(entries.get(i).getDot_pos()+1);
				entries.get(i).setOperation("scan");
				entries.get(i).setStart_pos(end);
				//set end position of new entry as original plus one
				entries.get(i).setend_pos(end+1);
				enqueue(entries.get(i), charts.get(chart_index+1));
		
			}
		}
	}
	
	/**
	  * it is called when a entry has its dot reached the right end of
	  * the rule, it find all previously created entries that were looking
	  * for this grammatical category at this position in the input, then 
	  * it creates entries copying the old entry, advancing the dot, and 
	  * append them into the charts
	  * @param entry	entry generated during the parse
	  * @param chart_index	index of current chart
	  */
	protected void complete(EarleyEntry entry, int chart_index){
		
		EntryItem head = entry.getHead();
		//search in previous chart
		for(int j=0; j<chart_index; j++)
		{
			EarleyChart chart = charts.get(j);
			ArrayList<EarleyEntry> entrylist = chart.getEntryList();
			for (int i= 0; i<entrylist.size(); i++){
				EarleyEntry cur_entry = entrylist.get(i);
				//System.out.println("current:" + cur_entry.toString());
				if(!cur_entry.ifComplete()){
					int end = cur_entry.getend_pos();
					if(end == entry.getstart_pos()){
						//if old entry end with the start postion of current entry
						EntryItem nextItem = cur_entry.getNextItem();
						//if current entry's head equals to old entry's item which is right to the dot 
						if(nextItem.equals(head)){
							EarleyEntry cloned = cur_entry.clone();
							if (cloned.moveIndex()){
								cloned.setOperation("complete");
								cloned.setStart_pos(cur_entry.getstart_pos());
								cloned.setend_pos(entry.getend_pos());
								ArrayList<EarleyEntry> s = new ArrayList<EarleyEntry>();
								s.add(entry);
								s.add(cur_entry);
								cloned.setAncestor(s);
								
								enqueue(cloned, charts.get(chart_index));
							}
							else
								System.out.println("Error: dot index cannot move!");
						}
					}
				}
			}
		}
	}
	
	
	
	
}
