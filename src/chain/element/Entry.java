package chain.element;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
/**
 * Class to represent entries in a database.
 * @author David
 */ 
public class Entry implements Serializable {

	/**
	 * Serial ID
	 */
	private static final long serialVersionUID = -7432809786895316875L;
	/**
	 * ID
	 */ 
	private String id;
	/**
	 * List of EventBlock
	 */
	private List<EventBlock> eb;
	
	/**
	 * No-argument constructor
	 */
	public Entry () {
		eb = new ArrayList<EventBlock>();
	}
	
	/**
	 * Constructor with ID
	 */
	public Entry (String i) {
		this();
		id = i;
	}
	
	/**
	 * Constructor with Entry
	 */
	public Entry (Entry e) {
		id = e.getId();
		eb = e.getEventBlock();
	}
	
	/**
	 * Method to add an EventBlock to this Entry
	 * @param e EventBlock to add
	 */
	public void addEvent (EventBlock e) {
		if (!eb.contains(e))
			eb.add(e);
	}
	
	/**
	 * Sets the ID for this Entry
	 * @param i id
	 */
	public void setId (String i) {
		id = i;
	}
	
	/**
	 * Returns the list of EventBlock in this Entry
	 * @return list of EventBlock
	 */
	public List<EventBlock> getEventBlock () {
		return eb;
	}
	
	/**
	 * Returns the ID of this Entry
	 * @return ID
	 */
	public String getId () {
		return id;
	}
	
	/**
	 * Standard toString method
	 * @return string representation
	 */
	public String toString () {
		StringBuilder sb = new StringBuilder(id);
		sb.append("\n");
		for (EventBlock e : eb) {
			sb.append(e.toString());
		}
		return sb.toString();
	}
}
