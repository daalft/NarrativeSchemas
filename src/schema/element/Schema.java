package schema.element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import chain.element.Chain;
import chain.element.Event;

/**
* Class used to represent a Schema as a list of chains
* @author David
*/
public class Schema implements Comparable<Schema> {

	/**
	 * Chains
	 */
	private List<Chain> chains;
	/**
	 * Counter
	 */
	private static int counter;
	/**
	 * ID
	 */
	private int id;
	/**
	 * Global score
	 */
	private double score;

	/**
	 * No-argument constructor
	 */
	public Schema () {
		// initialize components
		chains = new ArrayList<Chain>();
		// generate ID
		id = ++counter;
	}

	/**
	 * Returns the length of the longest chain
	 * @return length of longest chain
	 */
	public int getLongestChainCount () {
		List<Integer> counts = new ArrayList<Integer>();
		for (Chain c : chains)
			counts.add(c.getEvents().size());
		return Collections.max(counts);
	}
	
	/**
	 * Increments the score of this schema
	 * @param d value
	 */
	public void incrementScore (double d) {
		score += d;
	}
	
	/**
	 * Returns the chains in this schema
	 * @return chains
	 */
	public List<Chain> getChains() {
		return chains;
	}
	
	/**
	 * Returns the ID of this schema
	 * @return id
	 */
	public int getID () {
		return id;
	}
	
	/**
	 * Adds an event at the specified position
	 * <p>
	 * If <em>pos</em> is a valid position, the event
	 * is added at that position. If <em>pos</em> does not 
	 * point to a valid position, a new chain will be created
	 * and the event will be added to that chain
	 * @param e event to add
	 * @param pos position
	 */
	public void add (Event e, int pos) {
		// increment score if this verb is not yet in the schema
		if (!containsVerb(e.getVerb()))
			score += e.getMaxScore();
		// check is pos is a valid position
		if (pos < chains.size()) {
			chains.get(pos).add(e);
		}
		else {
			Chain c = new Chain();
			c.add(e);
			chains.add(c);
		}
	}

	/**
	 * Returns whether this schema contains a given verb
	 * @param v verb to check
	 * @return whether this schema contains the given verb
	 */
	private boolean containsVerb (String v) {
		for (Chain c : chains)
			for (Event e : c.getEvents())
				if (e.getVerb().equals(v))
					return true;
		return false;
	}
	
	/**
	 * Returns whether this schema contains a given event
	 * @param e event to check
	 * @return whether this schema contains the given event
	 */
	public boolean contains (Event e) {
		for (Chain c : chains) {
			if (c.contains(e))
				return true;
		}
		return false;
	}
	
	/**
	 * Returns the number of chains in this schema
	 * @return number of chains
	 */
	public int getChainCount () {
		return chains.size();
	}

	/**
	 * Returns the number of distinct verb in this schema
	 * @return number of verbs
	 */
	public int getVerbCount () {
		Set<String> verbs = new HashSet<String>();
		for (Chain c : chains) {
			for (Event e : c.getEvents()) {
				verbs.add(e.getVerb());
			}
		}
		return verbs.size();
	}

	/**
	 * Returns a sorted list of key-value pairs of the verbs and their scores
	 * @return key-value list
	 */
	private List<Entry<String, Double>> getScoreMap () {
		// inner map
		Map<String, Double> sm = new HashMap<String, Double>();
		// for all chains
		for (Chain c : chains) {
			// get the events
			List<Event> le = c.getEvents();
			// for all events
			for (Event e : le) {
				// get key (=verb)
				String key = e.getVerb();
				// if map does not contain key
				if (!sm.containsKey(key)) {
					// put key and score into map
					sm.put(key, e.getMaxScore());	
				}
			}
		}
		// convert map to list
		List<Entry<String, Double>> es = new ArrayList<Entry<String, Double>>(sm.entrySet());
		// sort list according to values (not keys)
		Collections.sort(es, new Comparator <Entry<String, Double>>() {
			@Override
			public int compare(Entry<String, Double> o1,
					Entry<String, Double> o2) {
				return (o2.getValue().compareTo(o1.getValue()));
			}
			
		});
		return es;
	}
	
	/**
	 * toString method
	 * @return string representation
	 */
	public String toString () {
		// sort chains
		Collections.sort(chains);
		// get verbs with scores
		List<Entry<String, Double>> scores = getScoreMap();
		// initialize string builder
		StringBuilder sb = new StringBuilder("*****" + System.lineSeparator());
		// append score of schema
		sb.append("score="+String.format("%.6f",score)).append(System.lineSeparator());
		// list all distinct verbs
		sb.append("Events: ");
		for (Entry<String, Double> e : scores) {
			sb.append(e.getKey()).append(" ");
		}
		// delete last character of stringbuilder (which is whitespace)
		sb.deleteCharAt(sb.length()-1);
		sb.append(System.lineSeparator());
		// append scores of verbs
		sb.append("Scores: ");
		for (Entry<String, Double> e : scores) 
			sb.append(String.format("%.3f",e.getValue())).append(" ");
		sb.deleteCharAt(sb.length()-1);
		sb.append(System.lineSeparator());
		// for all chains
		for (Chain c : chains) {
			// inner set of mentions
			Set<String> ms = new HashSet<String>();
			// mention pool
			List<String> mentionpool = new ArrayList<String>();
			// add all mentions of all events of this chain to mention pool
			for (Event e : c.getEvents()) {
				mentionpool.addAll(e.getMentions());
				
			}// retain mentions with count > 1
			for (int i = 0; i <  mentionpool.size(); i++) {
				if (Collections.frequency(mentionpool, mentionpool.get(i)) > 1) {
					ms.add(mentionpool.get(i));
				}
			}
			// append chain representation
			sb.append(c.toString());
			// append all mentions with count > 1
			sb.append(" ( ");
			for (String mention : ms) {
				sb.append(mention).append(" ");
			}
			sb.append(")");
			sb.append(System.lineSeparator());
		}
		sb.append(System.lineSeparator());
		// return
		return sb.toString();
	}

	/**
	 * Comparison based on score
	 */
	@Override
	public int compareTo(Schema o) {
		if (this.score > o.score)
			return 1;
		if (this.score < o.score)
			return -1;
		return 0;
	}
}
