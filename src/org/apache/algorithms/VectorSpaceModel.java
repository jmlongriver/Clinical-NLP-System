package org.apache.algorithms;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

import antlr.collections.List;

public class VectorSpaceModel {
    public enum SimDistanceType { ExactMatch, PartOfMatch, EditDistance, CommonWord, Jaccard };
    public HashMap<String, Double> weight_list;
    public SimDistanceType _sim_distance_type;
    
    public VectorSpaceModel() {
        // default TfIdf;
        _sim_distance_type = SimDistanceType.Jaccard;
        weight_list = new HashMap<String, Double>();
    }
    
    public VectorSpaceModel( SimDistanceType type ) {
        _sim_distance_type = type;
        weight_list = new HashMap<String, Double>();
    }
    
    public void setSimDistanceType( SimDistanceType type ) {
        _sim_distance_type = type;
        weight_list = new HashMap<String, Double>();
    }
    
    
    
    public ArrayList< Integer > getRankingTopN( String query, ArrayList<String> candidates, int n ) {
        assert( n >= 0 );
        //calWeight(candidates);
        ArrayList<Integer> ret = new ArrayList<Integer>();
        //System.out.println("query:"+ query);
        Map<Integer, Double> score_map = new HashMap<Integer, Double>();
        for( int i = 0; i < candidates.size(); i++ ) {
            String candidate = candidates.get( i );
            double score = getSimilarity( query, candidate );
            score_map.put( i, score );
            
        }
        //System.out.println("-----------------");
        //System.out.println("query:"+query);
        ArrayList< Map.Entry<Integer, Double> > ranked_score_list = 
                new ArrayList< Map.Entry<Integer, Double> >( score_map.entrySet() );
        
        Collections.sort(ranked_score_list, new Comparator<Map.Entry<Integer, Double>>() {   
            public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {      
                //return (o2.getValue() - o1.getValue()); 
                return (o2.getValue().compareTo(o1.getValue()));
            }
        });
        
        for( int i = 0; i < Math.min( n, ranked_score_list.size()); i++ ) {
        	int index = ranked_score_list.get( i ).getKey();
            ret.add( index );
            //System.out.println( i + ":\t" + candidates.get(index) + "\t" + score_map.get(index));
        }
        
        
        return ret;
    }
    
    
    public double getScoreBywords(HashSet<String> words){
    	double score = 0.0;
    	
    	for(String str : words){
    		//System.out.println(str);
    		if(str.trim().equals("")){
    			continue;
    		}
    		if(str.matches("\\d*\\.?\\d*")){
    			score += 1.8;
    		}
    		else
    		if(str.matches("mg|ml|mcg|unt")){
    			score += 0.4;
    		}
    		else
    			score += 1.0;
    	}
    	//System.out.println(score);
    	return score;
    }
    
    
    public double getJaccardSimilarity(String[] sent1, String[] sent2){
		HashSet<String> h1 = new HashSet<String>();
		HashSet<String> h2 = new HashSet<String>();
		HashSet<String> h3 = new HashSet<String>();
		for(String s: sent1){
			h1.add(s);
			h3.add(s);
		}
		//System.out.println("h1 "+ h1);
		for(String s: sent2){
			h2.add(s);
			h3.add(s);
		}
		//System.out.println("h2 "+ h2);
		
		//int sizeh1 = h1.size();
		//Retains all elements in h3 that are contained in h2 ie intersection
		h1.retainAll(h2);
		/*
		double intersection_score = 0.0;
		for (String str: h1){
			intersection_score +=getWeight(str);
		}
		
		
		double union_score = 0.0;
		for (String str: h3){
			union_score +=getWeight(str);
		}*/
		//h1 now contains the intersection of h1 and h2
		//System.out.println("Intersection "+ h1);
		
			
		//h2.removeAll(h1);
		//h2 now contains unique elements
		//System.out.println("Unique in h2 "+ h2);
		
		//Union 
		/*
		System.out.println("**********");
		System.out.println(StringUtils.join(sent1," "));
		System.out.println(StringUtils.join(sent2," "));
		System.out.println("h1: "+h1.toString());
		System.out.println(getScoreBywords(h1));
		
		System.out.println("h3: "+h3.toString());
		System.out.println(getScoreBywords(h3));
		*/
		return getScoreBywords(h1)/getScoreBywords(h3);
		
	}
    
    
    public double getSimilarity( String sent1, String sent2 ) {
    
        switch( _sim_distance_type ) {
	        case ExactMatch:
	            return getExactMatchSimilarity( sent1.split( " " ), sent2.split( " " ) );
	        case EditDistance:
	            return getEditDisSimilarity( sent1.split( " " ), sent2.split( " " ) );
	        case Jaccard:
	            return getJaccardSimilarity( sent1.split(" "), sent2.split(" ") );
	        case PartOfMatch:
	        	//System.out.println("sent1 : " + sent1);
	        	//System.out.println("sent2 : " + sent2);
	        	//System.out.println(getPartOfSimilarity( sent1.split(" "), sent2.split(" ")));
	            return getPartOfSimilarity( sent1.split(" "), sent2.split(" ") );
	        case CommonWord:
	        	return getCommonSimilarity( sent1.split(" "), sent2.split(" ") );
        }        
        return 0;
    }
    
    public double getEditDisSimilarity( String[] sent1, String[] sent2 ) {
        assert( sent1.length != 0 );
        assert( sent2.length != 0 );
        double AB = 0;
        double AA = 0;
        double BB = 0;
        for( String word1: sent1 ) {
            if( word1.length() == 0 ) {
                continue;
            }
            AA += 1;
            for( String word2: sent2 ) {
                if( word2.length() == 0 ) {
                    continue;
                }
                BB += 1;
                double dist = editDistance( word1, word2 );
                dist = 1 / ( 1 + dist / word2.length() );
                AB += dist;
                //System.out.println( "\tword1=[" + word1 + "]\tword2=[" + word2 + "]\teditDist=[" + dist + "]\tAB=["+AB+"]" );
            }
        }        
        return AB / Math.sqrt( AA * BB );
    }
    
    public double getExactMatchSimilarity( String[] sent1, String[] sent2 ) {
        Map<String, Double> A = new HashMap<String, Double>();
        Map<String, Double> B = new HashMap<String, Double>();
        for( String word : sent1 ) {
            A.put( word, 1.0 );
        }        
        for( String word : sent2 ) {
            B.put( word, 1.0 );
        }
        return this.getCosineSimilarity( A, B );
    }
    public double getPartOfSimilarity( String[] sent1, String[] sent2 ) {
        assert( sent1.length != 0 );
        assert( sent2.length != 0 );
        double AB = 0;
        double AA = 0;
        double BB = 0;
        for( String word1: sent1 ) {
            if( word1.length() == 0 ) {
                continue;
            }
            AA += 1;
            for( String word2: sent2 ) {
                if ( word2.length() == 0 )
                    continue;
                BB += 1;
                if( word1.startsWith( word2 ) || word2.startsWith(word1) ) {
                    //System.out.println( "\tword1=[" + word1 + "]\tword2=[" + word2 + "]");
                    AB += 1;
                }
            }
        }
        AA = sent1.length;
        //BB = sent2.length;
        
        return AB / Math.sqrt( AA * BB );
    }
    
    public double getTfIdfSimilarity( String[] sent1, String[] sent2 ) {
        Map<String, Double> A = new HashMap<String, Double>();
        Map<String, Double> B = new HashMap<String, Double>();
        for( String word : sent1 ) {
            Double weight = this.getWeight( word );
            if( word.length() != 0 ) {
                A.put( word, weight );
            }
        }
        for( String word : sent2 ) {
            Double weight = this.getWeight( word );
            if( word.length() != 0 ) {
                B.put( word, weight );
            }
        }    
        return this.getCosineSimilarity( A, B );
    }
    
    public double getCommonSimilarity( String[] sent1, String[] sent2 ) {
        assert( sent1.length != 0 );
        assert( sent2.length != 0 );
        double score = 0.0;
        ArrayList<Integer> index_exist = new ArrayList<Integer>();
        for( String word1: sent1) {
            if(word1.length() == 0)
            	continue;
           
            for( int j=0; j< sent2.length; j++ ) {
                if ( sent2[j].length() == 0 )
                    continue;
                
                if( word1.startsWith(sent2[j]) || sent2[j].startsWith(word1)  && (!index_exist.contains(j))) {
                    //System.out.println( "\tword1=[" + word1 + "]\tword2=[" + word2 + "]");
                	score += 1.0;
                    index_exist.add(j);
                }
            }
        }
        
        
        return score;
    }
    public void calWeight(ArrayList<String> sents){
    	HashMap<String, Integer> tf_dict = new HashMap<String, Integer>();
    	HashMap<String, ArrayList<Integer>> df_dict = new HashMap<String, ArrayList<Integer>>();
    	int index = 0;
    	for(String str : sents){
    		String[] words = str.split(" ");
    		for(String word : words){
    			word = word.trim();
    			if(!tf_dict.containsKey(word)){
    				tf_dict.put(word, 1);
    			}
    			else{
    				int freq = tf_dict.get(word);
    				tf_dict.put(word,  freq+1);
    			}
    			
    			if(!df_dict.containsKey(word)){
    				ArrayList<Integer> indexes = new ArrayList<Integer>();
    				indexes.add(index);
    				df_dict.put(word, indexes);
    			}
    			else{
    				ArrayList<Integer> indexes = df_dict.get(word);
    				if(!indexes.contains(index)){
    					indexes.add(index);
    				}
    			}
    			
    		}
    		index += 1;
    	}
    	
    	for (String key : tf_dict.keySet()) {
    		int df = df_dict.get(key).size();
    	    double weight = tf_dict.get(key)/(double)df;
    	    weight_list.put(key, weight);
    	   
    	    ArrayList< Map.Entry<String, Double> > ranked_score_list = 
                    new ArrayList<Map.Entry<String, Double> >( weight_list.entrySet() );
            
            Collections.sort(ranked_score_list, new Comparator<Map.Entry<String, Double>>() {   
                public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {      
                    //return (o2.getValue() - o1.getValue()); 
                    return (o2.getValue().compareTo(o1.getValue()));
                }
            });
            /*
            for( int i = 0; i < ranked_score_list.size(); i++ ) {
                System.out.println(ranked_score_list.get(i).getKey() + "\t"+ranked_score_list.get(i).getValue() + "\t"+tf_dict.get(key) + "\t"+df);
            }*/
    	}
    	
    	
    }
    
    public double getWeight( String word ) {
        // todo: how to get weight??
        if(weight_list.containsKey(word.trim())){
        	return weight_list.get(word.trim());
        }
        else
        	return 1;
    }
    

    public double getCosineSimilarity(Map<String,Double> A, Map<String, Double> B) {
        double AB = 0;
        double AA = 0;
        double BB = 0;
        for( Object o: A.keySet() ) {
            String key = (String)o;
            double  vA = A.get( key );
            if( B.containsKey( key ) ) {
                double vB = B.get( key );
                AB += vA * vB;
                //System.out.println( "\tword=[" + key + "]\tAB=[" + AB + "]");
            }
            AA += vA*vA;
        }
        for( Object o: B.keySet() ) {
            String key = (String)o;
            double  vB  = B.get(key);
            BB += vB * vB;
        }
        
        if( AA == 0 || BB == 0 ) {
            return -1;
        }
        return AB/Math.sqrt( AA * BB );
    }

    public double editDistance(String s, String t) {
        int m=s.length();
        int n=t.length();
        int[][]d=new int[m+1][n+1];
        for(int i=0;i<=m;i++){
            d[i][0]=i;
        }
        for(int j=0;j<=n;j++){
            d[0][j]=j;
        }
        for(int j=1;j<=n;j++){
            for(int i=1;i<=m;i++){
                if(s.charAt(i-1)==t.charAt(j-1)){
                    d[i][j]=d[i-1][j-1];
                }
                else{
                    d[i][j]=tripleMin((d[i-1][j]+1),(d[i][j-1]+1),(d[i-1][j-1]+1));
                }
            }
        }
        return(d[m][n]);
      }
    
    public static int tripleMin(int a,int b,int c){
        return(Math.min(Math.min(a,b),c));
      }
}