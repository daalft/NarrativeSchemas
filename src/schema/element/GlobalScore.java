package schema.element;

public class GlobalScore implements Comparable<GlobalScore>{

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public Score getS() {
		return s;
	}

	public void setS(Score s) {
		this.s = s;
	}

	public Score getO() {
		return o;
	}

	public void setO(Score o) {
		this.o = o;
	}

	public Score getP() {
		return p;
	}

	public void setP(Score p) {
		this.p = p;
	}

	private double score;
	private Score s, o, p;
	
	public GlobalScore() {
	}
	
	public GlobalScore (double d, Score s, Score o, Score p) {
		score = d;
		this.s = s;
		this.o = o;
		this.p = p;
	}

	@Override
	public int compareTo(GlobalScore o) {
		if (o.score > this.score)
			return -1;
		if (o.score < this.score)
			return 1;
		return 0;
	}
	
	public String toString () {
		StringBuilder sb = new StringBuilder();
		sb.append(score).append(System.lineSeparator());
		if (s != null)
			sb.append(s).append(System.lineSeparator());
		if (o != null)
			sb.append(o).append(System.lineSeparator());
		if (p != null)
			sb.append(p).append(System.lineSeparator());
		return sb.toString();
	}
}
