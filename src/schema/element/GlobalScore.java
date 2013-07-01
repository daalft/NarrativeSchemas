package schema.element;

/**
* Class used to represent a global score that is made up of the scores of the dependency scores
* @author David
*/
public class GlobalScore implements Comparable<GlobalScore>{

	/**
	 * Global score
	 */
	private double score;
	/**
	 * Dependency score
	 */
	private Score s, o, p;
	
	/**
	 * No-argument constructor
	 */
	public GlobalScore() {
		
	}
	
	/**
	 * Constructor with scores
	 * @param d global score
	 * @param s subject score
	 * @param o object score
	 * @param p preposition score
	 */
	public GlobalScore (double d, Score s, Score o, Score p) {
		score = d;
		this.s = s;
		this.o = o;
		this.p = p;
	}

	/**
	 * Returns the global score
	 * @return global score
	 */
	public double getScore() {
		return score;
	}

	/**
	 * Sets the global score 
	 * @param score score
	 */
	public void setScore(double score) {
		this.score = score;
	}

	/**
	 * Returns the subject score
	 * @return subject score
	 */
	public Score getS() {
		return s;
	}

	/**
	 * Sets the subject score
	 * @param s subject score
	 */
	public void setS(Score s) {
		this.s = s;
	}

	/**
	 * Returns the object score
	 * @return object score
	 */
	public Score getO() {
		return o;
	}

	/**
	 * Sets the object score
	 * @param o object score
	 */
	public void setO(Score o) {
		this.o = o;
	}

	/**
	 * Returns the preposition score
	 * @return preposition score
	 */
	public Score getP() {
		return p;
	}
	
	/**
	 * Sets the preposition score
	 * @param p preposition score
	 */
	public void setP(Score p) {
		this.p = p;
	}

	/**
	 * Comparison based on score
	 */
	@Override
	public int compareTo(GlobalScore o) {
		if (o.score > this.score)
			return -1;
		if (o.score < this.score)
			return 1;
		return 0;
	}
	
	/**
	 * toString method
	 * @return string representation
	 */ 
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
