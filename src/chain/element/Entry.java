package chain.element;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Entry implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7432809786895316875L;
	private String id;
	private List<EventBlock> eb;
	
	public Entry () {
		eb = new ArrayList<EventBlock>();
	}
	
	public Entry (String i) {
		this();
		id = i;
	}
	
	public Entry (Entry e) {
		id = e.getId();
		eb = e.getEventBlock();
	}
	public void addEvent (EventBlock e) {
		if (eb.contains(e)) {} 
		else
		eb.add(e);
	}
	
	public void setId (String i) {
		id = i;
	}
	
	public List<EventBlock> getEventBlock () {
		return eb;
	}
	
	public String getId () {
		return id;
	}
	
	public String toString () {
		StringBuilder sb = new StringBuilder(id);
		sb.append("\n");
		for (EventBlock e : eb) {
			sb.append(e.toString());
		}
		return sb.toString();
	}
}
