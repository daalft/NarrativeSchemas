package schema.element;
/**
 * Class used to memorize the score of an event with pointers to the chain and event for later retrieval
 * @author David
 */ 
public class Score implements Comparable<Score>{
	/**
	 * Pointer
	 */
	private int event, chain;
	/**
	 * Score
	 */
	private double score;
	
	/**
	 * No-argument constructor
	 */
	public Score () {
		
	}
	
	/**
	 * Constructor with arguments
	 * @param e pointer to event
	 * @param c pointer to chain
	 * @param s score
	 */
	public Score (int e, int c, double s) {
		event = e;
		chain = c;
		score = s;
	}
	
	/**
	 * Sets the pointer to the event
	 * @param pos position
	 */
	public void setEventPosition (int pos) {
		event = pos;
	}
	
	/**
	 * Sets the pointer to the chain
	 * @param pos position
	 */
	public void setChainPosition (int pos) {
		chain = pos;
	}
	
	/**
	 * Sets the score
	 * @param d score
	 */
	public void setScore (double d) {
		score = d;
	}
	
	/**
	 * Returns the event position
	 * @return event position
	 */
	public int getEventPosition () {
		return event;
	}
	
	/**
	 * Returns the chain position
	 * @return chain position
	 */
	public int getChainPosition () { 
		return chain;
	}
	
	/**
	 * Returns the score
	 * @return score
	 */
	public double getScore () {
		return score;
	}
	
	/**
	 * Comparison based on score
	 */
	@Override
	public int compareTo(Score o) {
		if (o.score < this.score)
			return 1;
		if (o.score > this.score)
			return -1;
		return 0;
	}
	
	/**
	 * Standard toString method
	 * @return string representation
	 */
	public String toString () {
		return score + ";" + event + ":" + chain;
	}
}
