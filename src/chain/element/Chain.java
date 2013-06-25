package chain.element;

import java.util.ArrayList;
import java.util.List;

public class Chain implements Comparable<Chain> {

	private List<Event> events;
	private double score;
	
	public Chain () {
		events = new ArrayList<Event>();
	}
	
	public List<Event> getEvents () {
		return events;
	}
	
	public void setScore (double d) {
		score += d;
	}
	
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
	
	public void add (Event e) {
		if (!events.contains(e))
			events.add(e);
	}
	
	public boolean contains (Event e) {
		for (Event ev : events) {
			if (e.equals(ev))
				return true;
		}
		return false;
	}
	@Override
	public String toString () {
		StringBuilder sb = new StringBuilder("[ ");
		for (Event e : events)
			sb.append(e.getTypedDep()).append(" ");
		sb.append("]");
		return sb.toString();
	}

	@Override
	public int compareTo(Chain o) {
		if (this.score < o.score)
			return 1;
		if (this.score > o.score)
			return -1;
		return 0;
	}
}
