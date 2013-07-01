package chain.element;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
/**
* Class used to represent a block of events
* @author David
*/
public class EventBlock implements Serializable {

	/**
	 * Serial ID
	 */
	private static final long serialVersionUID = 4560149494312112667L;
	/**
	 * Events in this block
	 */
	private List<Event> events;
	
	/**
	 * No-argument constructor
	 */
	public EventBlock () {
		events = new ArrayList<Event>();
	}
	
	/**
	 * Constructor with EventBlock
	 * @param e EventBlock
	 */
	public EventBlock (EventBlock e) {
		events = e.getEvents();
	}
	
	/**
	 * Adds an Event to this EventBlock
	 * @param e event to add
	 */
	public void add (Event e) {
		events.add(e);
	}
	
	/**
	 * Returns all events in this block
	 * @return list of Event
	 */
	public List<Event> getEvents () {
		return events;
	}

	/**
	 * toString method
	 * @return string representation
	 */
	public String toString () {
		StringBuilder sb = new StringBuilder();
		for (Event e : events) {
			sb.append(e.toString()).append("\n");
		}
		sb.append("\n");
		return sb.toString();
	}
}
