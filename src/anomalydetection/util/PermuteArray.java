package anomalydetection.util;

import java.util.ArrayList;
import java.util.List;

public class PermuteArray {
	   // used by next
    private int level;

    private int data[];
    private int iter[];
    private int valk[];
    private int ret[];


    public PermuteArray( int N ) {
        level = 0;
        iter = new int[ N ];
        valk = new int[ N ];
        data = new int[ N ];
        ret = new int[ N ];
        for( int i = 0; i < data.length; i++ ) {
            data[i] = -1;
        }
    }

    public int sgn() {
        // Is there a way to compute the parity while performing the permutations
        // making this much less expensive
        int total = 0;

        for( int i = 0; i < ret.length; i++ ) {
            int val = ret[i];

            for( int j = i+1; j < ret.length; j++ ) {
                if( val > ret[j] ) {
                    total++;
                }
            }
        }

        if( total % 2 == 1 )
            return -1;
        return 1;
    }

    /**
     * Computes N factorial
     */
    public static int fact( int N ) {
        int ret = 1;

        while( N > 0 ) {
            ret *= N--;
        }

        return ret;
    }

    /**
     * Creates a list of all permutations for a set with N elements.
     *
     * @param N Number of elements in the list being permuted.
     * @return A list containing all the permutations.
     */
    public static List<int[]> createList( int N )
    {
        int data[] = new int[ N ];
        for( int i = 0; i < data.length; i++ ) {
            data[i] = -1;
        }

        List<int[]> ret = new ArrayList<int[]>();

        createList(data,0,-1,ret);

        return ret;
    }


    /**
     * Internal function that uses recursion to create the list
     */
    private static void createList( int data[], int k , int level , List<int[]> ret )
    {
        data[k] = level;

        if( level < data.length-1 ) {
            for( int i = 0; i < data.length; i++ ) {
                if( data[i] == -1 ) {
                    createList(data,i,level+1,ret);
                }
            }
        } else {
            int []copy = new int[data.length];
            System.arraycopy(data,0,copy,0,data.length);
            ret.add(copy);
        }
        data[k] = -1;
    }

    /**
     * Creates the next permutation in the sequence.
     *
     * @return An array containing the permutation.  The returned array is modified each time this function is called.
     */
    public int[] next()
    {
        boolean hasNewPerm = false;

        escape:while( level >= 0) {
//            boolean foundZero = false;
            for( int i = iter[level]; i < data.length; i = iter[level] ) {
                iter[level]++;

                if( data[i] == -1 ) {
                    level++;
                    data[i] = level-1;

                    if( level >= data.length ) {
                        // a new permutation has been created return the results.
                        hasNewPerm = true;
                        System.arraycopy(data,0,ret,0,ret.length);
                        level = level-1;
                        data[i] = -1;
                        break escape;
                    } else {
                        valk[level] = i;
                    }
                }
            }

            data[valk[level]] = -1;
            iter[level] = 0;
            level = level-1;

        }

        if( hasNewPerm )
            return ret;
        return null;
    }
}


