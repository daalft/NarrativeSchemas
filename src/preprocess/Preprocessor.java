package preprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;

/**
 * Class for preprocessing text
 * @author David
 *
 */
public class Preprocessor {

	/**
	 * A list of SemanticGraph
	 */
	private List<SemanticGraph> sgs;
	/**
	 * A map of coreferences
	 */
	private Map<Integer, CorefChain> ccg;
	
	/**
	 * No-argument constructor
	 */
	public Preprocessor () {
		sgs = new ArrayList<SemanticGraph>();
		ccg = new HashMap<Integer, CorefChain>();
	}
	/**
	 * Preprocesses a text
	 * <p>
	 * The text is run through the entire StanfordCoreNLP Pipeline
	 * @param text text
	 */
	public void process (String text) {
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		Annotation document = new Annotation(text);
		pipeline.annotate(document);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		for(CoreMap sentence: sentences) {
			sgs.add(sentence.get(CollapsedCCProcessedDependenciesAnnotation.class));
		}
		ccg =  document.get(CorefChainAnnotation.class);
	}
	
	/**
	 * Method for retrieving the SemanticGraph list
	 * @return List&lt;SemanticGraph&gt;
	 */
	public List<SemanticGraph> getSemanticGraphs () {
		return sgs;
	}
	
	/**
	 * Method for retrieving the Coreference graph
	 * @return Map&lt;Integer, CorefChain&gt;
	 */
	public Map<Integer, CorefChain> getCorefChains () {
		return ccg;
	}
}
