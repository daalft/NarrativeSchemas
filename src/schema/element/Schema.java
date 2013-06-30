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

public class Schema implements Comparable<Schema> {

	private List<Chain> chains;
	private static int counter;
	private int id;
	private double score;

	public Schema () {
		chains = new ArrayList<Chain>();
		id = ++counter;
	}

	public int getLongestChainCount () {
		List<Integer> counts = new ArrayList<Integer>();
		for (Chain c : chains)
			counts.add(c.getEvents().size());
		return Collections.max(counts);
	}
	
	public void incrementScore (double d) {
		score += d;
	}
	public List<Chain> getChains() {
		return chains;
	}
	public int getID () {
		return id;
	}
	public void add (Event e, int pos) {
		if (!containsVerb(e.getVerb()))
			score += e.getMaxScore();
		if (pos < chains.size()) {
			chains.get(pos).add(e);
		}
		else {
			Chain c = new Chain();
			c.add(e);
			chains.add(c);
		}
	}

	private boolean containsVerb (String v) {
		for (Chain c : chains)
			for (Event e : c.getEvents())
				if (e.getVerb().equals(v))
					return true;
		return false;
	}
	
	public boolean contains (Event e) {
		for (Chain c : chains) {
			if (c.contains(e))
				return true;
		}
		return false;
	}
	public int getChainCount () {
		return chains.size();
	}

	public int getVerbCount () {
		Set<String> verbs = new HashSet<String>();
		for (Chain c : chains) {
			for (Event e : c.getEvents()) {
				verbs.add(e.getVerb());
			}
		}
		return verbs.size();
	}

	private List<Entry<String, Double>> getScoreMap () {
		Map<String, Double> sm = new HashMap<String, Double>();
		for (Chain c : chains) {
			List<Event> le = c.getEvents();
			for (Event e : le) {
				String key = e.getVerb();
				if (!sm.containsKey(key)) {
					sm.put(key, e.getMaxScore());
					
				}
			}
		}
		List<Entry<String, Double>> es = new ArrayList<Entry<String, Double>>(sm.entrySet());
		Collections.sort(es, new Comparator <Entry<String, Double>>() {
			@Override
			public int compare(Entry<String, Double> o1,
					Entry<String, Double> o2) {
				return (o2.getValue().compareTo(o1.getValue()));
			}
			
		});
		return es;
	}
	
	public String toString () {
		Collections.sort(chains);
		List<Entry<String, Double>> scores = getScoreMap();
		StringBuilder sb = new StringBuilder("*****" + System.lineSeparator());
		sb.append("score="+String.format("%.6f",score)).append(System.lineSeparator());
		sb.append("Events: ");
		for (Entry<String, Double> e : scores) {
			sb.append(e.getKey()).append(" ");
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append(System.lineSeparator());
		sb.append("Scores: ");
		for (Entry<String, Double> e : scores) 
			sb.append(String.format("%.3f",e.getValue())).append(" ");
		sb.deleteCharAt(sb.length()-1);
		sb.append(System.lineSeparator());
		for (Chain c : chains) {
			Set<String> ms = new HashSet<String>();
			List<String> mentionpool = new ArrayList<String>();
			for (Event e : c.getEvents()) {
				mentionpool.addAll(e.getMentions());
				
			}// retain mentions with count > 1
			for (int i = 0; i <  mentionpool.size(); i++) {
				if (Collections.frequency(mentionpool, mentionpool.get(i)) > 1) {
					ms.add(mentionpool.get(i));
				}
			}
			sb.append(c.toString());
			sb.append(" ( ");
			for (String mention : ms) {
				sb.append(mention).append(" ");
			}
			sb.append(")");
			sb.append(System.lineSeparator());
		}
		sb.append(System.lineSeparator());

		return sb.toString();
	}

	@Override
	public int compareTo(Schema o) {
		if (this.score > o.score)
			return 1;
		if (this.score < o.score)
			return -1;
		return 0;
	}
}
