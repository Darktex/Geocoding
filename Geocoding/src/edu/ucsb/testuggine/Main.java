package edu.ucsb.testuggine;
import static org.kohsuke.args4j.ExampleMode.ALL;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.parser.ParseException;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class Main {

	@Option(name="-yelp",usage="Flags if Yelp must be used")
	private boolean yelp = false;

	@Option(name="-ta",usage="Flags if TA must be used")
	private boolean ta = false;

	@Option(name="-ot",usage="Flags if OT must be used")
	private boolean ot = false;
	
	// receives other command line parameters than options
	@Argument
	private List<String> arguments = new ArrayList<String>();

	public static void main(String[] args) throws IOException, SQLException, ParseException {
		new Main().doMain(args);
	}

	public void doMain(String[] args) throws IOException, SQLException, ParseException {

		for (String arg : args)
			arguments.add(arg);

		CmdLineParser parser = new CmdLineParser(this);

		// if you have a wider console, you could increase the value;
		// here 80 is also the default
		parser.setUsageWidth(800);

		try {
			// parse the arguments.
			parser.parseArgument(args);

			// you can parse additional arguments if you want.
			// parser.parseArgument("more","args");

			// after parsing arguments, you should check
			// if enough arguments are given.
			if( arguments.isEmpty() )
				throw new CmdLineException("No argument is given");

		} catch( CmdLineException e ) {
			// if there's a problem in the command line,
			// you'll get this exception. this will report
			// an error message.
			System.err.println(e.getMessage());
			System.err.println("java TADBInterface.jar [options...] arguments...");
			// print the list of available options
			parser.printUsage(System.err);
			System.err.println();

			// print option sample. This is useful some time
			System.err.println("  Usage: java TADBInterface.jar "+parser.printExample(ALL));

			return;
		}

		if (yelp) {
			YelpGeo yelpgeo = new YelpGeo();
		}
		if (ta) {
			TAGeo tageo = new TAGeo();
		}
		if (ot) {
			OTGeo otgeo = new OTGeo();
		}

	}
}