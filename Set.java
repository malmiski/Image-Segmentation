
//
// Task 1. Set<T> class (5%)
// This is used in DisjointSets<T> to store actual data in the same sets
//

//You cannot import additonal items
import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.NoSuchElementException;
//You cannot import additonal items

//
//Hint: if you think really hard, you will realize this class Set<T> is in fact just a list
//      because DisjointSets<T> ensures that all values stored in Set<T> must be unique, 
//      but should it be array list or linked list??
//

public class Set<T> extends AbstractCollection<T>
{

	/**
	 * The class to represent a node of a linked list
	 */
	private class ListItem{
		ListItem(T data, ListItem next){
			this.data = data;
			this.next = next;
		}

		/**
		 * The data of node
		 */
		T data;
		/**
		 * The next node in the list
		 */
		ListItem next;
	}

	/**
	 * The dummy node head of the linked list
	 */
	private ListItem head = new ListItem(null, null);
	/**
	 * The tail of the list
	 */
	private ListItem tail = head;
	private int size = 0;


	//O(1)

	/**
	 * Adds an item to the list
	 * @param item the item to add
	 * @return whether the item was added or not
	 */
	public boolean add(T item)
	{
		// If the item is null throw a null pointer exception
		if(item == null){
			throw new NullPointerException();
		}

		/**
		 * Set the next pointer of the c
		 */
		tail.next = new ListItem(item, null);
		tail = tail.next;
		size++;
		return true;
	}
	
	//O(1)
	public boolean addAll(Set<T> other)
	{
		if(other == null){
			throw new NullPointerException();
		}
		tail.next = other.head.next;
		tail = other.tail;
		size += other.size;
		return true;
	}
	
	//O(1)
	public void clear()
	{
		// Remake the list by reseting head to a dummy node
		head = new ListItem(null, null);
		// tail to head
		tail = head;
		// and resetting the size of the list
		size = 0;
	}
	
	//O(1)
	public int size()
	{
		return size;
	}

	/**
	 * Returns an iterator over the list
	 * @return an iterator to traverse the list
	 */
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			/**
			 * Represents the current item being traversed
			 */
			ListItem current = head;

			/**
			 * Returns the data of the next item in the list
			 * @return the data of the next item in the list
			 */
			public T next() {
				if (!hasNext()) {
					throw new NoSuchElementException("No more cars");
				}
				// update the current node pointer
				current = current.next;
				// return the data of the current itemm
				return current.data;
			}

			/**
			 * Determines whether we have anoter item in the list
			 * @return whether another item in the list is there
			 */
			public boolean hasNext() {
				return current.next != null;
			}
		};
	}
}
