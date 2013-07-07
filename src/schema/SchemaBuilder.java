package schema;

import io.NCWriter;
import io.Reader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import schema.element.GlobalScore;
import schema.element.Schema;
import schema.element.Score;

import chain.element.Chain;
import chain.element.Event;

/**
 * Class to build schemas from input file
 * @author David
 *
 */
public class SchemaBuilder {

	/**
	 * PMI lookup table
	 */
	private Map <String, Double> pmiTable;
	/**
	 * (e,d; e',d';a) frequency lookup table
	 */
	private Map<String, Integer> frequencyTable;
	/**
	 * Frequency threshold value
	 */
	private int frequencyThreshold = 2;
	/**
	 * Pool
	 */
	private List<String> mentionPool, dependencyPool;
	/**
	 * Events
	 */
	private List<Event> events;
	/**
	 * Weighting factor
	 */
	private double lambda = 0.08, beta = 0.2;
	/**
	 * Path to pair buffer file
	 */
	private String path = "";
	/**
	 * Flag to indicate the use of full prepositional information
	 */
	private boolean fullArgument;
	/**
	 * List of merged events
	 */
	private List<Event> list, activeList;
	/**
	 * Schema size
	 */
	private int schemaSize = 6;
	/**
	 * Schemas
	 */
	private List<Schema> schemas;
	/**
	 * File writer
	 */
	private NCWriter ncw;

	/**
	 * No-argument constructor
	 */
	public SchemaBuilder () {
		// initialize components
		pmiTable = new HashMap<String, Double>();
		frequencyTable = new HashMap<String, Integer>();
		mentionPool = new ArrayList<String>();
		dependencyPool = new ArrayList<String>();
		schemas = new ArrayList<Schema>();
		activeList = new ArrayList<Event>();
	}

	/**
	 * Specify path to pair buffer file
	 * @param path path to pair buffer
	 */
	public void setPathToPairBuffer (String path) {
		this.path = path;
	}
	
	/**
	 * Data reading method
	 * @throws IOException 
	 */
	private void readData (boolean writeFrequencyFile) throws IOException {
		System.err.print("Reading data...");
		long now = System.currentTimeMillis();
		// initialize reader
		Reader r = new Reader();
		// read pair buffer file
		r.readPairBuffer(path, fullArgument);
		// retrieve dependencies
		dependencyPool = r.getDependencyPool();
		// retrieve pmi table
		pmiTable = r.getTable();
		// retrieve events
		events = r.getEvents();
		printTimeTaken(now, "ms");
		populateFrequencyTable(writeFrequencyFile);
	}

	/**
	 * Method to populate the frequency lookup table
	 * @param write parameter whether to write frequency file
	 * @param file optional frequency file
	 * @throws IOException 
	 */
	private void populateFrequencyTable (boolean write, String...file) throws IOException {
		long startLog = System.currentTimeMillis();
		System.err.print("Start populating frequency table...");
		// filename
		String fname = "";
		// check for file with name "frequencyFile" in this directory
		if (new File("./frequencyFile").exists()) {
			// if file exists, set filename to this name
			fname = "./frequencyFile";
			// else if a filename is given, use that name
		} else if (file.length > 0 && (new File(file[0]).exists())) {
			fname = file[0];
		}
		// if filename is not empty
		if (!fname.equals("")) {
			// initialize reader
			Reader r = new Reader();
			// read file
			String freq = r.readFile(new File(fname));
			// retrieve information
			for (String s : freq.split(System.lineSeparator())) {
				String[] sp = s.split("\t");
				// get frequency count
				int intfreq = Integer.parseInt(sp[1]);
				// populate table
				frequencyTable.put(sp[0], intfreq);
			}
			printTimeTaken(startLog, "ms");
			// end method
			return;
		}
		// if no frequency file found, initialize writer
		if (write)
			ncw = new NCWriter();
		// merge events
		list = mergeEvents();
		// remove verbs that don't have subject and object
		purifyList();
		// outer loop: for all verbs
		for (int i = 0; i < list.size()-1; i++) {
			// get the i-th verb
			Event e1 = list.get(i);
			// inner loop: for all verbs
			for (int j = i+1; j < list.size(); j++) {
				// get the j-th verb
				Event e2 = list.get(j);
				// if both verbs are the same, continue
				if (e1.equals(e2))
					continue;
				// for all mentions of e2
				for (int k = 0; k < e2.getMentions().size(); k++) {
					// get k-th mention
					String a = e2.getMention(k);
					/*
					 * Short circuit 1:
					 * If e1 does not contain the mention of e2,
					 * there will be no frequency for (e1,e2,a).
					 */
					if (!e1.getMentions().contains(a))
						continue;
					// assemble key and reverse key
					String key = e1.getTypedDep() + e2.getTypedDep() + a;
					String revKey = e2.getTypedDep() + e1.getTypedDep() + a;
					/*
					 * Short circuit 2:
					 * If the frequency (e1,e2,a) has been calculated,
					 * do not calculate the frequency (e2,e1,a), because it's the same
					 */
					if (frequencyTable.containsKey(key) || frequencyTable.containsKey(revKey))
						continue;
					// calculate the frequency of occurrence
					int f = (int) freq(e1, e2, a);
					// if frequency is above the frequency threshold
					if (f >= frequencyThreshold) {
						//System.out.println("Calculating " + key + ": " + f);
						// put key->value into table
						frequencyTable.put(key, f);
						// if write flag
						if (write)
							// write file
							ncw.write(key + "\t" + f + System.lineSeparator(), file.length>0?file[0]:"./frequencyFile");
					}
				}
			}
		}
		printTimeTaken(startLog, "s");
	}

	/**
	 * Sets the weighting factor lambda
	 * <p>
	 * Default value: 0.08
	 * @param lambda weighting factor
	 */
	public void setLambda (double lambda) {
		this.lambda = lambda;
	}

	/**
	 * Sets the weighting factor beta
	 * <p>
	 * Default value: 0.2
	 * @param beta weighting factor
	 */
	public void setBeta (double beta) {
		this.beta = beta;
	}

	/**
	 * Sets the desired schema size
	 * <p>
	 * Algorithm will add verbs to a narrative schema until
	 * the number of verbs provided by <em>schema size</em> is reached
	 * @param size schema size
	 */
	public void setSchemaSize (int size) {
		schemaSize = size;
	}

	/**
	 * Calculates similarity between two events with respect to a given argument a
	 * @param e1 event 1
	 * @param e2 event 2
	 * @param a argument
	 * @return similarity value
	 */
	private double sim (Event e1, Event e2, String a) {		
		// concatenate lookup string
		String lookup = e1.getTypedDep() + e2.getTypedDep();
		// get pmi value
		double pmi = pmiLookup(lookup);
		// return similarity
		int f = frequencyLookup(e1, e2, a);
		//(int) freq(e1,e2,a); //frequencyLookup(e1, e2, a);
		double simval = 0.0;
		if (f > 0)
			simval = pmi + (lambda * Math.log(f));
		//System.out.println("Similarity of " + a + " with " + e1.getTypedDep() + " and " + e2.getTypedDep() + ": " + simval);
		return simval;
	}

	/**
	 * Utility method to un-clutter code
	 * <p>
	 * Looks up the frequency value for two events and an argument in the frequency table
	 * @param e1 event 1
	 * @param e2 event 2
	 * @param a argument
	 * @return frequency
	 */
	private int frequencyLookup (Event e1, Event e2, String a) {
		// assemble key
		String key = e1.getTypedDep() + e2.getTypedDep() + a;
		// assemble reverse key
		String revKey = e2.getTypedDep() + e1.getTypedDep() + a;
		// if table contains key
		if (frequencyTable.containsKey(key))
			// return value
			return frequencyTable.get(key);
		if (frequencyTable.containsKey(revKey))
			return frequencyTable.get(revKey);
		// else return 0
		return 0;
	}

	/**
	 * Utility method to not clutter code
	 * <p>
	 * Returns the PMI value if the key is in the table, 
	 * 0.0 otherwise
	 * @param lookup key to look up
	 * @return PMI if present
	 */
	private double pmiLookup (String lookup) {
		// if table contains lookup key
		if (pmiTable.containsKey(lookup)) {
			// return value
			return pmiTable.get(lookup);
		}
		// return 0.0 else
		return 0.0;
	}

	/**
	 * Helper method to calculate the occurrence of an argument <em>a</em> given two events
	 * <p>
	 * Returns how often the argument <em>a</em> filled the argument positions of the events 1 and 2
	 * @param e1 event 1
	 * @param e2 event 2
	 * @param a argument
	 * @return frequency count
	 */
	private double freq (Event e1, Event e2, String a) {
		// frequency counter
		int count = 0;
		// because of j = 1, j can cause IndexOutOfBoundsException if events contains less than two events
		try {
			// loop over all events (outer loop)
			for (int i = 0; i < events.size(); i++) {
				// loop over all events (inner loop)
				for (int j = 1; j < events.size(); j++) {
					// don't count visited elements more than once
					if (j < i)
						continue;
					// if we can find the events e1 and e2
					if (events.get(i).equals(e1) && events.get(j).equals(e2)) {
						// if the events have 'a' as argument
						if (a.equals(events.get(i).getMention()) && a.equals(events.get(j).getMention()))
							// increase frequency counter
							count++;
					}
				}
			}
		} catch (IndexOutOfBoundsException e) { // catch eventual exceptions
			// empty block
		}
		// return frequency count
		return count;
	}

	/**
	 * Scores an argument against a chain
	 * <p>
	 * Returns the similarity between all events in the chain and the argument a
	 * @param c chain
	 * @param a argument
	 * @return similarity value
	 */
	private double score (Chain c, String a) {
		// set n for convenience
		// -1 because list is zero-based
		int n = c.getEvents().size()-1;
		// internal sum counter
		double sum = 0.0;
		// because of j = i + 1, j can throw IndexOutOfBoundsException
		try {
			// for i from 0 to n-1 (can't go to n, because of pairwise comparison)
			for (int i = 0; i < n-1; i++) {
				// for j from i+1 to n (j always starts one to the right from i)
				for (int j = i+1; j < n; j++) {
					// increase sum by similarity of events
					sum += sim(c.get(i), c.get(j), a);
				}
			}
		} catch (IndexOutOfBoundsException e) {
			// empty block
		}
		// return sum
		return sum;
	}

	/**
	 * Returns the maximum value of similarity of a chain c given
	 * a verb f and its dependency g
	 * <p>
	 * All arguments in c are scored against the event (f-g) and all arguments in c.
	 * The maximum value of these calculations gives an indication how well (f-g) 
	 * fits into the existing chain c
	 * @param c chain
	 * @param f verb
	 * @param g dependency
	 * @return similarity value
	 */
	private double chainsim (Chain c, String f, String g) {
		// intern score list to find maximum
		List<Double> intern_scores = new ArrayList<Double>();
		// for all events in chain c
		for (Event e : c.getEvents()) {
			for (String m : e.getMentions())
				// add their mentions to the mention pool
				mentionPool.add(m);
		}
		// create new dummy event based on f and g
		Event fg = new Event(f+"-"+g, "", "");
		// for every mention in the pool
		for (String m : mentionPool) {
			// internal sum 
			double intern_sum = 0.0;
			// score c in respect to mention
			double score = score(c,	m);
			// for all chains in c
			for (int i = 0; i < c.getEvents().size(); i++) {
				// increase internal sum by similarity 
				intern_sum += sim(c.get(i), fg, m);
			}
			// add score to internal sum, and add the combined sum to intern score list
			intern_scores.add(score + intern_sum);
		}
		// clear the mention pool
		mentionPool.clear();
		// find and return the maximum value
		double max = Collections.max(intern_scores);
		return max;
	}

	/**
	 * Calculates the narrative similarity of a verb <em>v</em> given a schema <em>n</em>
	 * <p>
	 * Based on the threshold value of <em>beta</em>, <em>v</em> is added to a chain in <em>n</em> or a new
	 * chain is created for <em>v</em>
	 * @param n narrative schema
	 * @param v verb
	 * @return similarity value
	 * @deprecated
	 */
	@SuppressWarnings("unused")
	private double narsim (Schema n, String v) {
		// intern score list to find maximum
		List<Double> intern_scores = new ArrayList<Double>();
		// for all dependencies
		for (String d : dependencyPool) {
			// for all chains in n
			for (Chain c : n.getChains()) {
				// add chainsim score to intern scores
				intern_scores.add(Math.max(beta, chainsim(c, v, d)));
			}
		}
		// select maximum
		double max = Collections.max(intern_scores);
		// return maximum
		return max;
	}

	/**
	 * Method to build schemas starting with verb <em>verb</em>
	 * @param verb starting verb
	 * @return schema
	 */
	private Schema buildSchema (String verb) {
		// safety counter
		int loopCounter = 0;
		// maximum iterations
		int maxLoops = schemaSize+5;
		// create new Schema
		Schema n = new Schema();
		// get all events for verb
		List<Event> events = getEvents(verb);
		// add all events to schema
		for (int i = 0; i < events.size(); i++) {
			Event e = events.get(i);
			e.setMaxScore(beta);
			// create new chain for each event
			n.add(e, i);
		}
		// delete original verb
		deleteVerb(verb);
		// begin loop
		loop:
			// while schema contains less than schemaSize verbs
			while (n.getVerbCount() < schemaSize) {
				// initialize local score lists
				List<GlobalScore> gs = new ArrayList<GlobalScore>();
				List<Score> scoreS = new ArrayList<Score>();
				List<Score> scoreO = new ArrayList<Score>();
				List<Score> scoreP = new ArrayList<Score>();
				// initialize local scores
				Score maxS = null;
				Score maxO = null;
				Score maxP = null;
				// for all events
				for (int i = 0; i < activeList.size(); i++) {
					// get i-th event
					Event e = activeList.get(i);
					// get verb of event
					String v = e.getVerb();
					// get all events with verb
					List<Event> ev = getEvents(v);
					// get chains
					List<Chain> chains = n.getChains();
					// clear score lists
					scoreS.clear();
					scoreO.clear();
					scoreP.clear();
					// for each event
					for (Event event : ev) {
						// for each chain
						for (int j = 0; j < chains.size(); j++) {
							// get dependency
							String dep = event.getDependency(false);
							// calculate score
							// and remember event position and chain position
							Score eventScore = scorecalc(activeList.indexOf(event), j, chainsim(chains.get(j), event.getVerb(), dep));
							// add score to relevant list
							if (dep.equals("s"))
								scoreS.add(eventScore);
							else if (dep.equals("o"))
								scoreO.add(eventScore);
							else
								scoreP.add(eventScore);
						}
					}
					// find maximum values
					maxS = scoreS.isEmpty()?new Score():Collections.max(scoreS);
					maxO = scoreO.isEmpty()?new Score():Collections.max(scoreO);
					maxP = scoreP.isEmpty()?new Score():Collections.max(scoreP);
					// calculate global maximum value
					double globalMax = (maxS==null?beta:maxS.getScore()) + (maxO==null?beta:maxO.getScore()) + (maxP==null?beta:maxP.getScore());
					// if global maximum value higher than 3 times beta
					if (globalMax > 3*beta)
						// add to global scores
						gs.add(new GlobalScore(globalMax, maxS, maxO, maxP));
				}
				// if no maximum has been found, break
				if (gs.isEmpty())
					break loop;
				// retrieve maximum scores
				GlobalScore best = Collections.max(gs);
				Score bestS = best.getS();
				Score bestO = best.getO();
				Score bestP = best.getP();
				// initialize verb to delete
				String vtd = "";
				// if maximum subject score exists
				if (bestS != null) {
					// get event at remembered position
					Event es = activeList.get(bestS.getEventPosition());
					es.setMaxScore(best.getScore());
					// only continue if subject and object do not point to the same chain
					if (bestS.getChainPosition() != bestO.getChainPosition()) {
						// if event not already in schema
						if (!n.contains(es)) {
							// if the score is less or equal than beta
							// create new chain
							if (bestS.getScore() <= beta) {
								n.add(es, n.getChainCount()+1);
								bestS.setChainPosition(bestS.getChainPosition()+1);
							} else { // else add at specified chain position
								n.add(es, bestS.getChainPosition());
							}
							n.getChains().get(bestS.getChainPosition()).setScore(bestS.getScore());
						}
						// set verb to delete
						vtd = es.getVerb();
					}
				}
				// same as above with object instead of subject
				if (bestO != null) {
					Event eo = activeList.get(bestO.getEventPosition());
					eo.setMaxScore(best.getScore());
					if (bestO.getChainPosition() != bestS.getChainPosition()) {

						if (!n.contains(eo)) {
							if (bestO.getScore() <= beta) {
								n.add(eo, n.getChainCount()+1);
								bestO.setChainPosition(bestO.getChainPosition()+1);
							} else {
								n.add(eo, bestO.getChainPosition());
							}
							n.getChains().get(bestO.getChainPosition()).setScore(bestO.getScore());
						}
						if (vtd.equals(""))
							vtd = eo.getVerb();
					}
				}
				// same as above with preposition
				if (bestP != null) {
					Event ep = activeList.get(bestP.getEventPosition());
					ep.setMaxScore(best.getScore());
					if (!n.contains(ep)) {
						if (bestP.getScore() <= beta) {
							n.add(ep, n.getChainCount()+1);
							bestP.setChainPosition(bestP.getChainPosition()+1);
						} else {
							n.add(ep, bestP.getChainPosition());
						}
						n.getChains().get(bestP.getChainPosition()).setScore(bestP.getScore());
					}
					if (vtd.equals(""))
						vtd = ep.getVerb();
				}
				// delete verb
				deleteVerb(vtd);
				// clear global score list
				gs.clear();
				// safety measure to avoid endless loop
				if (loopCounter++ > maxLoops)
					break loop;
			}
		// return schema
		return n;
	}
	
	/**
	 * Private score calculation
	 * <p>
	 * Returns a Score object with the given score, event position and chain position
	 * @param eventpos event position
	 * @param chainpos chain position
	 * @param score score
	 * @return Score object
	 */
	private Score scorecalc (int eventpos, int chainpos, double score) {
		return new Score(eventpos, chainpos, Math.max(beta, score));
	}

	/**
	 * Helper method to delete all verbs that do not have 
	 * a subject and an object argument
	 */
	private void purifyList () {
		for (int i = 0; i < list.size(); i++) {
			if (!(hasComplement(list.get(i), "s") &&hasComplement(list.get(i), "o"))) {
				list.remove(i--);				
			}
		}
	}

	/**
	 * Method to run SchemaBuilder
	 * <p>
	 * If <em>shuffle</em> is set to <b>true</b>, the list of events will be shuffled
	 * before schema building.<br/>
	 * If <em>sort</em> is set to <b>true</b>, the list of events will be sorted
	 * before schema building.<br/>
	 * If both values are set to <b>true</b>, <em>sort</em> will take precedence over
	 * <em>shuffle</em>.<br/>
	 * Filename is the name of the output file
	 * @param shuffle shuffle events
	 * @param sort sort events 
	 * @param filename output filename
	 * @throws IOException
	 */
	public void run (boolean shuffle, boolean sort, String filename, boolean writeFreqFile) throws IOException {
		// read data
		readData(writeFreqFile);
		// merge events
		list = mergeEvents();
		// remove events that don't have subject and object position
		purifyList();
		// populate list of active verbs
		activeList.addAll(list);
		if (shuffle)
			Collections.shuffle(activeList);
		if (sort)
			Collections.sort(activeList);
		long start = System.currentTimeMillis();
		System.err.print("Building schemas...");
		// for all active verbs
		for (int i = 0; i < activeList.size(); i++) {
			// build schema
			Schema s = buildSchema(activeList.get(i).getVerb());
			// add only if required number of verbs reached
			// and if at least one chain with more than one event exists
			if (s.getVerbCount() >= schemaSize && s.getLongestChainCount() > 1)
				schemas.add(s);
		}
		printTimeTaken(start, "s");
		// sort schemas by global score
		Collections.sort(schemas);
		Collections.reverse(schemas);
		System.err.print("Writing schemas...");
		for (Schema s : schemas) {
			// make sure there is an initialized writer
			if (ncw == null)
				ncw = new NCWriter();
			// write to file
			ncw.write(s.toString(), filename);
		}
		long end = System.currentTimeMillis();
		System.err.println("Finished. Time taken: " + (end - start)/1000 + " s");
	}

	/**
	 * Utility method to print time taken in a given format
	 * <p>
	 * Format is specified as a string and can be<br/><em>s</em>
	 * for seconds<br/><em>m</em> for minutes<br/><em>ms</em> for 
	 * milliseconds
	 * @param startTime start time
	 * @param format format
	 */
	private void printTimeTaken (long startTime, String format) {
		long now = System.currentTimeMillis();
		long sec = (now - startTime)/1000;
		long min = sec / 60;
		if (format.equals("s"))
			System.err.println("[" + sec + " s]");
		else if (format.equals("m"))
			System.err.println("[" + min + " m]");
		else
			System.err.println("[" + (now - startTime) + " ms]");
	}

	/**
	 * Method to merge all verbs that have the same lemma and dependency
	 * by merging all arguments of all verbs with same lemma and dependency
	 * @return list of merged events as <i>List&lt;Event&gt;</i>
	 */
	private List<Event> mergeEvents () {
		// create new list
		List<Event> merge = new ArrayList<Event>();
		// for all events
		for (int i = 0; i < events.size(); i++) {
			// retrieve i-th event
			Event e = events.get(i);
			// create dummy events
			Event emc = new Event();
			// set typed dep
			emc.setTypedDep(e.getTypedDep());
			// add mention
			emc.addMention(e.getMention());
			// if dummy event already in list
			if (merge.contains(emc)) {	
				// add mention to event in list
				merge.get(merge.indexOf(emc)).addMention(e.getMention());
				// increment score by one
				merge.get(merge.indexOf(emc)).incrementScore(1);
			} else { // dummy event not in list
				// create new dummy event
				Event em = new Event();
				// set typed dep
				em.setTypedDep(e.getTypedDep());
				// add mention
				em.addMention(e.getMention());
				// add to list
				merge.add(em);
			}
		}
		// return list
		return merge;
	}

	/**
	 * Checks whether there is a complement for this verb in the list of verbs
	 * <p>
	 * If the optional parameter <em>dep</em> is specified, checks whether the list
	 * contains an event with the verb given in <em>e</em> and the specified dependency <em>dep</em><br/><br/>
	 * Otherwise, the complement is an event with the same verb and another dependency as follows:<br/>
	 * <table>
	 * <tr><th>Dependency in <em>e</em></th><th>Complement</th></tr>
	 * <tr><td>s (subject)</td><td>o (object)</td></tr>
	 * <tr><td>o (object)</td><td>s (subject)</td></tr>
	 * <tr><td>p (preposition)</td><td>s or o (subject or object)</td></tr>
	 * </table>
	 * @param e Event to check
	 * @param dep optional dependency to check
	 * @return whether there is a complement or not
	 */
	private boolean hasComplement (Event e, String...dep) {
		// get the dependency
		// for prepositions, only get "p" instead of full information (f.i. p_of)
		String d = e.getDependency(false);
		// initialize complement string
		// if dep is specified, use given dep
		String comp = dep.length>0?dep[0]:"";
		// create dummy event 
		Event dummy = new Event();
		// if dep is not specified
		if (dep.length == 0) {
			// depending on the dependency of e, select the corresponding complement
			switch (d) {
			// for s, check for o
			case "s": comp = "o";
			// for o, check for s
			case "o": comp = "s";
			// for p, check for either s or o
			case "p": 
				// first check s
				dummy.setTypedDep(e.getVerb()+"-"+"s");
				// save value for contains
				boolean b = list.contains(dummy);
				// change dependency
				dummy.setTypedDep(e.getVerb()+"-"+"o");
				// concatenate value of contains with previous value using logical OR
				b = b | list.contains(dummy);
				// return value
				return b;
				// default case: break;
			default: break;
			}
		}
		// if method reaches this point, comp should be set and d was not p
		// set complement
		dummy.setTypedDep(e.getVerb()+"-"+comp);
		// return contains value
		return list.contains(dummy);
	}

	/**
	 * Method to retrieve complement of event
	 * <p>
	 * Method {@link #hasComplement(Event)} should be invoked prior to invoking
	 * this method to make sure there is a complement<br/><br/>
	 * If there is no complement, method returns null
	 * @param e event
	 * @param dep optional dependency
	 * @return complement event or <b>null</b> if there is no complement
	 * @see #hasComplement(Event)
	 * @deprecated
	 */
	@SuppressWarnings("unused")
	private Event getComplement (Event e, String...dep) {
		// get the dependency
		// for prepositions, only get "p" instead of full information (f.i. p_of)
		String d = e.getDependency(false);
		// initialize complement string to given dep if specified, else empty string
		String comp = dep.length > 0 ? dep[0] : "";
		// create dummy event 
		Event dummy = new Event();
		// if no dep specified
		if (dep.length == 0) {
			// depending on the dependency of e, select the corresponding complement
			switch (d) {
			// for s, check for o
			case "s": comp = "o";
			// for o, check for s
			case "o": comp = "s";
			// for p, check for either s or o
			case "p": 
				// first check s
				dummy.setTypedDep(e.getVerb()+"-"+"s");
				// if complement-s in list, return
				if(list.contains(dummy))
					return list.get(list.indexOf(dummy));
				// change dependency
				dummy.setTypedDep(e.getVerb()+"-"+"o");
				// if complement-o in list, return
				if (list.contains(dummy))
					return list.get(list.indexOf(dummy));
				// if neither in list, return null
				return null;
				// default case: break;
			default: break;
			}
		}
		// if method reaches this point, comp should be set and d was not p
		// set complement
		dummy.setTypedDep(e.getVerb()+"-"+comp);
		// return event if in list
		if (list.contains(dummy))
			return list.get(list.indexOf(dummy));
		// else return null
		return null;
	}

	/**
	 * Method to retrieve all events for a given verb
	 * <p>
	 * For a given verb <em>verb</em>, returns all events
	 * that contain <em>verb</em> as verb
	 * @param verb verb
	 * @return list of events
	 */
	private List<Event> getEvents (String verb) {
		List<Event> events = new ArrayList<Event>();
		for (int i = 0; i < activeList.size(); i++) {
			if (activeList.get(i).getVerb().equals(verb))
				if (!events.contains(activeList.get(i)))
					events.add(activeList.get(i));
		}
		return events;
	}

	/**
	 * Method to delete all events that contain a given verb 
	 * from the list of currently active verbs
	 * @param verb verb
	 */
	private void deleteVerb (String verb) {
		for (int i = 0; i < activeList.size(); i++) {
			if (activeList.get(i).getVerb().equals(verb))
				activeList.remove(i--);
		}
	}

	/**
	 * Sets the flag to use full argument for prepositional dependencies
	 * <p>
	 * By default, prepositional dependencies will only be represented by <em>p</em><br/>
	 * instead of the full dependency information (e.g. <em>p_with</em>). If this flag is set to true,<br/>
	 * the full information will be used
	 * @param b flag
	 */
	public void setUseFullArgument (boolean b) {
		fullArgument = b;
	}

	/**
	 * Main method
	 * @param args arguments
	 */
	public static void main(String[] args) {
		try {
			new SchemaBuilder().run(true, false, "schemas_size6_v1", true);
		} catch (IOException e) {

			e.printStackTrace();
		}
	}
}
