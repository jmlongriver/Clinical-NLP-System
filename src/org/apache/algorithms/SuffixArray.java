/**
 * 
 */
package org.apache.algorithms;

import java.util.*;

import org.apache.NLPTools.Document;
import org.apache.NLPTools.Token;
import org.apache.medex.Lexicon;

import org.apache.NLPTools.Global;
import org.apache.medex.DrugTag;

/** 
 * SuffixArray Class by implementing CD3 algorithm, which is a linear time method building suffix array.
 * @author yonghuiwu
 *
 */
public class SuffixArray implements Global{

	/**
	 * 
	 */
	//private members
	
	Document doc;
	Lexicon lex;
	
	//added from python version suffix array
	char end_char;  //# char that not occurred in the input text,  serves as the end of string
	Global.SuffixArrayMode mode;  //# the mode of building a tree, 0: using all suffix, 1: using suffix [pos: ],  that pos is a start pos of a word
	Global.SuffixArrayCaseMode cmode;
		    
	String otext;// # the input string added ' ' and ' $'
	String text ; //#original input string 
	int N; //# the length of __text
		    
	private Map<Character,Integer> text2int=new HashMap<Character,Integer>();//# mapping from letter to int, text2int['a'] = 1
		    
	private Vector<Character> int2text = new Vector<Character>();//# mapping from int to text,  int2text[1] = 'a'

	int[] int_text; //# convert __text into int using __text2int

		   
	int[] SA;//   # ranked suffix array

	Vector<Global.SuffixArrayNode> ST;  // # suffix tree
	int ST_num;// # suffix tree node num
	
	//default constructor for suffix array
	public SuffixArray(Document doc,Lexicon lex,char endc,Global.SuffixArrayMode mode,Global.SuffixArrayCaseMode cmode) {
		// TODO Auto-generated constructor stub
		this.doc=doc;
		this.lex=lex;
		//System.out.println("end char : |"+endc+"|");
		this.end_char=endc;
		this.mode=mode;
		this.cmode=cmode;
		this.re_build();
		//System.out.println("suffixarray init over");
	}
	
	public void reset_doc(Document doc){
		this.doc=doc;
	}
	
	public void reset_lexicon(Lexicon lex){
		this.lex=lex;
	}
	
	public void re_build(){
		this.otext=this.doc.norm_str().trim();
		
		if(this.cmode==Global.SuffixArrayCaseMode.SENSITIVE){
			//System.out.println("SuffixArray runs in Case Sensitive mode");
		}
		else{
			//System.out.println("SuffixArray runs in Case Non-Sensitive mode");
			this.otext=this.otext.toLowerCase();
		}
		this.otext=this.otext.replace(this.end_char, ' ');
		this.otext=this.otext+this.end_char;
		//System.out.println("Doc string: |"+this.otext+"|");
		this.N=this.otext.length();
		this.DC3_SA();
		
		//this.show_suffix_str();
		
		//#print 'Building Suffix tree'
        if (this.mode == Global.SuffixArrayMode.ALL){
            //System.out.println("suffix tree mode 0: All suffix mode");
            this.construct_tree();
        }
        else if (this.mode == Global.SuffixArrayMode.WORD){
            //System.out.println("suffix tree mode 1: only suffix as word start mode");
            this.construct_tree_word();
            //System.out.println("suffix tree mode 1: only suffix as word start mode end");
        }
	}
	
	public SuffixArray(Document doc,Lexicon lex) {
		// TODO Auto-generated constructor stub
		this.doc=doc;
		this.lex=lex;
		//System.out.println("suffixarray init over");
	}
	
	//private functions
	private void DC3_SA(){
		//# get all the unique letters, biuld int text
		//System.out.println("#######DC3_SA");
		this.int_text=new int[this.otext.length()+3];
		int count=1;
		for (int i=0;i<this.otext.length();i++){
			char ch=otext.charAt(i);
			int code=0;
			if (!this.text2int.containsKey(ch)){
				this.text2int.put(ch, count);
				code=count;
				this.int2text.add(ch);
				count=count+1;
				
			}
			else{
				code=this.text2int.get(ch);
			}
			this.int_text[i]=code;
		}
		
		
		/***************************************************************
		 * rank the Characters. Ranking is not required for searching. 
		 *
		 *If not ranking, the characters will coded by the occurrence sequence.
		 *After ranking ,the coding is according to the alphabet sequence, easy to compare the result.
		//
		Global.CharacterComparator ccom=new Global.CharacterComparator(this.int2text);
		Integer[] indices=ccom.createIndexArray();
		Arrays.sort(indices,ccom);
		for(int i=0;i<indices.length;i++){
			this.text2int.put(this.int2text.get(indices[i]), i+1);
		}
		Iterator it = this.text2int.entrySet().iterator();
		  
		while (it.hasNext()) {		   
			Map.Entry entry = (Map.Entry) it.next();		   
			Character key = (Character) entry.getKey();		   
			int value = (Integer) entry.getValue();					   
			this.int2text.set(value-1, key);		   
			//System.out.println("key=" + key + " value=" + value);  
		}
		
		for(int i=0;i<this.otext.length();i++){
			this.int_text[i]=this.text2int.get(this.otext.charAt(i));
		}//*/
		
		//Show the code value for Characters
		/*it = this.text2int.entrySet().iterator();
		  
		while (it.hasNext()) {		   
			Map.Entry entry = (Map.Entry) it.next();		   
			Character key = (Character) entry.getKey();		   
			int value = (Integer) entry.getValue();					   		   
			System.out.println("key=" + key + " value=" + value);  
		}*/
		
		
		int n=this.otext.length();
		int K=n;
		this.SA=new int[n+3];
		
		this.suffix_array(this.int_text,SA,n,K);
		//System.out.println("#######DC3_SA end");
	}
	
	public void suffix_array(int[] s,int[] SA, int n,int K){
		int n0 = (n + 2) / 3 ;//# number of positions MOD 3 = 0, eg,  for string:  y a b b a d a b b a d  o   , n0 = 4 = {0, 3, 6, 9}
        int n1 = (n + 1) / 3 ;//# positions MOD 3 = 1,  n1 = 4 = {1, 4, 7, 10}
        int n2 = n / 3       ;//# positions MOD 3 = 2   n2 = 4 = {2, 5, 8, 11}
        int n02 = n0 + n2;//

        int [] s12 = new int[n02 + 3];//# s12 is all the suffix positions MOD 3 = 1 or 2
        int [] SA12 = new int [n02 + 3] ;//# SA12 is the ranked suffix array of s12

        int[] s0 = new int [n0] ;//# all the suffix positions MOD 3 = 0
        int[] SA0 = new int[n0] ;//# the ranked suffix array of s0

        //# find all suffixs with position MOD 3 =  1 or 2                        0 1 2 3 4 5 6
        //# Notice,  when n % 3 = 1,  there is a dummy pos MOD 3 = 1 added.  eg,  y a b c 0 0 0,  s1 = {1, 4},  where suffix[4] is dummy(000)

        int j = 0;
        for (int i=0;i<n + (n0 - n1);i++){
            if (i % 3 != 0){
            	s12[j] = i;
                j = j + 1;
            }          
        }
        this.radix_pass(s12 , SA12, s, 2, n02, K);
        
        this.radix_pass(SA12, s12 , s, 1, n02, K);
        this.radix_pass(s12 , SA12, s, 0, n02, K);
        
        //# find lexicographic names of triples
        int name = 0;
        int c0 = -1;
        int c1 = -1;
        int c2 =- 1;
        for(int i=0;i<n02;i++){
        	if (s[SA12[i]] != c0 || s[SA12[i]+1] != c1 || s[SA12[i]+2] != c2){
        		name = name + 1;                        
        		c0 = s[SA12[i]];                       
        		c1 = s[SA12[i]+1];                       
        		c2 = s[SA12[i]+2];
        	}
        	if (SA12[i] % 3 == 1){// # left half
        		s12[SA12[i]/3] = name ;//#  SA12[0] is the position ranked in top 0, SA12[0] / 3 = the triple number of this position
        	}
        	else{//# right half, s12 is now the ranking of tripples
        		s12[SA12[i]/3 + n0] = name;
        	}
                    
        }
        
        //# recurse if names are not yet unique
        
        if (name < n02){
            this.suffix_array(s12, SA12, n02, name);
            //# store unique names in s12 using the suffix array
            for(int i=0;i<n02;i++){
            	s12[SA12[i]] = i + 1;
            }
        }
        else{//# generate the suffix array of s12 directly, SA12[tripple_rank]=tripple_no
        	for(int i=0;i<n02;i++){
        		SA12[s12[i] - 1] = i;
        	}
        }
        
        //#sort positions MOD 3 == 0,  by the first letter

                
        j = 0;
        for (int i=0;i<n02;i++){
        	if (SA12[i] < n0){
        		s0[j] = 3*SA12[i];  
        		j = j + 1;
        	}
                
        }
           
        this.radix_pass(s0, SA0, s,0, n0, K);
        
        //# merge sorted SA0 suffixes and sorted SA12 suffixes
        int p = 0;
        int t = n0 - n1;
        int k = 0;
        while (k < n){
            //#print 'p: ', p, " t: ", t
            //#print 'i: ', i, " j: ", j, ' k: ', k, " n: ", n

            int i = this.GetI(t, SA12, n0);
            j = SA0[p];
            boolean flag=false;

            if (SA12[t] < n0){
                flag = this.leq2(s[i],s12[SA12[t] + n0], s[j],s12[j/3]);
            }
            else{
                flag = this.leq3(s[i],s[i+1],s12[SA12[t]-n0+1], s[j],s[j+1],s12[j/3+n0]);
            }

            if (flag){// # suffix from SA12 is smaller
                SA[k] = i;
                k = k + 1;
                t = t + 1;
                if (t == n02){// # SA12 is done, only SA0 suffixes left
                    for (int pp=p;pp<n0;pp++){
                        SA[k] = SA0[pp];
                        k = k + 1;
                    }
                }
            }
            else{// # SA0 is smaller
                SA[k] = j;
                k = k + 1;
                p = p + 1;
                if (p == n0){//  # SA0 done,  only SA12 left
                    for (int tt=t;tt<n02;tt++){
                        SA[k] = this.GetI(tt, SA12, n0);
                        k = k + 1;
                    }
                }
            }
        }
            
        

	}
	
	//#compare two pairs
	private boolean leq2(int a1, int a2, int b1, int b2){
        return (a1 < b1 || (a1 == b1 && a2 <= b2));
	}
	
	//#compare two triples
	private boolean leq3(int a1, int a2, int a3, int b1, int b2, int b3){
        return (a1 < b1 || (a1 == b1 && this.leq2(a2, a3, b2, b3)));
	}
        		
	private int GetI(int t, int[] SA12, int n0){
        if (SA12[t] < n0){
        	return SA12[t] * 3 + 1;
        }
        else{
            return (SA12[t] - n0) * 3 + 2; //#<n0, left(mod 1), else right(mod 2);
        }
	}
	
	/**
	 * The implementation of radix sort algorithm in java -- linear time
	 */
	private void radix_pass(int[]a, int[]b, int[] r, int index,int n, int K){
		int[] c = new int[K + 1];
		for (int i=0;i<n;i++){
			c[r[a[i]+index]] = c[r[index+a[i]]] + 1;
		}

		        
		        
		        
		int summ = 0;
		for(int i=0;i<K+1;i++){
			int tmp=c[i];
			c[i]=summ;
			summ=summ+tmp;
		}
		        
		for(int i=0;i<n;i++){
			b[c[r[index+a[i]]]] = a[i];        
			c[r[index+a[i]]] = c[r[index+a[i]]] + 1;
		}
		                  
	}
	
	//# Construct the suffix tree from the suffix array
    //#build tree from suffix ranked by DC3
    //#[father, [son], [start, end], [pos1, pos2,...  ]]
    //#Method: insert the suffix into a tree one by one.
    //#Since the suffix were ranked,  when insert a suffix, we only need to consider the right most tree
    //# if a suffix has common string with the current right most tree,  insert the suffix to the right most tree
    //# else, insert the suffix as the right most child in to root
    private void construct_tree(){
    	//System.out.println("############start construct_tree");
        this.ST = new Vector<Global.SuffixArrayNode>();
        Global.SuffixArrayNode node=new Global.SuffixArrayNode();
        this.ST.add(node);
        this.ST_num = 1;

        for (int i=0;i<this.N;i++){
            this.insert_SF_tree(this.SA[i], 0, 0); //# 0 denote the root in __SA;
        }
        //System.out.println("############end construct_tree");
    }
    
    //# build suffix tree from the suffix [pos: ], where 'pos' is the beginning of a word
    private void construct_tree_word(){
    	this.ST = new Vector<Global.SuffixArrayNode>();
        Global.SuffixArrayNode node=new Global.SuffixArrayNode();
        this.ST.add(node);
        this.ST_num = 1;
 
        for (int i=0;i<this.N;i++){
        	int pos=this.SA[i];
        	if (this.otext.charAt(pos) != ' ' && this.otext.charAt(pos) != '\n' && this.otext.charAt(pos) != this.end_char && (pos == 0 || (this.otext.charAt(pos-1) == ' ' || this.otext.charAt(pos-1) == '\n'))){
        		this.insert_SF_tree(this.SA[i], 0, 0); //# 0 denote the root in __SA;
        	}
        }
    }
            

    
    //#insert the string [pos: ] to node
    private void insert(int poss,int lcs, int node){
    	//System.out.println("#######insert");
        int pos = poss + lcs;
        SuffixArrayNode SAnode=new SuffixArrayNode();
        SAnode.father=node;
        SAnode.start=pos;
        SAnode.end=this.N;
        SAnode.pos.add(poss);
        this.ST.add(SAnode);// add leaf node
        this.ST.get(node).son.add(this.ST_num);
        if (node != 0){
            this.ST.get(node).pos.add(poss);
        }
        this.ST_num = this.ST_num + 1;
        //System.out.println("#######insert end");
    }
        
    //# get the longest common sequence length
    private int get_lcs(int pos, int start, int end){
        if (start ==- 1){
            return 0;
        }
        int poss = pos;
        int tpos = start;
        int lcd = 0;
        while (poss < this.N && tpos < end){
            if (this.otext.charAt(poss) == this.otext.charAt(tpos)){
                lcd = lcd + 1;
                poss = poss + 1;
                tpos = tpos + 1;
            }
            else{
                break;
            }
        }

        return lcd;
    }

    //# insert when split a node,  do not append positions, it's inter insert
    private void insert_inter(int poss,int lcs, int node){
        int pos = poss + lcs;
        SuffixArrayNode SAnode=new SuffixArrayNode();
        SAnode.father=node;
        SAnode.start=pos;
        SAnode.end=this.N;
        SAnode.pos.add(poss);
        this.ST.add(SAnode);//# leaf node
        this.ST.get(node).son.add(this.ST_num);
        this.ST_num = this.ST_num + 1;
    }
        
    /**
    *# insert the suffix string [pos: ] into node with node number of  'node'
    *# when matching a suffix [pos: ] on the edge [start: end] of the right most child, only 3 result:
    *# 1.  nstart = start, not a single charactor matched.  2.  nstart == end, the current edge is mached as part of suffix [pos: ] 
    *3.  start < nstart  < end, denoting the matching stopped at a midian position between start and end,  we need to split the node.  
    *The suffix never will exhausted before the edge, since the 0 will be complimented after the charactors were over,  and 0 < all the charactors, and the suffix is > edge.  
    *Thus, it never gona happen.
    **/
    private void insert_SF_tree(int poss, int d, int node){
        //#print 'pos', pos, 'node', node, '\n'
    	//System.out.println("#######insert_SF_tree:"+poss+" | "+d+" | "+node);

        int pos = poss + d;
        //System.out.println("node:"+node+" size:"+this.ST.get(node).son.size());
        
        if (this.ST.get(node).son.size() == 0){
            this.insert(poss,d, node);
        }
        else{
            int rnode = this.ST.get(node).son.get(this.ST.get(node).son.size()-1) ;//# right most child
            int start = this.ST.get(rnode).start;
            int end = this.ST.get(rnode).end;
            int lcs  = this.get_lcs(pos, start, end);// # compare with right most child
            
            int nstart = start + lcs;
            //#print ' ---------- lcs: ', lcs, "nstart: ", nstart, "start: ", start, "end: ", end, self.__text[start: end], self.__text[pos: ]

            if (nstart == start){// # no match,  insert into node as right most child
                this.insert_inter(poss,lcs + d, node);
            }
            else if (nstart == end){// # the current node is matched over, continue to match
                //#print '###recercive: ', npos, rnode
                if (this.ST.get(rnode).son.size() > 0){
                	this.ST.get(rnode).pos.add(poss);
                    this.insert_SF_tree(poss,lcs + d, rnode);
                }
                else{
                    this.insert_inter(poss,lcs + d, rnode);
                }
            }
            else if (nstart > start && nstart < end ){// # matched in the middle, split the current node and insert into right
            	SuffixArrayNode SAnode=new SuffixArrayNode();
                SAnode.father=rnode;
                SAnode.start=nstart;
                SAnode.end=end;
                SAnode.son.addAll(this.ST.get(rnode).son) ;
                SAnode.pos.addAll(this.ST.get(rnode).pos);
                for(int i=0;i<this.ST.get(rnode).son.size();i++){
                	int child=this.ST.get(rnode).son.get(i);
                	this.ST.get(child).father=this.ST_num;
                }
                this.ST.get(rnode).end=nstart;
                this.ST.get(rnode).son.clear();//# clear the children
                this.ST.get(rnode).son.add(this.ST_num);
                this.ST.get(rnode).pos.add(poss);
                this.ST.add(SAnode);
                this.ST_num=this.ST_num+1;
                this.insert_inter(poss, lcs+d, rnode);
                
            }
        }
        //System.out.println("#######insert_SF_tree  end");
    }

   
    
    public Vector<Integer> SAsearch(String query){

    	//System.out.println("SAsearch:|"+query);
    	Vector<Integer> result = new Vector<Integer>();
        if (this.otext.length() == 0 || this.ST.get(0).son.size() == 0){
            return result;
        }

        int qpos = 0;
        int llen = query.length();
        int start_node_id=0;
        int match_pos=0;
        while (qpos < llen){
        	if(! this.text2int.containsKey(query.charAt(qpos))){//the query string contains a character, which not appeared in text, never match
        		start_node_id =- 1;
        		break;
        	}
            int node = start_node_id;
            int pos = match_pos;
            int start = this.ST.get(node).start;
            int end = this.ST.get(node).end;


            //#print 'node', node
            //#print 'qpos', qpos, repr(query[qpos]), start, end, pos
            if (node == 0 || start + pos == end){
                //#print "node ", node, 'match over'
                if (this.ST.get(node).son.size() == 0){
                    start_node_id =- 1;
                    break;
                }
                for(int i=0;i<this.ST.get(node).son.size();i++){
                    //#print repr(self.__text[self.__ST[child][2][0]]), ":", repr(query[qpos])
                	int child=this.ST.get(node).son.get(i);
                	int tmp=this.ST.get(child).start;
                	if( this.text2int.get(this.otext.charAt(tmp)) > this.text2int.get(query.charAt(qpos)) ){// no match, 
                		//since the tree is ranked, if current > query, then no match
                		start_node_id =- 1;
                		break;
                	}
                    if (this.otext.charAt(tmp)  == query.charAt(qpos)){
                        start_node_id = child;
                        match_pos = 1;
                        break;
                    }
                    else{// # no match
                        start_node_id =- 1;
                    }
                }
            }

            else{
                //#print 'match node ', node
                //#print repr(self.__text[start + pos]), ":", repr(query[qpos])
                if (this.otext.charAt(start+pos)==query.charAt(qpos)){
                    match_pos = pos + 1;
                }
                else{
                    start_node_id =- 1;
                }
            }

            if (start_node_id == -1){
                break;
            }

            qpos = qpos + 1;
        }

        
        if (start_node_id >= 0){
            //#print repr(self.__otext)
        	//System.out.println("SA search hit:"+query+":"+this.ST.get(start_node_id).pos.toString());
            if (this.mode==Global.SuffixArrayMode.ALL){// # all suffix mode
            	for(int i=0;i<this.ST.get(start_node_id).pos.size();i++){
            		int val=this.ST.get(start_node_id).pos.get(i);
            		result.add(val);
            	}
            }
            else if(this.mode==Global.SuffixArrayMode.WORD){ //#word suffix mode, judge if the search end at a word bondary
            	for(int i=0;i<this.ST.get(start_node_id).pos.size();i++){
            		int val=this.ST.get(start_node_id).pos.get(i);
            		char w=this.otext.charAt(val+llen);
            		int tmpos=val+llen+1;
            		if (w == ' ' || w == '\n' || w == this.end_char || (w=='.' && (tmpos==this.otext.length()||(tmpos<this.otext.length() && (this.otext.charAt(tmpos)==' ' || this.otext.charAt(tmpos)=='\n') )))){
            			result.add(val);
            		}
            		
            	}
            }
        }
                    
        return result;
    }
	
    /**
	 * Searching the lexicons using suffixarry search
	 */
    public Vector<SuffixArrayResult> search(){
    	Vector<SuffixArrayResult> result = new Vector<SuffixArrayResult>();
		
		
		//int text_len=this.otext.length();
		//int token_len=this.doc.boundary_token_vct().size();
		//System.out.println("search str:\n");
		//System.out.println(lower_norm_str);
		int len=this.lex.lex_list().size();
		
		int pos=-1;
		int pos2=-1;
		String lower_key="";
		String semantic_type="";
		Map<Integer,Integer> startMap;
		Map<Integer,Integer> endMap;
		Vector<Token> token_vct;
		
			
		startMap=this.doc.startMap();
		endMap=this.doc.endMap();	
		token_vct=this.doc.token_vct();
		
		
		for (int i=0;i<len;i++){
			lower_key=this.lex.lex_list().get(i).toLowerCase();
			semantic_type=this.lex.sem_list().get(i);
			//System.out.println("searching : "+lower_key+" "+semantic_type);
			Vector<Integer> tresult=this.SAsearch(lower_key);
			//System.out.println("result size: "+tresult.size());
			for(int j=0;j<tresult.size();j++){
				pos=tresult.get(j);
				pos2=pos+lower_key.length();
				SuffixArrayResult re=new SuffixArrayResult();
				//System.out.println("before get boundaryStartMap:"+pos);
				//System.out.println(startMap);
				int tmp=startMap.get(pos);
				//System.out.println("get map, token index = " + tmp);
				
				re.start_token=tmp;
				re.start_pos=token_vct.get(tmp).startPos();
				//System.out.println("start pos:"+re.start_pos);

				//System.out.println("before get endMap, token index = " + pos2);
				tmp=endMap.get(pos2);
				//System.out.println("after get endMap, token index = " + tmp);
				re.end_token=tmp;
				re.end_pos=token_vct.get(tmp).endPos();
				re.semantic_type=semantic_type;
				result.add(re);
				
			}
		}
		//System.out.println("search end ");
		return result;
    }
	//public functions, search lexicon.
    // basic search, using string match.
	public Vector<SuffixArrayResult> simple_search(){
		//System.out.println("search");
		Vector<SuffixArrayResult> result = new Vector<SuffixArrayResult>();
		
		String lower_norm_str=doc.boundary_norm_str().toLowerCase();
		int text_len=lower_norm_str.length();
		int token_len=this.doc.boundary_token_vct().size();
		//System.out.println("search str:\n");
		//System.out.println(lower_norm_str);
		int len=this.lex.lex_list().size();
		
		int pos=-1;
		int pos2=-1;
		for (int i=0;i<len;i++){
			String lower_key=this.lex.lex_list().get(i).toLowerCase();
			//System.out.println("search for: "+i+" : "+lower_key);
			
			int startp=0;
			pos=lower_norm_str.indexOf(lower_key,startp);
			//System.out.println("pos:"+pos);
			while(pos>=0){
				pos2=pos+lower_key.length();
				if((pos==0 || this.doc.boundary_norm_str().charAt(pos-1)==' ' || this.doc.boundary_norm_str().charAt(pos-1)=='\n') && 
						(pos2==lower_norm_str.length() || this.doc.boundary_norm_str().charAt(pos2)==' ' || this.doc.boundary_norm_str().charAt(pos2)=='\n' ||
						(this.doc.boundary_norm_str().charAt(pos2)=='.' && (pos2+1==lower_norm_str.length() ||lower_norm_str.charAt(pos2+1)==' ' || lower_norm_str.charAt(pos2+1)=='\n')))){
					//System.out.println("\nsearch |"+lower_key+"| hit pos:"+pos +" pos2:"+pos2+" str:|"+this.doc.boundary_norm_str().substring(pos,pos2)+"|");
					//System.out.println("char at pos:|"+this.doc.boundary_norm_str().charAt(pos)+"|");
					/*
					if(pos-1>=0){
						System.out.println("char at pos-1:|"+this.doc.boundary_norm_str().charAt(pos-1)+"|");
					}
					
					
					if(pos2+30<this.doc.boundary_norm_str().length()){
						System.out.println("string at pos2:"+this.doc.boundary_norm_str().charAt(pos2)+":|"+this.doc.boundary_norm_str().substring(pos2,pos2+30));
					}
					*/
					SuffixArrayResult re=new SuffixArrayResult();
					//System.out.println("before get boundaryStartMap:"+pos);
					int tmp=(Integer) this.doc.boundaryStartMap().get(pos);
					//System.out.println("get map, token index = " + tmp);
					
					re.start_token=tmp;
					re.start_pos=this.doc.boundary_token_vct().get(tmp).startPos();
					//System.out.println("start pos:"+re.start_pos);
					
					
					
					tmp=(Integer) this.doc.boundaryEndMap().get(pos2);
					re.end_token=tmp;
					re.end_pos=this.doc.boundary_token_vct().get(tmp).endPos();
					re.semantic_type=this.lex.sem_list().get(i);
					result.add(re);
				}
				
				startp=pos+1;
				//find next search point
				while(startp <text_len && lower_norm_str.charAt(startp)!=' ' && lower_norm_str.charAt(startp)!='\n'){
					startp=startp+1;
				}
				//System.out.println("start new search at pos:"+startp);
				pos=lower_norm_str.indexOf(lower_key, startp);
				
			}
			
		}
		
		return result;
	}
	
	//# get the strings for current node
    private String get_node_str(int node){
    	String result="";
    	//System.out.println("get node str start");
    	int start = this.ST.get(node).start;
        int end = this.ST.get(node).end;
        if (start >= 0 && end >= 0){
            result= this.otext.substring(start,end);
        }
        else{
            result= "";
        }
        //System.out.println("get node str end");
        return result;
    }
    
    //# print the tree structure in array format
    public void print_tree(){
    	System.out.println("######print_tree");
        for (int i=0;i<this.ST.size();i++){
            if (i == 0){
            	System.out.println(""+i+" ["+this.ST.get(0).father+","+this.ST.get(0).son.toString()+",["+this.ST.get(0).start+","+this.ST.get(0).end+"], "+this.ST.get(0).pos.toString()+"");
            }
            else{
            	System.out.println(""+i+" ["+this.ST.get(i).father+","+this.ST.get(i).son.toString()+",["+this.ST.get(i).start+","+this.ST.get(i).end+"], "+this.ST.get(i).pos.toString()+" |"+this.otext.substring(this.ST.get(i).start,this.ST.get(i).end)+"|");
            	
            }
        }
        System.out.println("######print_tree end");
    }

    /** Debug function
	 * # traverse the suffix tree and print all the suffix
	 */
	
    public void traverse(){
        Stack<String> path = new Stack<String>();
        Stack<Integer> stack=new Stack<Integer>();
        stack.push(0);
        Set<Integer> seen = new HashSet<Integer>();
        while (stack.size()>0){
        	int node=stack.lastElement();
        	//System.out.println("node11:"+node);
        	if (seen.contains(node)){
        		//System.out.println("node22 seen:"+node);
        		//System.out.println("stack size:"+stack.size());
                stack.pop();
                //System.out.println("path size "+path.size());
                path.pop();
                //System.out.println("continue:22");
                continue;
        	}
            seen.add(node);
            String strr = this.get_node_str(node);
            //System.out.println("get node str after");

            path.push(strr);
            int llen=this.ST.get(node).son.size();

            if (llen == 0){
            	this.print_path(path);
            	//System.out.println(path.toString()+"|"+path.size());
                stack.pop();
                path.pop();
                //System.out.println("continue:");
                continue;
            }
            //System.out.println("node:"+node);
            llen=this.ST.get(node).son.size();
            //System.out.println("llen:"+llen);
            int i = llen - 1;
            while (i >= 0){
            	stack.add(this.ST.get(node).son.get(i));
                i = i - 1;
            }
        }
    }

    private void print_path(Stack<String> path){
    	String pp="";
    	for(int i=0;i<path.size();i++){
    		pp=pp+path.elementAt(i);
    	}
    	//System.out.println("|"+pp+"|");
    }
    /** Debug only function
	 * # print all the suffix strings in SA, according to the ranking result of DC3
	 */
    //
    public void show_suffix_str(){
    	System.out.println("\n------All Suffix strings");
        if (this.mode == Global.SuffixArrayMode.ALL){
        	for(int i=0;i<this.N;i++){
        		System.out.println("|"+this.otext.substring(this.SA[i])+"|");
        	}

        }
        else if (this.mode == Global.SuffixArrayMode.WORD){
        	for(int i=0;i<this.N;i++){
        		int pos=this.SA[i];
        		if (this.otext.charAt(pos) != ' ' &&this.otext.charAt(pos) != '\n' && (pos == 0 || (this.otext.charAt(pos-1) == ' ' || this.otext.charAt(pos-1) == '\n'))){
        			System.out.println("|"+this.otext.substring(pos).replace("\n", "/n")+"|");
        		}
        		
        	}
        }
        else{
            System.out.println( "error mode string"+ this.mode);
        }
    }


}
