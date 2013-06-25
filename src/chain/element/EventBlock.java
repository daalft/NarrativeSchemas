package chain.element;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EventBlock implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4560149494312112667L;
	private List<Event> events;
	
	public EventBlock () {
		events = new ArrayList<Event>();
	}
	
	public EventBlock (EventBlock e) {
		events = e.getEvents();
	}
	
	public void add (Event e) {
		events.add(e);
	}
	
	public List<Event> getEvents () {
		return events;
	}

	public String toString () {
		StringBuilder sb = new StringBuilder();
		for (Event e : events) {
			sb.append(e.toString()).append("\n");
		}
		sb.append("\n");
		return sb.toString();
	}
}
