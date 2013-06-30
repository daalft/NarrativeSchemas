package chain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import chain.element.TypedDep;
import edu.stanford.nlp.ling.IndexedWord;

/**
 * Class to represent a narrative chain as a list of typed dependencies
 * @author David
 *
 */
public class NarrativeChain {

	/**
	 * Chains
	 */
	private List<TypedDep> chain;

	/**
	 * No-argument constructor
	 */
	public NarrativeChain () {
		// initialize components
		chain = new ArrayList<TypedDep>();
	}

	/**
	 * Builds a chain with a given protagonist
	 * <p>
	 * Adds the typed dependency from the given list where the 
	 * typed dependency contains the given protagonist
	 * @param dep dependency
	 * @param entity protagonist
	 */
	public void buildChain (List<TypedDep> dep, IndexedWord entity) {
		// for all typed dependencies
		for (TypedDep td : dep) {
			// if typed dependency lemma equals given protagonist lemma
			if (td.getDep().lemma().equals(entity.lemma())) {
				// add to chain
				chain.add(td);
			}
		}
	}

	/**
	 * Returns all typed dependencies in this chain
	 * @return list of typed dependencies
	 */
	public List<TypedDep> getDeps () {
		return chain;
	}
	
	/**
	 * Returns the most mentioned entity in the list of dependencies
	 * @param dep List of dependencies
	 * @return protagonist
	 * @deprecated
	 */
	String findProtagonist (List<TypedDep> dep) {
		// local map
		Map<String, Integer> m = new TreeMap<String, Integer>();
		// for all typed dependencies
		for (TypedDep td : dep) {
				// add frequency to map
				m.put(td.getDep().word(), (m.get(td.getDep().word())==null)?1:m.get(td.getDep().word())+1);
		}
		// find most occurring word
		int max = Collections.max(m.values());
		// return most occurring word
		for (Entry<String, Integer> entry : m.entrySet()) {
			if (entry.getValue()==max) {
				return entry.getKey();
			}
		}
		// method should not reach this point
		return "";
	}

	/**
	 * Standard toString method
	 */
	public String toString () {
		StringBuilder sb = new StringBuilder();
		for (TypedDep td : chain) {
			sb.append(td.toString()).append("\n");
		}
		return sb.toString();
	}

	/**
	 * Returns whether this chain contains elements
	 * <p>
	 * If this chain contains no elements, returns <em>false</em><br/>
	 * If this chain contains at least one element, returns <em>true</em>
	 * @return whether this chain contains elements
	 */
	public boolean empty () {
		return chain.isEmpty();
	}

	/**
	 * Alternative toString method
	 * @return linear string representation
	 */
	public String toLinearString() {
		StringBuilder sb = new StringBuilder();
		for (TypedDep td : chain) {
			sb.append(td.toString()).append(":");
		}
		return sb.toString();
	}
}
