package chain.element;

import edu.stanford.nlp.ling.IndexedWord;

/**
 * Class that holds information about a typed dependency
 * @author David
 *
 */
public class TypedDep {

	/**
	 * Information about the typed dependency
	 */
	private String lemma, type, idPrefix;
	/**
	 * Dependency
	 */
	private IndexedWord dep;
	/**
	 * Canonical mention
	 */
	private String mention;
	
	/**
	 * No-argument constructor
	 */
	public TypedDep () {

	}
	
	/**
	 * Constructor
	 * @param l lemma
	 * @param t type
	 * @param d dependency
	 * @param id document ID
	 */
	public TypedDep (String l, String t, IndexedWord d, String id) {
		this();
		idPrefix = id;
		lemma = l;
		type = t;
		dep = d;
		mention = createId(d);
	}
	
	/**
	 * Method to create unique IDs
	 * <p>
	 * The ID is based on the IndexedWord's document ID, sentence index and begin position
	 * @param iw IndexedWord
	 * @return unique ID
	 */
	private String createId (IndexedWord iw) {
		return idPrefix + iw.docID() + iw.sentIndex() + iw.beginPosition();
	}
	
	/**
	 * Sets the lemma for this typed dependency
	 * @param l lemma
	 */
	public void setLemma (String l) {
		lemma = l;
	}
	
	/**
	 * Sets the type for this typed dependency
	 * @param t type
	 */
	public void setType (String t) {
		type = t;
	}
	
	/**
	 * Sets the dependency for this typed dependency
	 * @param d dependency
	 */
	public void setDep (IndexedWord d) {
		dep = d;
		mention = createId(d);
	}
	
	/**
	 * Returns the lemma of this typed dependency
	 * @return lemma
	 */
	public String getLemma () {
		return lemma;
	}
	
	/**
	 * Returns the type of this typed dependency
	 * @return type
	 */
	public String getType () {
		return type;
	}
	
	/**
	 * Returns the initial dependency of this typed dependency
	 * @return dependency
	 */
	public IndexedWord getDep () {
		return dep;
	}
		
	/**
	 * Standard toString method
	 */
	public String toString () {
		// if the dependency has an associated lemma, use lemma, else use word form
		String dep_lemma = dep.lemma()==null?dep.word():dep.lemma();
		StringBuilder sb = new StringBuilder(lemma + "-" + type + "( " + dep_lemma + " ");
		sb.append(") [ ");
		sb.append(mention).append(" ");
		sb.append("]");
		return sb.toString();
	}
	
	/**
	 * Returns a copy of this typed dependency with the given type as type
	 * @param type type
	 * @return copy of this
	 */
	TypedDep copyWithType (String type) {
		TypedDep o = new TypedDep();
		o.setLemma(this.getLemma());
		o.setDep(this.getDep());
		o.setType(type);
		o.mention = createId(this.getDep());
		return o;
	}


	
	public String getMention () {
		return mention;
	}
}
