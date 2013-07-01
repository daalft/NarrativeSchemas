package chain.element;
/**
 * Class representing a pair of events with the PMI value
 * @author David
 */
public class Pair implements Comparable <Pair>{
	/**
	 * Events in this pair
	 */
	private Event e1, e2;
	/**
	 * PMI score
	 */
	private double pmi;
	/**
	 * Header information
	 */
	private boolean head;
	/**
	 * Header information
	 */
	private String h;

	/**
	 * No-argument constructor
	 */
	public Pair () {

	}
	
	/**
	 * Head constructor
	 * @param id id
	 */
	public Pair (String id) {
		head = true;
		h = id;
	}

	/**
	 * Returns true if this is not a pair, but a header
	 * @return true if header
	 */
	public boolean isHeader () {
		return head;
	}

	/**
	 * Pair constructor
	 * @param e1 event 1
	 * @param e2 event 2
	 * @param pmi2 pmi value
	 */
	public Pair(Event e1, Event e2, double pmi2) {
		this.e1 = e1;
		this.e2 = e2;
		this.pmi = pmi2;
	}
	
	/**
	 * Returns event 1
	 * @return event 1
	 */
	public Event getE1() {
		return e1;
	}
	
	/**
	 * Sets event 1
	 * @param e event
	 */
	public void setE1(Event e) {
		this.e1 = e;
	}
	
	/**
	 * Returns event 2
	 * @return event 2
	 */
	public Event getE2() {
		return e2;
	}
	
	/**
	 * Sets event 2
	 * @param e event
	 */
	public void setE2(Event e) {
		this.e2 = e;
	}
	
	/**
	 * Returns PMI value
	 * @return pmi value
	 */
	public double getPmi() {
		return pmi;
	}
	
	/**
	 * Sets PMI value
	 * @param pmi pmi
	 */
	public void setPmi(double pmi) {
		this.pmi = pmi;
	}

	/**
	 * toString method
	 * @return string representation
	 */
	public String toString () {
		if (head) {
			return h;
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append(e1).append("\t").append(e2);
			sb.append("\t").append(pmi);
			return sb.toString();
		}
	}

	/**
	 * Comparison between PMI values
	 */
	@Override
	public int compareTo(Pair p) {
		if (pmi < p.pmi)
			return -1;
		if (pmi > p.pmi)
			return 1;
		return 0;
	}

	/**
	 * Returns header ID
	 * @return header ID
	 */
	public String getH() {
		return h;
	}

	/**
	 * Sets header ID
	 * @param h header id
	 */
	public void setH(String h) {
		this.h = h;
	}
}
