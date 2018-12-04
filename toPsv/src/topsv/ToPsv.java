package topsv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import com.opencsv.*;
import org.apache.commons.cli.*;

public class ToPsv {
	static Options defineOptions(){
		Options options = new Options();
		
		Option opSkip = Option.builder("s")
                		.longOpt("skip").argName("Y or N")
            			.numberOfArgs(1)
                		.required(false)
                		.desc("Skip first line")
                		.build();
        options.addOption(opSkip);
        
        Option opDelim = Option.builder("d")
        				.longOpt("delim")
        				.argName("character , comma or \t tab")
            			.numberOfArgs(1).required(false)
            			.desc("Field delimiter")
            			.build();
        options.addOption(opDelim);
        
        
        return options;
	}
	static boolean skip = true;
	static String delimiter = ",";
	
	public static void main(String[] args) {
		ToPsv obj = new ToPsv();
		int argc = args.length;
		
		// set up of command line options
		Options options = defineOptions();
		CommandLineParser parser = new DefaultParser();
		CommandLine line = null;
		try {
			line = parser.parse(options, args);
	    } catch (ParseException ex) {
	        System.err.println("Parsing failed.  Reason: " + ex.getMessage());
	        System.exit(1);
	    }
		
		if (line.hasOption("help") || argc == 0) {
		    HelpFormatter formatter = new HelpFormatter();
		    formatter.printHelp("toPsv", options);
		    System.exit(0);
		}
		if(line.hasOption("skip")) {
			 String s = line.getOptionValue( "skip" );
			 if(s.toLowerCase().equals("n")) {
			 	skip = false;
			 }
		}
		if(line.hasOption("delim")) {
			String d = line.getOptionValue("delim");	
			if(d.equals("tab") || d.equals("\t")) {
				delimiter ="tab";
			}
		}
		obj.readDaFile(args[0]);
	}

	public void readDaFile(String filePath) {
		File inputFile = new File(filePath);
		char sepchr = "tab".equals(delimiter)?'\t':',';
		//boolean skip = "skip".equals(hdr)?true:false;
		BufferedReader br = null;
		int lineNumber = 0;
		String line = "";
		try {
			br = new BufferedReader(new FileReader(inputFile));
			while ((line = br.readLine()) != null) {
				lineNumber++;
				if (lineNumber == 1 && skip)
					continue; // header skip it
				if (line.trim().length() == 0) //skip empty line (only ws)
					continue;

				final CSVParser parser = new CSVParserBuilder().withSeparator(sepchr).withQuoteChar('\"').build();
				final CSVReader reader = new CSVReaderBuilder(new StringReader(line)).withCSVParser(parser).build();

				String[] tokens;
				while ((tokens = reader.readNext()) != null) {

					try {
						String res = "";
						boolean flg = false; 
						// must use flag since first
						// field may be empty and can not skip it
						for (String s : tokens) {
							if (flg)
								res += "|";
							else
								flg = true;
							res += s;
						}
						System.out.println(res);
					} catch (ArrayIndexOutOfBoundsException exp) {
						for (String s : tokens) {
							System.out.println(lineNumber + ": Exception: " + lineNumber + " :" + s);
						}
						System.out.println();
						continue;
					}
				}
			}
		} catch (FileNotFoundException e) {
			System.err.println(line);
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println(line);
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.err.println("Line count:\t" + lineNumber);
		// System.out.println("Data count:\t"+ data.size());
		return;
	}

}
