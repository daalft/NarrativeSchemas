package chain;

import io.NCWriter;
import io.Reader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import preprocess.Preprocessor;

import chain.element.Entry;
import chain.element.TypedDep;
import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.semgraph.SemanticGraphEdge;

public class ChainBuilder {
	/**
	 * Lock
	 */
	Lock lock;
	/**
	 * Preprocessor
	 */
	Preprocessor pp;
	/**
	 * Typed dependency builder
	 */
	private TypedDepBuilder tdl;
	/**
	 * Coreference graph
	 */
	private Map<Integer, CorefChain> graph;
	/**
	 * Dependency parsed sentences
	 */
	private List<SemanticGraph> sentences;
	/**
	 * Grammatical relations used to extract roles
	 */
	private GrammaticalRelation[] relations = {  	
			EnglishGrammaticalRelations.NOMINAL_PASSIVE_SUBJECT, // object in passives
			EnglishGrammaticalRelations.DIRECT_OBJECT, // object
			EnglishGrammaticalRelations.INDIRECT_OBJECT, // object
			EnglishGrammaticalRelations.OBJECT, // object
			EnglishGrammaticalRelations.NOMINAL_SUBJECT, // subject
			EnglishGrammaticalRelations.SUBJECT, // subject
			EnglishGrammaticalRelations.AGENT, // subject in passives
	}; 
	/**
	 * Types corresponding to the grammatical relations in <em>relations</em>
	 */
	private String[] types = { 
			"o",
			"o",
			"o",
			"o",
			"s",
			"s",
			"s"
	}; 
	/**
	 * Entities
	 */
	private Set<IndexedWord> entities;
	/**
	 * Output Writer
	 */
	private NCWriter ncw;
	
	/** 
	 * No-argument constructor
	 */
	public ChainBuilder () {
		// check lengths of relations and types
		if (types.length != relations.length) {
			System.err.println("Error!\nThe arrays 'relations' and 'types' are not of equal size!\nAborting...");
			System.exit(1);
		}
		// initialize components
		ncw = new NCWriter();
		entities = new HashSet<IndexedWord>();
		tdl = new TypedDepBuilder();
		sentences = new ArrayList<SemanticGraph>();
		graph = new HashMap<Integer, CorefChain>();
		pp = new Preprocessor();
		new ArrayList<Entry>();
	}

	/**
	 * Method for reinitializing main components
	 */
	private void reinitialize () {
		entities = new HashSet<IndexedWord>();
		tdl = new TypedDepBuilder();
		sentences = new ArrayList<SemanticGraph>();
		graph = new HashMap<Integer, CorefChain>();
		pp = new Preprocessor();
	}

	/**
	 * Retains only chains with more than one mention
	 * @param map coreference graph
	 * @return coreference graph
	 */
	private Map<Integer, CorefChain> cleanCorefGraph (Map<Integer, CorefChain> map) {
		// local map
		Map<Integer, CorefChain> m = new HashMap<Integer, CorefChain>();
		// for each chain
		for (int i : map.keySet()) {
			// if it contains more than one mention
			if (map.get(i).getCorefMentions().size() > 1)
				// add to local map
				m.put(i, map.get(i));
		}
		// return map
		return m;
	}

	/**
	 * Method for capturing semantic roles specified in the array <em>relations</em>
	 * @param sg SemanticGraph
	 * @param id document id
	 */
	public void capture (SemanticGraph sg, String id) {
		// for each relation
		for (int i = 0; i < relations.length; i++) {
			// find all edges
			List<SemanticGraphEdge> edges = sg.findAllRelns(relations[i]);
			// for each edge
			for (SemanticGraphEdge sge : edges) {
				// build relations
				buildRelations(sge, types[i], id);
			}
		}
		// extra for prepositions
		for (GrammaticalRelation gr : EnglishGrammaticalRelations.getPreps()) {
			List<SemanticGraphEdge> edges = sg.findAllRelns(gr);
			for (SemanticGraphEdge sge : edges) {
				buildRelations(sge, "p_"+gr.toString().split("_")[1], id);
			}
		}
	}

	/**
	 * Builds relations from SemanticGraphEdge and type
	 * @param sge SemanticGraphEdge
	 * @param type type
	 * @param id document id
	 */
	private void buildRelations (SemanticGraphEdge sge, String type, String id) {
		// check if tag is verb
		if (sge.getGovernor().tag().startsWith("V")) {
			// ignore "be" and "do"
			if (sge.getGovernor().lemma().equals("be")||sge.getGovernor().lemma().equals("do"))
				return;
			//System.err.println("Resolving coref...");
			// build typed dependency with lemma, type and resolved coref and add to builder
			tdl.add(new TypedDep(sge.getGovernor().lemma(), type, resolveCoref(sge.getTarget()), id));
		}
	}

	/**
	 * Method for processing a text
	 * @param text text to process
	 * @param id document id
	 */
	public synchronized void run (String text, String id) {
		// run preprocessor
		pp.process(text);
		// grab graph
		graph = cleanCorefGraph(pp.getCorefChains());
		// grab sentences
		sentences = pp.getSemanticGraphs();
		// for each sentence, capture all roles
		System.err.println("Capturing roles...");
		for (SemanticGraph sg : sentences) {
			capture(sg, id);
		}
		// TODO change to list or leave as set
		Set<NarrativeChain> ncl = new HashSet<NarrativeChain>();
		System.err.println("Building chains...");
		for (IndexedWord entity : entities) {
			NarrativeChain nc = new NarrativeChain();
			nc.buildChain(tdl.getList(), entity);
			if (!nc.empty())
				ncl.add(nc);
		}
		// write to buffer file
		try {
			// do not write empty chain IDs
			if (ncl.isEmpty())
				return;
			ncw.write(id);
			ncw.write(";");
			for (NarrativeChain nc : ncl) {
				ncw.write(nc.toLinearString() + "&!&");
			}
			ncw.write("\n");
		} catch (IOException ioe) {
			System.err.println("An exception occured:\n" + ioe.getMessage());
		}
		ncl.clear();
		reinitialize();
		System.out.println("Reinitialize...");
	}

	/**
	 * Method for processing folder containing NYT files
	 * <p>
	 * Only used for NYT format files. Otherwise annotated files
	 * might yield errors
	 * @param folder path to folder
	 * @throws IOException 
	 */
	public void runNytFolder (String folder) throws IOException {
		// initialize reader
		Reader r = new Reader();
		// process each file
		for (File f : new File(folder).listFiles()) {
			System.err.println("Opening file..." + f.getName());
			// read all texts from file
			List<String> texts = r.readNytFile(f);
			// for each text
			for (String text : texts) {
				// retrieve id and text
				String id = text.split(r.getSplit())[0];
				String t = text.split(r.getSplit())[1];
				// textual output
				System.err.println("Reading text..." + id);
				// catch out of memory exception thrown by Stanford Parser
				try {
					run(t,id);
				} catch (OutOfMemoryError oome) {
					// textual output
					System.err.println("Skipping text..." + id);
					// write error file
					try {
						ncw.writeError(id + "\n");
					} catch (IOException e) {
						// ignore error
					}
					// jump to next iteration
					continue;
				}
				System.err.println("Finished..." + id);			
			}
		}
	}
	
	/**
	 * Method for processing folder of simple text files
	 * <p>Only used for plain text files. XML or otherwise annotated files
	 * might yield errors
	 * @param folder path to folder
	 */
	public void runFolder (String folder) {
		// initialize reader
		Reader r = new Reader();
		// process each file
		for (File f : new File(folder).listFiles()) {
			// text buffer
			String text = "";
			// document id
			String id = f.getName();
			System.err.println("Opening file..." + id);
			// default try-catch block
			try {
				text = r.readFile(f);
			} catch (IOException e) {
				// ignore error
			}
			// catch OutOfMemoryException thrown by Stanford Parser
			try {
				run(text,id);
			} catch (OutOfMemoryError oome) {
				// textual output
				System.err.println("Skipping file..." + id);
				// write error file
				try {
					ncw.writeError(id + "\n");
				} catch (IOException e) {
					// ignore error
				}
				// jump to next iteration
				continue;
			}
			System.err.println("Finished..." + id);			
		}
	}

	/**
	 * Resolves word to the most representative corefering mention if applicable
	 * @param word word
	 * @return most representative mention
	 */
	private IndexedWord resolveCoref (IndexedWord word) {
		// check each chain
		for (int i : graph.keySet()) {
			// grab the chain
			CorefChain a = graph.get(i);
			// check each coref mention
			for (CorefMention b : a.getCorefMentions()) {
				// if the word is equal to the most representative mention, return the word
				if (a.getRepresentativeMention().mentionSpan.equals(word.word())) {
					// add word to entities
					if (!myContains(word))
						entities.add(word);
					return word;
				}
				// if the word is found in a chain, return the head word of the most representative mention of the word
				if (b.mentionSpan.equals(word.word())) {
					// fetch index of headword
					int index = a.getRepresentativeMention().headIndex;
					// fetch index of sentence -1 (because sentences indexed from 1, list from 0)
					int sindex = a.getRepresentativeMention().sentNum-1;
					// add word to entities
					if (!myContains(word))
						entities.add(word);
					try {
						return sentences.get(sindex).getNodeByIndex(index);
					} catch (IllegalArgumentException e) {
						System.out.println(sentences.get(sindex) + ";" + index);
					}
				}
			}
		}
		// method should never reach this point
		return word;
	}

	/**
	 * Private helper method for simple IndexedWord comparison
	 * @param iw IndexedWord
	 * @return whether the array <em>entities</em> contains an IndexedWord with the same text and tag
	 */
	private boolean myContains (IndexedWord iw) {
		// get text
		String word = iw.word();
		// get tag
		String pos = iw.tag();
		// look through entities
		for (IndexedWord iw2 : entities) {
			// get text and tag
			String w2 = iw2.word();
			String pos2 = iw2.tag();
			// if it exists in entities, return true
			if (word.equals(w2) && pos.equals(pos2))
				return true;
		}
		// else return false
		return false;
	}

	/**
	 * Returns the path to the buffer file
	 * @return path to buffer file
	 */
	public String getPath () {
		return ncw.getPath();
	}

	/**
	 * Set the path to the buffer file
	 * @param path path to buffer file
	 */
	public void setPath (String path) {
		ncw.setPath(path);
	}
	
	/**
	 * Set the path to the error file
	 * @param path path to error file
	 */
	public void setErrorPath (String path) {
		ncw.setErrorPath(path);
	}
}
