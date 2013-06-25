package io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import chain.element.Entry;

/**
 * Class used to write different output formats
 * @author David
 *
 */
public class NCWriter {
	/**
	 * Default path to output file
	 */
	private static String defaultPath = "./buffer";
	/**
	 * Default path to error file
	 */
	private static String defaultErrorPath = "./bufferError";

	/**
	 * No-argument constructor
	 */
	public NCWriter () {

	}

	/**
	 * Writes XML to output file
	 * <p>Always appends content to file
	 * @param e entry to save
	 * @throws ParserConfigurationException 
	 * @throws TransformerConfigurationException 
	 */
	public void writeXML (Entry e) throws ParserConfigurationException, TransformerConfigurationException {
		
	}
	
	/**
	 * Writes to output file
	 * <p>Always appends content to file
	 * @param text text to write
	 * @throws IOException
	 */
	public void write (String text, String...file) throws IOException {
		String path = "";
		if (file.length > 0) {
			path = file[0];
		} else {
			path = defaultPath;
		}
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(path), true));
		bw.write(text);
		bw.close();
	}

	/**
	 * Writes pair output to file
	 * @param pair output to write
	 * @throws IOException
	 */
	public void writePair (String pair, String...file) throws IOException {
		String path = "";
		if (file.length > 0) {
			path = file[0];
		} else {
			path = defaultPath + "_pairs";
		}
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(path), true));
		bw.write(pair);
		bw.close();
	}
	
	/**
	 * Serializes a list of entries
	 * <p>
	 * The file name is the default file name with an additional
	 * <em>.ser</em> file ending
	 * @param le list of entries to write
	 * @throws IOException 
	 */
	public void writeSerial (List<Entry> le) throws IOException {
		FileOutputStream fileOut = new FileOutputStream(defaultPath + ".ser", true);
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(le);
		out.close();
		fileOut.close();
	}

	/**
	 * Writes to error file
	 * <p>Always appends content to file
	 * @param text text to write
	 * @throws IOException
	 */
	public void writeError (String text) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(defaultErrorPath), true));
		bw.write(text);
		bw.close();
	}

	/**
	 * Method for changing the output path
	 * <p>Argument can be filename or directory. 
	 * In the case of a directory, the default filename <em>buffer.file</em>
	 * will be used
	 * @param p new path
	 */
	public void setPath (String p) {
		if (new File(p).isDirectory()) {
			p += "buffer.file";
		}
		defaultPath = p;
	}

	/**
	 * Method for changing the error output path
	 * <p>Argument can be filename or directory. 
	 * In the case of a directory, the default filename <em>bufferError.file</em>
	 * will be used
	 * @param p new path
	 */
	public void setErrorPath (String p) {
		if (new File(p).isDirectory()) {
			p += "bufferError.file";
		}
		defaultErrorPath = p;
	}

	public String getPath () {
		return defaultPath;
	}
}
