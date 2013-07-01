package chain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import chain.element.TypedDep;

/**
 * Utility class for building typed dependencies
 * @author David
 *
 */
public class TypedDepBuilder {

	/**
	 * List of TypedDep in this Builder
	 */
	private List<TypedDep> list;
	
	/**
	 * No-argument constructor
	 */
	public TypedDepBuilder () {
		list = new ArrayList<TypedDep>();
	}
	
	
	/**
	 * Adds a TypedDep to this Builder
	 * @param td TypedDep
	*/	
	public void add (TypedDep td) {
		list.add(td);
	}
	
	/**
	 * Returns the list of TypedDep
	 * @return list of TypedDep
	 */
	public List<TypedDep> getList () {
		return list;
	}
	
	/**
	 * Returns the index of the TypedDep or -1 if TypedDep is not contained within this Builder
	 * <p>
	 * Only checks lemma and type for equality
	 * @param td TypedDep to check
	 * @return index of td
	 * @deprecated
	 */
	@SuppressWarnings("unused")
	private int getEntryIndex (TypedDep td) {
		for (int i = 0; i < list.size(); i++) {
			if ((list.get(i).getLemma().equals(td.getLemma()))&&(list.get(i).getType().equals(td.getType())))
				return i;
		}
		return -1;
	}
	
	/**
	 * Standard toString method
	 */
	public String toString () {
		StringBuilder sb = new StringBuilder();
		for (TypedDep td : list) {
			sb.append(td).append("\n");
		}
		return sb.toString();
	}
	
	/**
	 * Checks whether there is a TypedDep with the same lemma and different type in this Builder
	 * @param td Typed Dependency to check
	 * @return true or false
	 * @deprecated
	 */
	private boolean hasDoublet (TypedDep td) {
		for (TypedDep t : list) {
			if (t.getLemma().equals(td.getLemma())&&(!t.getType().equals(td.getType())))
				return true;
		}
		return false;
	}
	
	/**
	 * Returns the doublet for a given TypedDep
	 * <p>
	 * A doublet is a TypedDep with the same lemma but a different type than the given TypedDep
	 * <br>A call to {@link #hasDoublet(TypedDep)} is recommended before calling this method
	 * @param td TypedDep to check
	 * @return TypedDep doublet
	 * @see #hasDoublet(TypedDep)
	 * @deprecated
	 */
	private int getDoublet (TypedDep td) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getLemma().equals(td.getLemma())&&(!list.get(i).getType().equals(td.getType())))
				return i;
		}
		return -1;
	}
	
	/**
	 * Returns a string representation only containing doublets
	 * <p>
	 * Unpaired elements are left out
	 * @return string representation
	 * @deprecated
	 */
	public String toPairString () {
		Set<TypedDep> hs = new HashSet<TypedDep>();
		StringBuilder sb = new StringBuilder();
		for (TypedDep td : list) {
			if (hasDoublet(td)) {
				hs.add(td);
				hs.add(list.get(getDoublet(td)));
			}
		}
		for (TypedDep td : hs)
			sb.append(td).append("\n");
		return sb.toString();
	}
	
	/**
	 * Returns a formatted string representation only containing doublets
	 * <p>
	 * Unpaired elements are left out
	 * @return string representation
	 * @deprecated
	 */
	public String prettyPrint () {
		Set<TypedDep> hs = new HashSet<TypedDep>();
		StringBuilder sb = new StringBuilder();
		for (TypedDep td : list) {
			if (hasDoublet(td)) {
				if (!hs.contains(td)) {
					sb.append(td).append("\t").append(list.get(getDoublet(td))).append("\n");
				}
				hs.add(td);
				hs.add(list.get(getDoublet(td)));
			}
		}
		
		return sb.toString();
	}
}
