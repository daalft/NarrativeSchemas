package chain;

import io.NCWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import chain.element.Entry;
import chain.element.Event;
import chain.element.EventBlock;
import chain.element.Pair;

/**
 * Class used to build pairs and calculate pairwise mutual information
 * @author David
 *
 */
public class PairBuilder {

	/**
	 * Path to buffer file
	 */
	private static String path;
	/**
	 * Map with event counts 
	 */
	private Map<String, Integer> globalMap;
	/**
	 * List of entries
	 */
	private List<Entry> entries;
	/**
	 * Absolute count
	 */
	private static long absoluteCount;
	/**
	 * Output writer
	 */
	private NCWriter ncw;
	/** 
	 * Unique id buffer
	 */
	private Set<String> idUniq;
	/**
	 * Unique pair buffer
	 */
	private List<List<Pair>> unique;
	private String pairfile;

	/**
	 * No-argument constructor
	 * @throws IOException
	 */
	public PairBuilder () throws IOException {
		entries = new ArrayList<Entry>();
		globalMap = new HashMap<String, Integer>();
		idUniq = new HashSet<String>();
		unique = new LinkedList<List<Pair>>();
		
	}

	/**
	 * Method for setting the path to buffer file
	 * @param p path
	 */
	public void setPath (String p) {
		path = p;
	}

	/**
	 * Method for setting the path to pair buffer file
	 * @param p path
	 */
	public void setPairPath (String p) {
		pairfile = p;
	}
	
	/**
	 * Method for retrieving path to pair buffer
	 * @return
	 */
	public String getPairPath () {
		return pairfile;
	}
	/**
	 * Method for reading data
	 * @throws IOException
	 */
	private void readData () throws IOException {
		System.err.print("Reading data...");
		// log start time
		long startReading = System.currentTimeMillis();
		// create reader
		BufferedReader br = new BufferedReader(new FileReader(new File(path)));
		// create buffer
		String l = "";
		// read file content
		while ((l=br.readLine())!=null) {
			// split off id
			String[] sp = l.split(";");
			String id = sp[0];
			// create entry
			Entry e = new Entry(id);
			// split off event blocks
			String[] blocks = sp[1].split("&!&");
			for (int i = 0; i < blocks.length; i++) {
				EventBlock eb = new EventBlock();
				// split off events
				String[] events = blocks[i].split(":");
				for (int j = 0; j < events.length; j++) {
					
					eb.add(new Event(events[j]));
					// split off dependency
					String _str = events[j].split(" ")[0];
					// correct length to remove dangling parenthesis
					int _i = _str.length()-1;
					_str = _str.substring(0, _i);
					// populate global map
					globalMap.put(_str, increaseCount(_str));
					// increase absolute counter
					absoluteCount++;
				}
				// add created event block to entry
				e.addEvent(eb);
			}
			// add created entry to entries
			entries.add(e);
		}
		// close reader
		br.close();
		// log end time
		long endReading = System.currentTimeMillis();
		long secondsReading = endReading - startReading;
		System.err.println("[" + secondsReading + " ms]");	
	}

	/**
	 * Helper method to increase map value
	 * <p>
	 * If key <b>key</b> is in map, returns value for <b>key</b> plus one.
	 * <br>Otherwise returns 1
	 * @param key key in map
	 * @return value of key plus one
	 */
	private int increaseCount (String key) {
		if (globalMap.containsKey(key))
			return globalMap.get(key)+1;
		return 1;
	}

	/**
	 * Helper method to retrieve map value
	 * <p>
	 * If key <b>key</b> is in map, returns value for <b>key</b>.
	 * <br>Otherwise returns 0
	 * @param key key
	 * @return value of key
	 */
	private int getCount (String key) {
		if (globalMap.containsKey(key))
			return globalMap.get(key);
		return 0;
	}

	public void generatePairs () throws IOException {
		readData();
		System.err.println("Generating pairs...");
		for (Entry e : entries) {
			// generate id entry
			List <Pair> local = new ArrayList<Pair>();
			local.add(new Pair(e.getId()));
			unique.add(local);
			// generate pairs
			boolean generationSuccess = treatBlock(e.getEventBlock());
			if (!generationSuccess) {
				unique.remove(local);
			}
		}
		// for each set
		System.err.println("Writing output...");
		for (List<Pair> sp : unique) {
			// for each pair
			for (Pair p : sp)
				writeOutput(p);
				//System.out.println(p);
		}
		System.err.println("Done generating pairs.");
	}

	/**
	 * Helper method to write output
	 * @param p pair to write
	 */
	private void writeOutput (Pair p) {
		// initialize writer
		ncw = new NCWriter();
		// buffer
		String p4 = "";
		// if pair is header
		if (p.isHeader()) {
			// add space
			try {
				ncw.write("\n", pairfile);
			} catch (IOException e) {
				// nothing
			}
			// use id
			p4 = p.getH();
		} else { // else write pair information
			String p1 = p.getE1().getTypedDep() + "\t" + p.getE2().getTypedDep() + "\t" + p.getPmi();
			String p2 = " (" + p.getE1().getMention() + ") [" + p.getE1().getId() + "] ";
			String p3 = " (" + p.getE2().getMention() + ") [" + p.getE2().getId() + "] ";
			p4 = p1 + " |" + p2 + ":" + p3 + "|";
		}
		try {
			ncw.write(p4, pairfile);
			ncw.write("\n", pairfile);
		} catch (IOException e) {
			// nothing
		}
	}

	/**
	 * Helper method to treat lists of event blocks
	 * @param le list of event blocks
	 * @return false if no events were generated. true otherwise
	 */
	private boolean treatBlock (List<EventBlock> le) {
		boolean check = false;
		for (EventBlock eb : le) {
			List<Pair> ret = treatEvent (eb);
			if (!ret.isEmpty()) {
				check = true;
				if (!unique.contains(ret)) {
					unique.add(ret);
				}	
			}
		}
		if (check) 
			return true;
		return false;
	}

	/**
	 * Helper method to treat event blocks
	 * @param eb event block
	 * @return 
	 */
	private List<Pair> treatEvent (EventBlock eb) {
		List <Pair> local_unique = new ArrayList<Pair>();
		// change to array
		Object[] e = eb.getEvents().toArray();
		// double loop
		for (int i = 0; i < e.length; i++) {
			for (int j = 1; j < e.length; j++) {
				if (i == j)
					continue;
				// get events
				Event e1 = (Event) e[i];
				Event e2 = (Event) e[j];
				// generate keys
				String key1 = e1.getId() + e2.getId();
				String key2 = e2.getId() + e1.getId();
				// check if keys are stored
				if (!(idUniq.contains(key1)||idUniq.contains(key2))) {
					// if e1 and e2 have corefering mentions
					if (e1.getId().equals(e2.getId())) {
						// ignore same dependency
						if (e1.getTypedDep().equals(e2.getTypedDep()))
							continue;
						// add pair to pair set
						local_unique.add(new Pair(e1,e2,pmi(e1,e2)));
					}
					// else do nothing
				}
				// add keys
				idUniq.add(key1);
				idUniq.add(key2);
			}
		}
		// clear unique buffer
		idUniq.clear();
		return local_unique;
	}

	/**
	 * Calculates the pmi for two given events
	 * @param e1 event 1
	 * @param e2 event 2
	 * @return pmi
	 */
	private double pmi (Event e1, Event e2) {
		// probability for event 1
		double c1 = ((double)getCount(e1.getTypedDep()))/absoluteCount;
		// probability for event 2
		double c2 = ((double)getCount(e2.getTypedDep()))/absoluteCount;
		// numerator
		double num = pwdvg(e1, e2);
		// pmi
		return Math.log(num/(c1 * c2));
	}

	/**
	 * Calculates the numerator of the pmi equation
	 * @param e1 first event
	 * @param e2 second event
	 * @return numerator for pmi
	 */
	private double pwdvg (Event e1, Event e2) {
		// counter vars
		int num = 0;
		int denom = 0;
		// for each entry
		for (Entry e : entries) {
			// for each event block
			for (EventBlock eb : e.getEventBlock()) {
				// events
				List<Event> events = eb.getEvents();
				// double loop
				for (int i = 0; i < events.size(); i++) {
					for (int j = 0; j < events.size(); j++) {
						// skip if i == j
						if (i == j)
							continue;
						// get events
						Event i_e1 = events.get(i);
						Event i_e2 = events.get(j);
						// if inner event 1 coreferent with inner event 2
						if (i_e1.getMention().equals(i_e2.getMention())) {
							// counter up
							denom++;
						}
						// if given e1 coreferent with given e2
						if (e1.equals(i_e1)&&e2.equals(i_e2)) {
							if (i_e1.getMention().equals(i_e2.getMention())) {
								// counter up
								num++;
							}
						}
					}
				}
			} 
		}
		// return
		return ((double)num)/denom;
	}

	public static void main(String[] args) {
		try {
			new PairBuilder();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
