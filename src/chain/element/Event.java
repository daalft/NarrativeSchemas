package chain.element;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
/**
 * Class to represent an event as a typed dependency 
 * consisting of a verb lemma, an ID and one or more mentions.
 */
public class Event implements Serializable, Comparable<Event> {

	/**
	 * Serial ID
	 */
	private static final long serialVersionUID = -7900717167065083054L;
	/**
	 * Information
	 */
	private String typedDep, mention, id;
	/**
	 * List of mentions
	 */
	private Set<String> mentions;
	/**
	 * Internal event score
	 */
	private int globalScore = 0;
	/**
	 * Maximum event score as scored against a chain
	 */
	private double maxScore = 0.0;
	
	/**
	 * No-argument constructor
	 */
	public Event () {
		mentions = new HashSet<String>();
	}

	/**
	 * Sets the maximum score for this Event
	 * @param d score
	 */
	public void setMaxScore (double d) {
		maxScore = d;
	}
	
	/**
	 * Returns the maximum score for this Event
	 * @return max score
	 */
	public double getMaxScore () {
		return maxScore;
	}
	
	/**
	 * Increments this score by the given value <em>i</em>
	 * @param i value
	 */
	public void incrementScore (int i) {
		globalScore += i;
	}
	
	/**
	 * Constructor with full information
	 * @param information full information
	 */
	public Event (String information) {
		String[] sp = information.split(" ");
		if (sp.length < 2)
			System.err.println("Problem with " + information);
		typedDep = sp[0].trim();
		int i = typedDep.length()-1;
		typedDep = typedDep.substring(0, i);
		mention = sp[1];
		id = sp[4];
	}

	/**
	 * Returns the mention of this Event at position <em>pos</em>
	 * @param pos position
	 * @return mention
	 */
	public String getMention (int pos) {
		String[] s = mentions.toArray(new String[0]);
		return s[pos];
	}
	
	/**
	 * Constructor with full, explicit information
	 * @param d dependency
	 * @param s mention
	 * @param i id
	 */
	public Event (String d, String s, String i) {
		typedDep = d;
		mention = s;
		id = i;
	}

	/**
	 * Adds a mention to the list of mentions in this Event
	 * @param mention mention
	 */
	public void addMention (String mention) {
		mentions.add(mention);
	}
	
	/**
	 * Returns all mentions of this Event
	 * @return list of mentions
	 */
	public Set<String> getMentions () {
		return mentions;
	}
	
	/**
	 * Returns only the verb lemma of this Event's typed dependency
	 * @return verb lemma
	 */
	public String getVerb () {
		String[] sp = typedDep.split("-");
		return sp[0];
	}

	/**
	 * Method for retrieving the dependency part of this event
	 * <p>
	 * For prepositional arguments, the parameter <em>full</em>
	 * specifies whether the full argument <i>(e.g. p_of)</i>
	 * or only <i>p</i> is returned. A value of <b>true</b>
	 * returns the full argument.
	 * @param full flag
	 * @return dependency
	 */
	public String getDependency (boolean full) {
		String[] sp = typedDep.split("-");
		String d = sp[1];
		if (d.startsWith("p_"))
			if (full)
				return d;
			else
				return "p";
		return d;
	}

	/**
	 * Returns the typed dependency of this Event
	 * <p>
	 * Typed dependency consists of verb lemma and 
	 * dependency (subject, object, preposition)
	 * @return typed dependency
	 */
	public String getTypedDep() {
		return typedDep;
	}

	/**
	 * Sets the typed dependency of this Event
	 * @param typedDep typed dependency
	 */
	public void setTypedDep(String typedDep) {
		this.typedDep = typedDep;
	}

	/**
	 * Returns the mention of this Event
	 * @return mention
	 */
	public String getMention() {
		return mention;
	}

	/**
	 * Sets the mention of this Event
	 * @param mention mention
	 */
	public void setMention(String mention) {
		this.mention = mention;
	}
	
	/**
	 * Returns the ID of this Event
	 * @return ID
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the ID of this Event
	 * @param id id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * toString method
	 * @return string representation
	 */
	public String toString () {
		StringBuilder sb = new StringBuilder();
		sb.append(typedDep);
		if (mention != null) {
			sb.append(" (").append(mention).append(") [");
		sb.append(id).append("]");
		} else {
			sb.append(" ( ");
			for (String s : mentions)
				sb.append(s).append(" ");
			sb.append(")");
		}
		return sb.toString();
	}

	/**
	 * Modified equals method
	 * <p>
	 * Returns true if the typed dependencies <i>(verb argument pair)</i> between two events are the same
	 */
	@Override
	public boolean equals (Object o) {
		Event f = (Event)o;
		if (f.getTypedDep().equals(typedDep))
			return true;
		return false;
	}

	/**
	 * Comparison based on global score
	 */
	@Override
	public int compareTo(Event e) {
		if (e.globalScore < this.globalScore)
			return -1;
		if (e.globalScore > this.globalScore)
			return 1;
		return 0;
	}

}
