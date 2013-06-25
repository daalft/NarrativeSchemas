package chain.element;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class Event implements Serializable, Comparable<Event> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7900717167065083054L;
	private String typedDep, mention, id;
	private Set<String> mentions;
	private int globalScore = 0;
	
	public Event () {
		mentions = new HashSet<String>();
	}

	public void incrementScore (int i) {
		globalScore += i;
	}
	
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

	public String getMention (int pos) {
		String[] s = mentions.toArray(new String[0]);
		return s[pos];
	}
	
	public Event (String d, String s, String i) {
		typedDep = d;
		mention = s;
		id = i;
	}

	public void addMention (String mention) {
		mentions.add(mention);
	}
	
	public Set<String> getMentions () {
		return mentions;
	}
	
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

	public String getTypedDep() {
		return typedDep;
	}

	public void setTypedDep(String typedDep) {
		this.typedDep = typedDep;
	}

	public String getMention() {
		return mention;
	}

	public void setMention(String mention) {
		this.mention = mention;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

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

	@Override
	public int compareTo(Event e) {
		if (e.globalScore < this.globalScore)
			return -1;
		if (e.globalScore > this.globalScore)
			return 1;
		return 0;
	}
}
