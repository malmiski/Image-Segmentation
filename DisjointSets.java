
// Task 1. DisjointSets class (10%)

// hint: you can use the DisjointSets from your textbook

import java.util.ArrayList;

// disjoint sets class, using union by size and path compression.

/**
 * An implementation of the disjoint set data structure
 * with union by
 * @param <T> type of the sets
 */
public class DisjointSets<T>
{
    private int size = 0;
    //Constructor
    public DisjointSets( ArrayList<T> data  )
    {
      //your code here
        s = new int[data.size()];
        for(int i = 0; i<data.size(); i++){
            s[i] = -1;
        }
        sets = new ArrayList<Set<T>>(data.size());
        Set<T> set;
        for(T item : data){
            set = new Set<T>();
            set.add(item);
            sets.add(set);
        }
        size = data.size();
    }
    
    //Must have O(1) time complexity

    /**
     * Unions the sets represented by the ids root1 and root2
     * @param root1 the id of the first set
     * @param root2 the id of the second set
     * @return the unioned set of root1 and root2
     * @throws RuntimeException if the roots are the same id or if one of them is not a root
     */
    public int union( int root1, int root2 )
    {
        // If the roots are the same throw an exception
        if(root1 == root2){
            throw new RuntimeException("Must be roots of a set");
        }
        // If any of the roots given are not actually roots throw an exception
		if(!isRoot(root1) || !isRoot(root2)){
            throw new RuntimeException("Must be roots of a set");
        }
        // Decrease the size of the number of sets as we are going to combine two sets together
        size--;
		// If the second root is greater in size
        if(s[root2] < s[root1]){
            // add the size of root1 to root2
            s[root2] += s[root1];
            // And set root1's root to root2
		    s[root1] = root2;
		    // And add the data from root1 to root2
		    sets.get(root2).addAll(sets.get(root1));
//		    sets.set(root1, null);
		    return root2;
        }else{
            // Otherwise add the size of root2 to root1
            s[root1] += s[root2];
            // And set root2's root to root1
            s[root2] = root1;
            // And add the data fromm root2 to root1
            sets.get(root1).addAll(sets.get(root2));
//            sets.set(root2, null);
            return root1;
        }
    }

    //Must implement path compression
    /**
     * Returns the root of the set that the index x belongs to
     * @param x the index of the item you want to see the root of
     * @return the root of the set that x belongs to
     */
    public int find( int x )
    {
        if(!isItem(x)){
            throw new RuntimeException("not an item");
        }
        if(s[x] < 0){
            return x;
        }else{
            return s[x] = find(s[x]);
        }

    }

    //Get all the data in the same set
    //Must have O(1) time complexity
    public Set<T> get( int root )
    {
        // Return the set with the index root
        return sets.get(root);

    }
	
	//return the number of disjoint sets remaining
    // must be O(1) time

    /**
     * Return the number of non-disjoint sets in the disjoint set
     * @return
     */
	public int getNumSets()
	{
		return size;
	}

    //Data
    private int [ ] s;
    private ArrayList<Set<T>> sets;

    /**
     * Convenience method to determine whether an index is within our set
     * @param item the index of the item
     * @return whether it is in the disjoint set
     */
    private boolean isItem(int item){
        return ((item >= 0) && item < s.length);
    }

    /**
     * Determins whether the index given by root is an item and is a root of a set
     * @param root the index to consider
     * @return whether the index was an item and the root of a set
     */
    private boolean isRoot(int root){
        if(!isItem(root)){
            return false;
        }
        return (s[root] < 0);
    }
}
