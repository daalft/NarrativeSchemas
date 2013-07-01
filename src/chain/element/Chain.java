package chain.element;

import java.util.ArrayList;
import java.util.List;
/**
 * Class used to represent narrative chains as list of events
 * @author David
 */
public class Chain implements Comparable<Chain> {
	/**
	 * Events in this chain
	 */
	private List<Event> events;
	/**
	 * Score 
	 */ 
	private double score;
	
	/**
	 * No-argument constructor
	 */ 
	public Chain () {
		// initialize components
		events = new ArrayList<Event>();
	}
	
	/**
	 * Returns all events in this chain
	 * @return events in this chain
	 */ 
	public List<Event> getEvents () {
		return events;
	}
	
	/**
	 * Increments the score of this chain
	 * @param d score to add
	 */ 
	public void setScore (double d) {
		score += d;
	}
	
	/**
	 * Returns the score of this chain
	 * @return score
	 */ 
	public double getScore () {
		return score;
	}
	
	/**
	 * Method for retrieving Event by index position
	 * <p>
	 * If the given index would result in an 
	 * IndexOutOfBoundsException, null is returned
	 * @param i index
	 * @return event at position i
	 */
	public Event get (int i) {
		// if index is smaller or equal event size
		if (events.size()-1 >= i)
			// return desired event
			return events.get(i);
		// else return null
		return null;
	}
	
	/**
	 * Adds an event to this chain
	 * <p>
	 * The event is only added, if this chain 
	 * does not already contain the event
	 * @param e event to add
	 */ 
	public void add (Event e) {
		if (!events.contains(e))
			events.add(e);
	}
	
	/**
	 * Method to check whether this chain contains a given event
	 * @param e event to check
	 * @return true if event is in chain
	 */ 
	public boolean contains (Event e) {
		for (Event ev : events) {
			if (e.equals(ev))
				return true;
		}
		return false;
	}
	
	/**
	 * Standard toString method
	 */ 
	@Override
	public String toString () {
		StringBuilder sb = new StringBuilder("[ ");
		for (Event e : events)
			sb.append(e.getTypedDep()).append(" ");
		sb.append("]");
		return sb.toString();
	}

	/**
	 * Comparison of scores between chains
	 */ 
	@Override
	public int compareTo(Chain o) {
		if (this.score < o.score)
			return 1;
		if (this.score > o.score)
			return -1;
		return 0;
	}
}
