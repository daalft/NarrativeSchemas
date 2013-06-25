package chain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import chain.element.TypedDep;
import edu.stanford.nlp.ling.IndexedWord;

public class NarrativeChain {

	private List<TypedDep> chain;

	public NarrativeChain () {
		chain = new ArrayList<TypedDep>();
	}

	public void buildChain (List<TypedDep> dep, IndexedWord entity) {
		for (TypedDep td : dep) {
			if (td.getDep().lemma().equals(entity.lemma())) {
				chain.add(td);
			}
		}
	}

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
		Map<String, Integer> m = new TreeMap<String, Integer>();
		for (TypedDep td : dep) {
			
				m.put(td.getDep().word(), (m.get(td.getDep().word())==null)?1:m.get(td.getDep().word())+1);
			
		}
		int max = Collections.max(m.values());
		for (Entry<String, Integer> entry : m.entrySet()) {
			if (entry.getValue()==max) {
				return entry.getKey();
			}
		}
		return "";
	}

	public String toString () {
		StringBuilder sb = new StringBuilder();
		for (TypedDep td : chain) {
			sb.append(td.toString()).append("\n");
		}
		return sb.toString();
	}

	public boolean empty () {
		return chain.isEmpty();
	}

	public String toLinearString() {
		StringBuilder sb = new StringBuilder();
		for (TypedDep td : chain) {
			sb.append(td.toString()).append(":");
		}
		return sb.toString();
	}
}
