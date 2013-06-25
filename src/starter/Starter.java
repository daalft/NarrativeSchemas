package starter;

import java.io.IOException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import schema.SchemaBuilder;

import chain.ChainBuilder;
import chain.PairBuilder;

public class Starter {

	private static Options options;

	public Starter () {

	}

	private static void initialize () {
		// create Options object
		options = new Options();
		options.addOption("corpus", true, "Path to corpus file(s)");
		options.addOption("f", false, "Flag to indicate whether path p is a folder");
		options.addOption("nyt", false, "Flag to indicate whether path p contains NYT files");
		options.addOption("buffer", true, "Path to buffer file");
		options.addOption("error", true, "Path to error buffer");
		options.addOption("np", false, "No parse. Complete only second and third step");
		options.addOption("size", true,"Schema size");
		options.addOption("fpi", false,"Flag to indicate whether to use full prepositional information");
		options.addOption("lambda", true,"Weighting factor lambda");
		options.addOption("beta", true,"Weighting factor beta");
		options.addOption("shuffle", false,"Shuffle");
		options.addOption("sort", false,"Sort");
		options.addOption("output", true,"Output filename");
		options.addOption("write", false,"Write frequency file after calculation");
		options.addOption("co", false, "ChainBuilder only. Complete only the first step");
		options.addOption("so", false, "SchemaBuilder only. Complete only the third step");
	}

	public void run (String buffer, String error, String path, boolean nyt, boolean folder, boolean np, boolean shuffle, boolean sort,
			String filename, boolean write, boolean fpi, String lambda, String beta, String size, boolean co, boolean so) throws IOException {

		////////////////////////////////////
		// Chain Builder
		////////////////////////////////////

		ChainBuilder cb = new ChainBuilder();
		cb.setPath(buffer);
		if (!so) {
			if (!np) {
				cb.setErrorPath(error);
				if (nyt)
					cb.runNytFolder(path);
				else if (folder)
					cb.runFolder(path);
				else
					cb.run(path, "defaultId000");
			}
			if (co)
				return;
		}

		////////////////////////////////////
		// Pair Builder
		////////////////////////////////////

		PairBuilder pb = new PairBuilder();
		pb.setPath(cb.getPath());
		pb.setPairPath(cb.getPath()+"_pairs");
		if (!so) {
			pb.generatePairs();
		}

		////////////////////////////////////
		// Schema Builder
		////////////////////////////////////

		SchemaBuilder sb = new SchemaBuilder();
		if (fpi)
			sb.setUseFullArgument(fpi);
		if (lambda != null && !lambda.equals("")) {
			try {
				double lambd = Double.parseDouble(lambda);
				sb.setLambda(lambd);
			} catch (Exception e) {
				System.err.println("An error occurred! Wrong format for lambda.");
			}
		}
		if (beta != null && !beta.equals("")) {
			try {
				double bet = Double.parseDouble(beta);
				sb.setBeta(bet);
			} catch (Exception e) {
				System.err.println("An error occurred! Wrong format for beta.");
			}
		}
		if (size != null && !size.equals("")) {
			try {
				int si = Integer.parseInt(size);
				sb.setSchemaSize(si);
			} catch (Exception e) {
				System.err.println("An error occurred! Wrong format for schema size.");
			}
		}
		sb.setPathToPairBuffer(pb.getPairPath());
		sb.run(shuffle, sort, filename, write);
	}

	public static void main(String[] args) throws ParseException {
		initialize();
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse(options, args);
		if (!(cmd.hasOption("corpus")||(cmd.hasOption("f")&&cmd.hasOption("nyt"))||cmd.hasOption("buffer")||cmd.hasOption("error")
				|| cmd.hasOption("output"))) {
			System.err.println("Wrong number of arguments.");
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "java", options );
			return;
		}
		if (cmd.hasOption("f") && cmd.hasOption("nyt")) {
			System.err.println("Only one flag can be set at a time. Either folder or NYT folder!");
			return;
		}
		String b = cmd.getOptionValue("buffer");
		String e = cmd.getOptionValue("error");
		String p = cmd.getOptionValue("corpus");
		boolean nyt = cmd.hasOption("nyt");
		boolean f = cmd.hasOption("f");
		boolean noparse = cmd.hasOption("np");
		boolean shuffle = cmd.hasOption("shuffle");
		boolean sort = cmd.hasOption("sort");
		String filename = cmd.getOptionValue("output");
		boolean fpi = cmd.hasOption("fpi");
		boolean write = cmd.hasOption("write");
		String lambda = cmd.getOptionValue("lambda");
		String beta = cmd.getOptionValue("beta");
		String size = cmd.getOptionValue("size");
		boolean co = cmd.hasOption("co");
		boolean so = cmd.hasOption("so");
		long start = System.currentTimeMillis();
		long interrupt = 0L;
		try {
			new Starter().run(b, e, p, nyt, f, noparse, shuffle, sort, filename, write, fpi, lambda, beta, size, co, so);
		} catch (Exception ex) {
			ex.printStackTrace();
			interrupt = System.currentTimeMillis();
		} finally {
			long end = System.currentTimeMillis();
			long time = 0L;
			if (interrupt > 0) {
				System.err.println("Programm encountered an error!");
				time = interrupt - start;
			} else {
				time = end - start;
			}
			long sec = time/1000;
			double min = sec/60;
			double hour = min/60;
			double day = hour/24;
			double week = day/7;
			double year = day/365;
			System.out.println("Time taken:\n\t" + time + " ms.\n\t" 
					+ sec + " sec.\n\t"
					+ min + " min.\n\t"
					+ hour + " hours.\n\t"
					+ day + " days.\n\t"
					+ week + " weeks.\n\t"
					+ year + " years.");
		}
	}
}
