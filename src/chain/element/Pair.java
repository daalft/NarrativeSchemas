package chain.element;

public class Pair implements Comparable <Pair>{

	private Event e1, e2;

	private double pmi;

	private boolean head;
	private String h;

	public Pair () {

	}

	public Pair (String id) {
		head = true;
		h = id;
	}

	public boolean isHeader () {
		return head;
	}

	public Pair(Event e1, Event e2, double pmi2) {
		this.e1 = e1;
		this.e2 = e2;
		this.pmi = pmi2;
	}
	public Event getE1() {
		return e1;
	}
	public void setE1(Event e) {
		this.e1 = e;
	}
	public Event getE2() {
		return e2;
	}
	public void setE2(Event e) {
		this.e2 = e;
	}
	public double getPmi() {
		return pmi;
	}
	public void setPmi(double pmi) {
		this.pmi = pmi;
	}

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

	@Override
	public int compareTo(Pair p) {
		if (pmi < p.pmi)
			return -1;
		if (pmi > p.pmi)
			return 1;
		return 0;
	}

	public String getH() {
		return h;
	}

	public void setH(String h) {
		this.h = h;
	}
}
