package schema.element;

public class Score implements Comparable<Score>{

	private int event, chain;
	private double score;
	
	public Score () {
		
	}
	
	public Score (int e, int c, double s) {
		event = e;
		chain = c;
		score = s;
	}
	
	public void setEventPosition (int pos) {
		event = pos;
	}
	
	public void setChainPosition (int pos) {
		chain = pos;
	}
	
	public void setScore (double d) {
		score = d;
	}
	
	public int getEventPosition () {
		return event;
	}
	
	public int getChainPosition () { 
		return chain;
	}
	
	public double getScore () {
		return score;
	}

	@Override
	public int compareTo(Score o) {
		if (o.score < this.score)
			return 1;
		if (o.score > this.score)
			return -1;
		return 0;
	}
	
	public String toString () {
		return score + ";" + event + ":" + chain;
	}
}
