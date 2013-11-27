package org.apache.algorithms;


import java.util.*;
import java.io.*;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


// Cache intermediate value to speed up Percy Liang's implementation of brown clustering.
// by Jingqi Wang;
public class CachedBrownClustering {
    
    public static class StackItem {
        public Integer wordindex;
        public Integer path_i;
        public char    ch;
        
        public StackItem( Integer wordindex, int path_i, char ch ) {
            this.wordindex = wordindex;
            this.path_i = path_i;
            this.ch = ch;
        }
    }
    
    private static final Logger logger = Logger.getLogger( CachedBrownClustering.class );
    
    Long running_time;
    
    int    wordindex;                                           // for counting uniq words;
    
    int    cluster_number;                                      // final cluster number;
    
    public Map<String, Integer> word_id_map;                    // key = word in string; value = word index;
    public Map<Integer, String> id_word_map;                    // key = word index;     value = word in string;
    public Map<Integer, Integer> id_frequency_map;              // key = word index;     value = word frequency;
    public Vector<Integer> sorted_by_frequency;                 // word index sorted by frequency;
    public Map<Integer, Map<Integer, Integer> > leftwords_map;  // key1 = word index;   key2 = right word index; value=frequency;
    public Map<Integer, Map<Integer, Integer> > rightwords_map; // key1 = word index;   key2 = left word index;  value=frequency;
        
    public Map<Integer, Integer> slot2cluster;
    public Map<Integer, Integer> cluster2slot;
    public Map<Integer, Integer> word2rep;
    public Map<Integer, Integer> rep2cluster;
    public Map<Integer, Integer> cluster2rep;    
    public Integer curr_cluster_id = 0;
    public Integer T;
    
    public int free_slot1;
    public int free_slot2;
    
    public Map<Integer, Vector<Integer> > cluster_tree;
    public Map<String, String>           word_path_map;
    public Integer state2_cluster_offset;
    
    public CostCalculator calculator;
    public Integer p1_count[];
    public Integer p2_count[][];
    
    public CachedBrownClustering() {
        wordindex = 1;
        word_id_map = new HashMap<String, Integer> ();
        id_word_map = new HashMap<Integer, String> ();
        id_frequency_map = new HashMap<Integer, Integer>();
        sorted_by_frequency = new Vector<Integer> ();
        leftwords_map = new HashMap<Integer, Map<Integer, Integer>>();
        rightwords_map = new HashMap<Integer, Map<Integer, Integer>>();  
        
        free_slot1 = -1;
        free_slot2 = -1;
        
        running_time = new Long(0);
        
        PropertyConfigurator.configure ( "conf/log4j.conf");
    }
    

    // load all documents into memory;
    @SuppressWarnings("unchecked")
    public void addText( String filename ) {
        logger.trace( "readText: filename=[" + filename + "]" );
        //System.out.println( filename );
        try {
            BufferedReader br = new BufferedReader( new FileReader( filename ) );
            String line;
            String lastword = "";
            while( ( line = br.readLine() ) != null ) {
                String [] words = line.split( " " );
                if( words.length == 0 ) {
                    continue;
                }
                Vector<String> wordlist = new Vector<String>();
                for( int i = 0; i < words.length; i++ ) {
                    if( words[i].length() == 0 ) {
                        continue;
                    }
                    wordlist.add( words[i] );
                }
                if( wordlist.size() == 0 ) {
                    continue;
                }

                for( int i = 0; i < wordlist.size(); i++ ) {
                    String word = wordlist.get( i );
                    insert_word( word );
                    if( lastword != "" ) {
                        insert_words( lastword, word );
                    }
                    lastword = word;
                }
            }
            br.close();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        
        // sort by frequency;
        T = 0;
        List<Map.Entry<Integer, Integer>> sorted_map = new ArrayList<Map.Entry<Integer, Integer>>( id_frequency_map.entrySet());        
        Collections.sort( sorted_map, new Comparator< Map.Entry<Integer, Integer> >() {   
            public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {      
                return (o2.getValue() - o1.getValue());
            }
        });        
        for( Object o: sorted_map ) {
            Integer index = ((Map.Entry<Integer, Integer>)o).getKey();
            sorted_by_frequency.add( index );
            T += ((Map.Entry<Integer, Integer>)o).getValue();
        }
        
        return;
    }
    
    // add a single word into word_id_map, id_word_map and id_frequency_map;
    private int insert_word( String word ) {
        if( !word_id_map.containsKey( word ) ) {
            word_id_map.put( word, wordindex );
            id_word_map.put( wordindex, word );
            id_frequency_map.put( wordindex , 0 );
            leftwords_map.put( wordindex, new HashMap<Integer, Integer > () );
            rightwords_map.put( wordindex, new HashMap<Integer, Integer> () );
            wordindex += 1;
        }
        int index = word_id_map.get( word );
        id_frequency_map.put( index, id_frequency_map.get( index ) + 1 );        
        logger.trace( "insertWord: word=[" + word + "] index=[" + index + "] frequency=[" + id_frequency_map.get( index ) + "]" );
        return 0;
    }
    
    // add bigram into left/right map;    
    private int insert_words( String word1, String word2 ) {
        Integer index1 = word_id_map.get( word1 );
        Integer index2 = word_id_map.get( word2 );
        Map<Integer, Integer> value1 = null;
        Map<Integer, Integer> value2 = null;
        if( !rightwords_map.containsKey( index1 ) ) {
            rightwords_map.put( index1, new HashMap<Integer, Integer>() );
        }
        if( !leftwords_map.containsKey( index2 ) ) {
            leftwords_map.put( index2, new HashMap<Integer, Integer>() );
        }
        value1 = rightwords_map.get( index1 );
        value2 = leftwords_map.get( index2 );
        
        if( value1.containsKey( index2 ) ) {
            value1.put( index2,  value1.get( index2 ) + 1 );
        } else {
            value1.put( index2, 1 );
        }
        if( value2.containsKey( index1 ) ) {
            value2.put( index1, value2.get( index1 ) + 1 );
        } else {
            value2.put( index1, 1 );
        }
        
        rightwords_map.put( index1, value1 );
        leftwords_map.put( index2, value2 );
        logger.trace( "insertWords: word1=[" + word1 + "] word2=[" + word2 + "]" );
        return 0;
    }
        
    
    public int createInitialClusters( int num ) {
        Long startms= System.currentTimeMillis();
        
        cluster_number = num;
        
        p1_count = new Integer[ cluster_number + 2 ];
        p2_count = new Integer[ cluster_number + 2 ][ cluster_number + 2 ];
        
        // 1. init data structure;
        word2rep = new HashMap<Integer, Integer>();
        for( Integer wordindex : sorted_by_frequency ) {
            word2rep.put( wordindex, -1 );
        }
        
        slot2cluster = new HashMap<Integer, Integer>();
        cluster2slot = new HashMap<Integer, Integer>();
        rep2cluster  = new HashMap<Integer, Integer>();
        cluster2rep  = new HashMap<Integer, Integer>();

        for( int slot = 0; slot < cluster_number; slot++ ) {
            int wordindex = sorted_by_frequency.get( slot );            
            slot2cluster.put( slot, wordindex );
            cluster2slot.put( wordindex, slot );
            
            word2rep.put( wordindex, wordindex );
            
            rep2cluster.put( wordindex, wordindex );
            cluster2rep.put( wordindex, wordindex );
        }
        
        free_slot1 = cluster_number;
        free_slot2 = cluster_number + 1;
        slot2cluster.put( free_slot1, -1 );
        slot2cluster.put( free_slot2, -1 );
        
        for( Integer slot = 0; slot < cluster_number + 2; slot++ ) {
            for( int rightslot = 0; rightslot < cluster_number + 2; rightslot++ ) {
                p1_count[ slot ] = 0;
                p2_count[ slot ][ rightslot ] = 0;
                p2_count[ rightslot ][ slot ] = 0;
            }
        }
        
        // 2. calculate p1_count;        
        for( Integer slot: slot2cluster.keySet() ) {
            Integer wordindex = slot2cluster.get( slot );
            if( wordindex < 0 ) {
                p1_count[ slot ] = -1;
                continue;
            }
            p1_count[ slot ] = id_frequency_map.get( wordindex );
        }
        
        // 3. calculate p2_count;
        for( Integer slot: slot2cluster.keySet() ) {
            Integer wordindex = slot2cluster.get( slot );
            if( wordindex < 0 ) {
                continue;
            }

            Map<Integer, Integer> rightwords = rightwords_map.get( wordindex );
            for( Integer rightword : rightwords.keySet() ) {
                if( !cluster2slot.containsKey( rightword ) ) {
                    // right word not in the slot;
                    continue;
                }

                Integer count = rightwords.get( rightword );
                Integer rightslot = cluster2slot.get( rightword );
                p2_count[ slot ][ rightslot ] = count;
            }
        }
        
        
        Long endms= System.currentTimeMillis();
        running_time += endms - startms;
        
        calculator = new CostCalculator( cluster_number + 2, T );
        calculator.Init( p1_count, p2_count );
        
        return 0;
    }
    
    public int incorporateNewPhrase( Integer word ) {
        Long startms = System.currentTimeMillis();
        
        int s = put_cluster_in_free_slot( word );
        
        cluster2rep.put( word, word );
        rep2cluster.put( word, word );
        
        // re-compute p1_count;
        p1_count[s] = id_frequency_map.get( word );
        
        // re-compute p2_count;
        Map<Integer, Integer> freqs = new HashMap<Integer, Integer>();
        for( Integer rightword : rightwords_map.get( word ).keySet() ) {
            Integer rep = get_rep_root( rightword );
            if( !rep2cluster.containsKey( rep ) ) {
                continue;
            }
            Integer rightcluster = rep2cluster.get( rep );
            if( !cluster2slot.containsKey( rightcluster ) ) {
                continue;
            }
            freqs.put( cluster2slot.get( rightcluster ), rightwords_map.get( word ).get( rightword ) );
        }
        for( Integer t : freqs.keySet() ) {
            p2_count[s][t] = freqs.get(t);
        }
        freqs.clear();
        
        for( Integer leftword : leftwords_map.get( word ).keySet() ) {
            Integer rep = get_rep_root( leftword );  // todo;
            if( !rep2cluster.containsKey( rep ) ) {
                continue;
            }
            Integer leftcluster = rep2cluster.get( rep );
            if( !cluster2slot.containsKey( leftcluster ) ) {
                continue;
            }
            freqs.put( cluster2slot.get( leftcluster ), leftwords_map.get( word ).get( leftword ) );
        }
        for( Integer t : freqs.keySet() ) {
            p2_count[t][s] = freqs.get(t);
        }
        freqs.clear();
        
        Long endms= System.currentTimeMillis();
        running_time += endms - startms;
        
        // calculate p1, p2, q2, L2;
        calculator.AddSlot(s, p1_count, p2_count);
        return 0;
    }

    public int mergeClusters() {
        Long startms = System.currentTimeMillis();

        // 1. find best s, t;
        Integer bests = 0;
        Integer bestt = 0;
        Double minl = 1e30;
        for( Integer s : slot2cluster.keySet() ) {
            for( Integer t: slot2cluster.keySet() ) {
                if( slot2cluster.get( s ) < 0 || slot2cluster.get( t ) < 0 ) continue;
                
                if( s >= t ) {
                    continue;
                }
                Double l = calculator.read_L2( s, t );
                if( l < minl ) {
                    bests = s;
                    bestt = t;
                    minl = l;
                }
            }
        }
        
        logger.debug( "merge_cluster: bests=[" + bests + "] bestt=[" + bestt + "] min_l=[" + minl + "]" );
        
        // 2. merge bests, bestt;
        Integer s = bests;
        Integer t = bestt;
        int a = slot2cluster.get( s );
        int b = slot2cluster.get( t );
        int c = curr_cluster_id++;
        
        if( state2_cluster_offset != 0 ) {
            System.out.println( "merge: " + a + "," + b + " into " + c );
        }
        
        cluster_tree.put(c, new Vector<Integer>() );
        cluster_tree.get(c).add(a);
        cluster_tree.get(c).add(b);
        
        int u = put_cluster_in_free_slot( c );
        
        free_up_slots( s, t );
        
        p1_count[u] = p1_count[s] + p1_count[t];
        for( int i = 0; i < p1_count.length; i++ ) {
            if( p1_count[i] < 0 ) {
                continue;
            }
            p2_count[u][i] = p2_count[s][i] + p2_count[t][i];
            p2_count[i][u] = p2_count[i][s] + p2_count[i][t];
        }
        p2_count[u][u] = p2_count[s][s]+p2_count[s][t] + p2_count[t][s] + p2_count[t][t];
        
        // cluster_tree
        int A = cluster2rep.get( a );
        int B = cluster2rep.get( b );
        merge_rep( A, B );
        int C = get_rep_root( A );
        
        cluster2rep.remove(a);
        cluster2rep.remove(b);
        rep2cluster.remove(A);
        rep2cluster.remove(B);
        //System.out.println( "rep2cluster.remove:[" + A + "," + B + "]" );
        cluster2rep.put( c, C );
        rep2cluster.put( C, c );
        
        Long endms = System.currentTimeMillis();
        running_time += endms - startms;        
        calculator.MergeSlot(s, t, u, p1_count, p2_count);
        
        //System.out.println("mergeClusters:" + (endms-startms) + " calculate:" + CostCalculator.running_time );
        return c;
    }
    
    private int put_cluster_in_slot( Integer a, Integer s ) {
        cluster2slot.put( a, s );
        slot2cluster.put( s, a );
        return 0;
    }
    
    private int put_cluster_in_free_slot( Integer a ) {
        int s = -1;
        if( free_slot1 != -1 ) { s = free_slot1; free_slot1 = -1; }
        else if( free_slot2 != -1 ) { s = free_slot2; free_slot2 = -1; }
        logger.debug("put_cluster_in_free_slot: a=[" + a + "] free_slot1=[" + free_slot1 + "] free_slot2=[" + free_slot2 + "]" );
        assert( s != -1 );
        
        put_cluster_in_slot( a, s );
        return s;
    }
    
    private int free_up_slots( Integer s, Integer t ) {
        free_slot1 = s;
        free_slot2 = t;
        
        cluster2slot.remove( slot2cluster.get( s ) );
        cluster2slot.remove( slot2cluster.get( t ) );
        //System.out.println( "cluster2slot.remove:[" + s + "," + t + "]" );
        slot2cluster.put( s, -1 );
        slot2cluster.put( t, -1 );
        return 0;
    }
    
    private int get_rep_root( Integer s ) {
        Integer root = s;
        while( word2rep.get( root ) >= 0 && word2rep.get( root ) != root ) {
            root = word2rep.get( root );
        }
        logger.debug( "get_rep_root: s=[" + s + ":" + id_word_map.get(s) + "] root=[" + root + ":" + id_word_map.get(root) + "]" );
        return root;
    }
    
    private int merge_rep( Integer s, Integer t ) {
        Integer root1 = get_rep_root( s );
        Integer root2 = get_rep_root( t );
        if( root1 != root2 ) {
            word2rep.put( s, t );
        }
        return 0;
    }
    
    public int doClustering( int num ) {
        
        // step1: initial clusters;
        createInitialClusters( num );
        
        // step2: merge into cluster_number clusters;
        curr_cluster_id = id_word_map.size();
        state2_cluster_offset = 0;
        cluster_tree = new HashMap<Integer, Vector<Integer> >();
        Long startms = System.currentTimeMillis();
        Long lastms = System.currentTimeMillis();
        Long endms  = System.currentTimeMillis();
        Long t1  = System.currentTimeMillis();
        Long t2  = System.currentTimeMillis();
        for( int i = cluster_number; i < sorted_by_frequency.size(); i++ ) {
            Integer new_a = sorted_by_frequency.get( i );
            if( i % 100 == 0 ) {
                endms = System.currentTimeMillis();
                logger.trace( "merging " + i + " : " + new_a + ":" + id_word_map.get( new_a ) );
                logger.trace( "running_time: " + (running_time-t1) + " " + (CostCalculator.running_time-t2) + " " + ( endms - lastms ) + " " + ( endms - startms ) );
                lastms = System.currentTimeMillis();
                t1 = running_time;
                t2 = CostCalculator.running_time;
            }
            incorporateNewPhrase( new_a );
            mergeClusters();
        }

        // step3: merge into one clusters to generate word -> path;
        state2_cluster_offset = curr_cluster_id;
        for( int i = 0; i < cluster_number - 1; i++ ) {
            mergeClusters();
        }
        
        // step4: get word -> path map;
        word_path_map = new HashMap<String, String>();
        Vector<StackItem> stack = new Vector<StackItem>();
        char[] path = new char[ 1000 ];
        
        assert( cluster2slot.size() == 1 );
        for( Integer key: cluster2slot.keySet() ) {
            stack.add( new StackItem( key, 0, '\0' ) );
        }
        while( !stack.isEmpty() ) {
            StackItem item = stack.lastElement();
            Integer a = item.wordindex;
            Integer path_i = item.path_i;
            if( item.ch != '\0' ) {
                path[ path_i - 1 ] = item.ch; 
            }
            stack.remove( stack.size() - 1 );
            
            if( !cluster_tree.containsKey( a ) ) {
                String pathstr = new String( path );
                pathstr = pathstr.substring( 0, path_i );
                word_path_map.put( id_word_map.get(a), pathstr );
            } else {
                Integer left = cluster_tree.get( a ).get( 0 );
                Integer right = cluster_tree.get( a ).get( 1 );
                if( a >= state2_cluster_offset ) {
                    int newpath_i = path_i + 1;
                    stack.add( new StackItem( left, newpath_i, '0' ) );
                    stack.add( new StackItem( right, newpath_i, '1' ) );
                } else {
                    stack.add( new StackItem( left, path_i, '\0' ) );
                    stack.add( new StackItem( right, path_i, '\0' ) );                    
                }
            }
        }
        
        for( String word : word_path_map.keySet() ) {
            logger.info( "Path: word=[" + word + "] path=[" + word_path_map.get(word) + "]" );
        }
        
        return 0;
    }
    
    public String getWordPath( String word ) {
        if( !word_path_map.containsKey( word ) ) {
            return "";
        }
        return word_path_map.get( word );
    }
    
    public int loadPath( String pathFileName ) {
        try {
            BufferedReader br = new BufferedReader( new FileReader( pathFileName ) );
            String line;
            if( word_path_map == null ) {
                word_path_map = new HashMap<String, String>();
            }
            while( ( line = br.readLine() ) != null ) {
                String path = line.substring( 0, line.indexOf( "\t" ) );
                line = line.substring( line.indexOf( "\t" ) + 1 );
                String word = line.substring( 0, line.indexOf( "\t" ) );
                
                word_path_map.put( word, path );
            }
            br.close();
        
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        
        return 0;
    }
    
    public int dumpPath( String pathFileName ) {
        
        return 0;
    }
    
    static public void main( String[] argv ) {
        CachedBrownClustering bcl = new CachedBrownClustering();
        
        // 1. read text;
        try {            
            Directory docdir = FSDirectory.open( new File( "i2b2_data" ) );            
            String file[] = docdir.listAll();            
            for( String filename : file ) {
                filename = "i2b2_data/" + filename;
                bcl.addText( filename );
            }
            docdir.close();
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        // 2. start clustering;
        bcl.doClustering( 200 );
        
        // 3. get path by word;
        String word = "abdominopelvic";
        String wordpath = bcl.getWordPath( word );

        // or: 
        // 2. load path file;
        // bcl.loadPath( "word_path.txt" );
        // 3. get path by word;
        // String word = "abdominopelvic";
        // String wordpath = bcl.getWordPath( word );
        
        System.out.println( "word=[" + word + "] path=[" + wordpath + "]" );
        
        return;
    }
    
}

