package org.apache.algorithms;


import java.util.Map;

// 1. assign a version number to each cached value;
// 2. increase global version number when addSlot and MergeSlot;
// 3. check if the version number is valid when reading;
// 4. if the version number is not valid and the dependent p1_count or p2_count has changed, re-calculate;
// by Jingqi Wang;

public class CostCalculator {
    
    // for statistics;
    public static Integer cached_count;
    public static Integer not_cached;
    public static Long running_time;
    
    public class CachedValue{ 
        int version;
        double value;
        public CachedValue() {
            version = 0;
            value   = 0.0;
        }
        
        public void update( int version ) {
            // log;
            //System.out.println( "CachedHere: updateVersion!" );
            cached_count += 1;
            this.version = version;
        }
        
        public void reset( int version, double value ) {
            this.version = version;
            this.value   = value;
            // log;
            not_cached += 1;
            //System.out.println( "Reset here! not cached" );
        }
        
        public boolean is_valid( int version ) {
            // if true, log;
            boolean ret = this.version == version;
            if( ret ){ 
                //System.out.println( "CachedHere!" ); 
                cached_count += 1;
            }
            return ret;
        }
        
        public boolean has_value() {
            return version != 0;
        }
        
        public double value() {
            return value;
        }
        
        public void clear() {
            this.version = 0;
            this.value = 0.0;
        }
    }
    
    
    public Integer version;         // used for check cached value;
    public Integer slot_number;
    public Integer T;               // uniq words count;
    public double  log2;            // a const value of log( 2 );
    
    public int []      slot_using;          // -1 means slot not used;
    public Integer[]   p1_count;            // -1 means slot not used;
    public Integer[][] p2_count;            // current frequency for words and bigrams in slot;
    public Integer[]   p1_count_new;        
    public Integer[][] p2_count_new;        // used for insert new slot or merge slot;
    
    public CachedValue[] p1;
    public CachedValue[][] p2;
    public CachedValue[][] q2;
    public CachedValue[][] L2;
    
    // cached intermediate values
    public CachedValue[] log_p1;
    public CachedValue[] compute_s1;
    public CachedValue[][] bi_q2;
    public CachedValue[][] hyp_p1;
    public CachedValue[][] log_hyp_p1;
    public CachedValue[][] hyp_p2;
    public CachedValue[][] hyp_q2;    
    
    public CostCalculator( int slot_number, int T ) {
        this.version = 0;
        this.slot_number = slot_number;
        this.T = T;
        this.log2 = Math.log( 2 );
        
        slot_using = new int [ slot_number ];
        p1_count = new Integer[ slot_number ];
        p2_count = new Integer[ slot_number ][ slot_number ];
        p1_count_new = null;
        p2_count_new = null;
        p1 = new CachedValue[ slot_number ];
        p2 = new CachedValue[ slot_number ][ slot_number ];
        q2 = new CachedValue[ slot_number ][ slot_number ];
        L2 = new CachedValue[ slot_number ][ slot_number ];

        log_p1 = new CachedValue[ slot_number ];
        compute_s1 = new CachedValue[ slot_number ];
        
        bi_q2 = new CachedValue[ slot_number ][ slot_number ];
        hyp_p1 = new CachedValue[ slot_number ][ slot_number ];
        log_hyp_p1 = new CachedValue[ slot_number ][ slot_number ];
        hyp_p2 = new CachedValue[ slot_number ][ slot_number ];
        hyp_q2 = new CachedValue[ slot_number ][ slot_number ];

        for( int i = 0; i < slot_number; i++ ) {
            slot_using[i] = 1;
            p1[i] = new CachedValue();
            log_p1[i] = new CachedValue();
            compute_s1[i] = new CachedValue();
            for( int j = 0; j < slot_number; j++ ) {
                p2[i][j] = new CachedValue();
                q2[i][j] = new CachedValue();
                L2[i][j] = new CachedValue();
                bi_q2[i][j] = new CachedValue();
                hyp_p1[i][j] = new CachedValue();
                log_hyp_p1[i][j] = new CachedValue();
                hyp_p2[i][j] = new CachedValue();
                hyp_q2[i][j] = new CachedValue();
            }
        }
        
        running_time = new Long( 0 );
        cached_count = 0;
        not_cached = 0;
    }

    public int Init( Integer[] p1Count, Integer[][] p2Count ) {
        Long startms = System.currentTimeMillis();
        
        assert( p1Count.length == slot_number );
        assert( p2Count.length == slot_number );
        p1_count_new = p1Count;
        p2_count_new = p2Count;
        version += 1;
        
        for( int i = 0; i < p1_count_new.length; i++ ) {
            if( p1_count_new[i] < 0 ) {
                slot_using[i] = -1;
                continue;
            }
            get_p1( i );
            for( int j = 0; j < p1_count_new.length; j++ ) {
                if( p1_count_new[j] < 0 ) {
                    continue;
                }
                get_p2( i, j );
                get_q2( i, j );
            }
        }
        
        for( int i = 0; i < p1_count_new.length; i++ ) {
            if( slot_using[i] < 0 ) {
                continue;
            }
            for( int j = 0; j < p1_count_new.length; j++ ) {
                if( slot_using[j] < 0 ) {
                    continue;
                }
                get_L2( i, j );
            }
        }
        
        for( int i = 0; i < p1_count.length; i++ ) {
            p1_count[i] = p1_count_new[i];
            for( int j = 0; j < p1_count.length; j++ ) {
                p2_count[i][j] = p2_count_new[i][j];
            }
        }
        
        Long endms = System.currentTimeMillis();        
        running_time += endms - startms;
        return 0;
    }
    
    public int AddSlot( Integer slot, Integer[] p1Count, Integer[][] p2Count ) {
        Long startms = System.currentTimeMillis();
        
        assert( p1Count.length == slot_number );
        assert( p2Count.length == slot_number );
        p1_count_new = p1Count;
        p2_count_new = p2Count;
        version += 1;
        slot_using[slot] = 1;
        
        // update p1, p2, q2;
        get_p1( slot );        
        for( int i = 0; i < slot_number; i++ ) {
            if( slot_using[i] < 0 ) {
                continue;
            }
            get_p2( slot, i );
            get_p2( i, slot );
            get_q2( slot, i );
            get_q2( i, slot );
        }
        
        // update L2 related to slot;
        for( int i = 0; i < slot_number; i++ ) {
            if( slot_using[i] < 0 ) {
                continue;
            }
            get_L2( slot, i );
        }
        
        // update other L2;
        for( int i = 0; i < p1_count_new.length; i++ ) {
            if( slot_using[i] < 0 ) {
                continue;
            }
            for( int j = 0; j < p1_count_new.length; j++ ) {
                if( slot_using[j] < 0 ) {
                    continue;
                }
                if( i < j && i != slot && j != slot ) {
                    double ol = L2[i][j].value();
                    double l = get_bi_q2( i, slot ) + get_bi_q2( j, slot ) - get_bi_hyp_q2( i, j, slot );
                    L2[i][j].reset( version, ol + l );
                    L2[j][i].reset( version, ol + l );
                    
                }
            }
        }
        
        // update p1_count, p2_count;
        for( int i = 0; i < p1_count.length; i++ ) {
            p1_count[i] = p1_count_new[i];
            for( int j = 0; j < p1_count.length; j++ ) {
                p2_count[i][j] = p2_count_new[i][j];
            }
        }

        Long endms= System.currentTimeMillis();
        running_time += endms - startms;
                
        return 0;
    }
    
    public int MergeSlot( Integer s, Integer t, Integer u, Integer[] p1Count, Integer[][] p2Count ) {
        Long startms= System.currentTimeMillis();
        
        assert( p1Count.length == slot_number );
        assert( p2Count.length == slot_number );
        p1_count_new = p1Count;
        p2_count_new = p2Count;
        version += 1;
        slot_using[s] = -1;
        slot_using[t] = -1;
        slot_using[u] = 1;
        
        // update p1, p2, q2;
        get_p1( u );
        for( int i = 0; i < slot_number; i++ ) {
            if( slot_using[i] < 0 ) {
                continue;
            }
            get_p2( u, i );
            get_p2( i, u );
            get_q2( u, i );
            get_q2( i, u );
        }
        
        // update L2 using old values;
        for( int i = 0; i < p1_count_new.length; i++ ) {
            if( slot_using[i] < 0 ){
                continue;
            }
            for( int j = 0; j < p1_count_new.length; j++ ) {
                if( slot_using[j] < 0 ) {
                    continue;
                }
                if( i > j && i != u && j != u ) {
                    double l = compute_L2_using_old( s, t, u, i, j );
                    //System.out.println( i+ " " + j + " " + old + " " + l );
                    L2[i][j].reset( version, l );
                    L2[j][i].reset( version, l );
                }
            }
        }
        // clear unused slots.
        p1_count_new[s] = -1;
        p1_count_new[t] = -1;
        for( int i = 0; i < p1_count.length; i++ ){
            p2_count_new[i][s] = 0;
            p2_count_new[s][i] = 0;
            p2_count_new[t][i] = 0;
            p2_count_new[i][t] = 0;
        }
        
        // update L2 related to new generated slot u;
        for( int i = 0; i < p1_count_new.length; i++ ) {
            if( slot_using[i] < 0 ) continue;
            get_L2( i, u );
        }
        clear_slot( s );
        clear_slot( t );        

        // update p1_count, p2_count with the new ones;
        for( int i = 0; i < p1_count.length; i++ ) {
            p1_count[i] = p1_count_new[i];
            
            for( int j = 0; j < p1_count.length; j++ ) {
                p2_count[i][j] = p2_count_new[i][j];
            }
        }
        
        Long endms= System.currentTimeMillis();
        running_time += endms - startms;
                
        return 0;
    }
    
    // read only!
    public double read_L2( int s, int t ) {
        return L2[s][t].value();
    }
    
    private void clear_slot( int s ) {
        p1[s].clear();
        log_p1[s].clear();
        compute_s1[s].clear();
        for( int i = 0; i < p1_count.length; i++ ) {
            p2[s][i].clear();
            p2[i][s].clear();
            q2[s][i].clear();
            q2[i][s].clear();
            L2[s][i].clear();
            L2[i][s].clear();
            bi_q2[s][i].clear();
            bi_q2[i][s].clear();
            hyp_p1[s][i].clear();
            hyp_p1[i][s].clear();
            log_hyp_p1[i][s].clear();
            log_hyp_p1[s][i].clear();
            hyp_p2[s][i].clear();
            hyp_p2[i][s].clear();
            hyp_q2[s][i].clear();
            hyp_q2[i][s].clear();
        }
    }
    
    
    private double get_p1( int i ) {
        // if cached;
        CachedValue value = p1[i];
        if( value.is_valid( version ) ) {
            return value.value();
        } else if ( value.has_value() && p1_unchange( i ) ) {
            value.update( version );
            return value.value();
        }
        // else update;
        double ret = ((double) p1_count_new[i] ) / T;
        value.reset( version, ret );
        return value.value();
    }
    
    private double get_p2( int i, int j ) {
        CachedValue value = p2[i][j];
        if( value.is_valid( version ) ) {
            return value.value();
        } else if ( value.has_value() && p2_unchange( i, j ) ) {
            value.update( version );
            return value.value();
        }
        
        double ret = ((double) p2_count_new[i][j] ) / ( T - 1 );
        value.reset( version, ret );
        return value.value();
    }
    
    private double get_q2( int s, int t ) {
        CachedValue value = q2[s][t];
        if( value.is_valid( version ) ) {
            return value.value();
        } else if ( value.has_value() && p1_unchange( s ) && p1_unchange( t ) && p2_unchange( s, t ) ) {
            value.update( version );
            return value.value();
        }
        
        double logs = get_log( s );
        double logt = get_log( t );
        double ret = p2q( get_p2( s, t ), logs, logt );
        value.reset(version, ret);
        return value.value();
    }
    
    private double get_log( int s ) {
        CachedValue value = log_p1[s];
        if( value.is_valid(version) ) {
            return value.value();
        } else if( value.has_value() && p1_unchange( s ) ) {
            value.update(version);
            return value.value();
        }
        double ret = Math.log( get_p1( s ) );
        value.reset(version, ret);
        return value.value();            
    }
    
    private double get_log( int s, int t ) {
        CachedValue value = log_hyp_p1[s][t];
        if( value.is_valid(version) ) {
            return value.value();
        } else if( value.has_value() && p1_unchange( s ) ) {
            value.update(version);
            return value.value();
        }
        double ret = Math.log( get_hyp_p1( s, t ) );
        value.reset(version, ret);
        return value.value();            
    }
    
    private double p2q( double pst, double logs, double logt ) {
        double ret = 0.0;
        if( pst < 1e-10 ) { 
            ret = 0.0;
        } else {
            ret = pst * ( Math.log( pst ) - logs - logt ) / log2;
        }
        return ret;
    }
    
    private boolean p1_unchange( Integer s ) {
        return p1_count[s] == p1_count_new[s];
    }
    private boolean p2_unchange( int s, int t ) {
        return p2_count[s][t] == p2_count_new[s][t];
    }
    private boolean p2_all_unchange( Integer s ) {
        for( int i = 0; i < p1_count_new.length; i++ ) {
            if( p2_count_new[i][s] >= 0 && p2_count_new[s][i] >= 0 && p2_unchange( i, s ) && p2_unchange( s, i ) ){
                continue;
            } else {
                return false;
            }
        }
        return true;
    }
    
    
    private double get_L2( int s, int t ) {
        if( s == t ) { return 0.0; }
        CachedValue value = L2[s][t];
        if( value.is_valid(version) ) {
            return value.value();
        }
        double ret = get_compute_s1( s ) + get_compute_s1( t ) - get_bi_q2( s, t );        
        for( int u = 0; u < p1_count.length; u++ ) {
            if( u == s || u == t || p1_count_new[u] < 0 ) {
                continue;
            }
            ret -= get_bi_hyp_q2( s, t, u );
        }
        ret -= get_hyp_q2( s, t );

        value.reset(version, ret);
        L2[t][s].reset( version, ret );
        return value.value;
    }
    
    private double get_compute_s1( int s ) {
        CachedValue value = compute_s1[s];
        if( value.is_valid(version) ) {
            return value.value();
        } else if( value.has_value() && p2_all_unchange( s ) ) {
            value.update(version);
            return value.value();
        }
        
        double ret = 0.0;
        for( int t = 0; t < p1_count_new.length; t++ ) {
            if( p1_count_new[t] < 0 ) continue;
            
            ret += get_bi_q2( s, t );
        }
        
        value.reset(version, ret);
        return value.value();
    }
    
    private double get_bi_q2( int s, int t ) {
        CachedValue value = bi_q2[s][t];
        if( value.is_valid(version) ) {
            return value.value();
        } else if( value.has_value() && p2_unchange( s, t) && p2_unchange( t, s ) ) {
            value.update(version);
            return value.value();
        }
        
        double ret = 0.0;
        if( s == t ) {
            ret = get_q2( s, t );
        } else {
            ret = get_q2( s, t ) + get_q2( t, s );
        }
        value.reset(version, ret);
        return value.value();
    }
    
    private double get_bi_hyp_q2( int s, int t, int u ){
        // su, tu, us, ut, s, t, u;
        double ret = get_hyp_q2_left( s, t, u ) + get_hyp_q2_right( u, s, t );
        return ret;
    }
    
    private double get_hyp_q2( int s, int t ) {
        CachedValue value = hyp_q2[s][t];
        if( value.is_valid(version) ) {
            return value.value();
        } else if( value.has_value() && p1_unchange(s) && p1_unchange(t)&&p2_unchange(s,s)&&p2_unchange(s,t)&&p2_unchange(t,s)&&p2_unchange(t,t) ) {
            value.update(version);
            return value.value();
        }
        
        double p1 = get_hyp_p2( s, t );
        get_hyp_p1( s, t );
        double logp2 = get_log( s, t );
        double ret = p2q( p1, logp2, logp2 );
        value.reset(version, ret);
        return value.value();
    }
    
    private double get_hyp_p1( int s, int t ) {
        CachedValue value = hyp_p1[s][t];
        if( value.is_valid(version) ) {
            return value.value();
        } else if ( value.has_value() && p1_unchange(s) && p1_unchange(t) ) {
            value.update(version);
            return value.value();
        }
        
        double ret = 0.0;
        ret = get_p1( s ) + get_p1( t );
        return ret;
    }
    
    private double get_hyp_p2( int s, int t ) {
        CachedValue value = hyp_p2[s][t];
        if( value.is_valid(version) ) {
            return value.value();
        } else if( value.has_value() && p2_unchange(s,t)&&p2_unchange(s,s)&&p2_unchange(t,s)&&p2_unchange(t,t) ) {
            value.update(version);
            return value.value();
        }
        
        double ret = get_p2(s,s) + get_p2(s,t) + get_p2(t,s) + get_p2(t,t);
        value.reset(version, ret);
        return value.value();
    }
    
    private double get_hyp_q2_left( int s, int t, int u ) {
        double pst = get_hyp_p2_left( s, t, u );
        double logs = get_log( s, t );
        double logt = get_log( u );
        return p2q( pst, logs, logt );
    }
    
    private double get_hyp_q2_right( int u, int s, int t ) {
        double pst = get_hyp_p2_right( u, s, t );
        double logs = get_log( s, t );
        double logt = get_log( u );
        return p2q( pst, logs, logt );
    }
    
    private double get_hyp_p2_left( int s, int t, int u ) {
        double ret = get_p2( s, u ) + get_p2( t, u );
        return ret;
    }
    
    private double get_hyp_p2_right( int u, int s, int t ) {
        double ret = get_p2( u, s ) + get_p2( u, t );
        return ret;
    }
    
    
    private double compute_L2_using_old( int s,  int t,  int u,  int v,  int w ) {
        assert( v >= w );
        assert( v != u && w != u );
        
        double l = read_L2( v, w );
        l -= get_bi_q2( v, s ) + get_bi_q2( w, s ) + get_bi_q2( v, t ) + get_bi_q2( w, t );
        l += get_bi_hyp_q2( v, w, s ) + get_bi_hyp_q2( v, w, t );        
        l += get_bi_q2( v, u ) + get_bi_q2( w, u );
        l -= get_bi_hyp_q2( v, w, u );
        
        return l;
    }
    
 // compare double data;
    private Boolean feq( double v1, double v2 ) {
        if( v1 < v2 + 10e-10 && v1 >= v2 - 10e-10 ) {
            return true;
        } else {
            return false;
        }
    }
    
    public void assert_equal( Map<Integer, Double> p1, Map<Integer, Map<Integer, Double >> p2,
            Map<Integer, Map<Integer, Double> > q2, Map<Integer, Map<Integer, Double>> L2 ) {
        
        //System.out.println( "cached_count=" + cached_count + " not_cached=" + not_cached );
        for( Integer s : p1.keySet() ) {
            if( p1_count[ s ] < 0 ) continue;
            assert( feq( p1.get( s), this.p1[s].value() ) );
            //System.out.println( "p1: s=" + s + " old=" + p1.get(s) + " new=" + this.p1[s].value() );
            for( Integer t : p1.keySet() ) {
                if( p1_count[ t ] < 0 ) continue;
                //System.out.println( "p2: st=[" + s + " " + t + "] old=" + p2.get(s).get(t) + " new=" + this.p2[s][t].value() );
                //System.out.println( "q2: st=[" + s + " " + t + "] old=" + q2.get(s).get(t) + " new=" + this.q2[s][t].value() );

                assert( feq( p2.get(s).get(t), this.p2[s][t].value() ) );
                assert( feq( q2.get(s).get(t), this.q2[s][t].value() ) );
            }
        }
        for( Integer s : p1.keySet() ) {
            if( p1_count[ s ] < 0 ) continue;
            for( Integer t : p1.keySet() ) {
                if( p1_count[ t ] < 0 ) continue;
                //System.out.println( "L2: st=[" + s + " " + t + "] old=" + L2.get(s).get(t) + " new=" + this.L2[s][t].value() );
                assert( feq( L2.get(s).get(t), this.L2[s][t].value() ) );
            }            
        }
        //System.out.println( "cached_count=" + cached_count + " not_cached=" + not_cached );
    }    
}
