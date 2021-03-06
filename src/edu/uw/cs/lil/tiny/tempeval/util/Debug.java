package edu.uw.cs.lil.tiny.tempeval.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.uw.cs.utils.composites.Pair;

public class Debug {
	private static final String LOG_ROOT = "logs/";
	private static String logDir = LOG_ROOT; // Put logs in root by default
	private static SimpleDateFormat formatter = new SimpleDateFormat("HH-mm-ss_MM-dd-yyyy");

	public static enum Type {
		PROGRESS, STATS, ATTRIBUTE, DETECTION, DEBUG, ERROR, INCORRECT_ATTRIBUTE, DEBUG_ATTRIBUTE, PARSE_SELECTION, UNKNOWN_INCORRECT;
	}
	private static Map<Type, Set<Pair<String, PrintStream>>> filter = new HashMap<Type, Set<Pair<String, PrintStream>>>();

	private static String getTimestamp() {
		return formatter.format(new Date());
	}

	public static void setLogs(String dir) {
		logDir = LOG_ROOT + dir + "/";
		new File(logDir).mkdirs();
	}
	
	public static void setLogs() {
		setLogs("autogen-" + getTimestamp());
	}
	
	public static void addFilter(String prefix, PrintStream out, Type... types) {
		for (Type t : types){
			if (!filter.containsKey(t))
				filter.put(t, new HashSet<Pair<String, PrintStream>>());
			filter.get(t).add(Pair.of(prefix, out));
		}
	}
	
	public static void addFilter(String prefix, String filename, Type... types) throws FileNotFoundException {
		addFilter(prefix, new PrintStream(new File(logDir + filename)), types);
	}

	public static void println(Type t, Object s) {
		if (filter.containsKey(t)) {
			Set<Pair<String, PrintStream>> pairs = filter.get(t);
			for(Pair<String, PrintStream> p : pairs)
				p.second().println(p.first() + s);
		}
	}
	
	public static void println(Type t) {
		if (filter.containsKey(t)) {
			Set<Pair<String, PrintStream>> pairs = filter.get(t);
			for(Pair<String, PrintStream> p : pairs)
				p.second().println(p.first());
		}
	}
	
	public static void print(Type t, Object s) {
		if (filter.containsKey(t)) {
			Set<Pair<String, PrintStream>> pairs = filter.get(t);
			for(Pair<String, PrintStream> p : pairs)
				p.second().print(p.first() + s);
		}
	}
	
	public static void printf(Type t, String s, Object... args) {
		if (filter.containsKey(t)) {
			Set<Pair<String, PrintStream>> pairs = filter.get(t);
			for(Pair<String, PrintStream> p : pairs)
				p.second().printf(p.first() + s, args);
		}
	}
}
