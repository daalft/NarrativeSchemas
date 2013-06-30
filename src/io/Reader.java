package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import chain.element.Entry;
import chain.element.Event;

/**
 * Class used to read different file formats
 * @author David
 *
 */
public class Reader {

	/**
	 * Patterns
	 */
	private Pattern _doc, _text, _id;
	/**
	 * Delimiter between document id and text. Split on this delimiter to retrieve id and text. Must be canonical (unique)
	 */
	private final static String canonicalDelimiter = "&!&";
	/**
	 * Fall-back prefix for id generation
	 */
	private final static String fallbackPrefix = "fbprefix";
	/**
	 * Fall-back counter for id generation
	 */
	private static int counter;
	/**
	 * List of events for retrieval by SchemaBuilder
	 */
	private List<Event> events;
	/**
	 * PMI lookup table for retrieval by SchemaBuilder
	 */
	private Map<String, Double> table;
	/**
	 * Dependency pool for retrieval by SchemaBuilder
	 */
	private List<String> dependencyPool;
	
	/**
	 * No-argument constructor
	 */
	public Reader () {
		_doc = Pattern.compile("<DOC\\sid=.+?</DOC>");
		_text = Pattern.compile("<TEXT>.+?</TEXT>");
		_id = Pattern.compile("(?<=<DOC\\sid=\")[\\w\\d\\.]+(?=.+?>)");
		events = new ArrayList<Event>();
		table = new HashMap<String, Double>();
		dependencyPool = new ArrayList<String>();
	}
	
	/**
	 * Deserializing method for serialized files
	 * @param path path
	 * @return entry list
	 * @deprecated
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public List<Entry> deserialize (String path) throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(path));
		List<Entry> e = null;	
		e = (List<Entry>)in.readObject();
		in.close();
		return e;
	}
	
	/**
	 * Returns the canonical delimiter used to delimit id and text
	 * @return delimiter
	 */
	public String getSplit () {
		return canonicalDelimiter;
	}
	/**
	 * Method for reading plain text file content
	 * @param f path to file
	 * @return text
	 * @throws IOException
	 */
	public String readFile (File f) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(f));
		String l = "";
		StringBuilder sb = new StringBuilder();
		while ((l=br.readLine())!=null)
			sb.append(l).append(System.lineSeparator());
		br.close();
		return sb.toString();
	}

	/**
	 * Method for extracting text from a NYT-formatted text file
	 * <p>
	 * Only applicable to single files
	 * @param f NYT file
	 * @return list of String
	 * @throws IOException
	 */
	public List<String> readNytFile (File f) throws IOException {
		// text buffer
		List <String> buffer = new ArrayList<String>();
		// file reading loop
		BufferedReader br = new BufferedReader(new FileReader(f));
		String l = "";
		StringBuilder sb = new StringBuilder();
		while ((l = br.readLine())!=null) 
			sb.append(l).append(" ");
		// close reader
		br.close();
		String t = sb.toString();
		// match creation
		Matcher m_doc = _doc.matcher(t);
		Matcher m_text = _text.matcher(t);
		// for every text found inside file
		while (m_doc.find()) {
			// id buffer
			String id = "";
			// document text
			String _local = m_doc.group();
			// get id if possible
			Matcher m_id = _id.matcher(_local);
			if (m_id.find())
				id = m_id.group();
			// generate fall-back id
			else
				id = fallbackPrefix  + counter++;
			// get inner text
			if (m_text.find()) {
				// locally store text
				String _intermed = m_text.group();
				// delete remaining tags
				_intermed = _intermed.replaceAll("<.+?>", " ");
				// replace quotes
				_intermed = _intermed.replaceAll("''", "\"");
				_intermed = _intermed.replaceAll("``", "\"");
				// delete quoted content
				_intermed = _intermed.replaceAll("\".+?\"", "");
				// prepend id
				_intermed = id + canonicalDelimiter + _intermed;
				// add text to buffer
				buffer.add(_intermed);
			}
		}	
		return buffer;
	}
	
	/**
	 * Method to read pair buffer file for SchemaBuilder
	 * @param path path to pair buffer file
	 * @param fullPrep flag to indicate that full preposition information should be used
	 * @throws IOException 
	 */
	public void readPairBuffer (String path, boolean fullPrep) throws IOException {
		if (path.equals(""))
			return;
		BufferedReader br = new BufferedReader(new FileReader(new File(path)));
		String l = "";
		while ((l = br.readLine())!=null) {
			// ignore headers and empty lines
			if (!l.contains("|"))
				continue;
			String[] sp = l.split("\t");
			// sp[0] = typed dep 1
			// sp[1] = typed dep 2
			String[] pm = sp[2].split("\\|");
			//System.out.println(sp[2].split("\\|").length);
			// pm[0] = pmi
			double pmi = Double.parseDouble(pm[0]);
			// mention is twice the same in one row, hence retrieval of only one is enough
			// mention is letters enclosed in parentheses
			Pattern p = Pattern.compile("(?<=\\()[\\w'-\\.]+?(?=\\))");
			Matcher m = p.matcher(pm[1]);
			// matcher should always find match
			m.find();
			String mention = m.group();
			if (!fullPrep) {
				if (sp[0].contains("p_"))
					sp[0] = sp[0].substring(0, sp[0].indexOf('_'));
				if (sp[1].contains("p_"))
					sp[1] = sp[1].substring(0, sp[1].indexOf('_'));
			}
			Event e1 = new Event(sp[0], mention, "");
			Event e2 = new Event(sp[1], mention, "");
			events.add(e1);
			events.add(e2);
			table.put(sp[0]+sp[1], pmi);
			addDependencyToPool(e1.getDependency(fullPrep));
			addDependencyToPool(e2.getDependency(fullPrep));
		}
		br.close();
	}
	
	/**
	 * Helper method to retain only unique elements
	 * @param dependency dependency to add
	 */
	private void addDependencyToPool (String dependency) {
		if (!dependencyPool.contains(dependency))
			dependencyPool.add(dependency);
	}
	
	public List<Event> getEvents () {
		return events;
	}
	
	public Map<String, Double> getTable () {
		return table;
	}
	
	public List<String> getDependencyPool () {
		return dependencyPool;
	}
}
