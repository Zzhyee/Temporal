package edu.uw.cs.lil.tiny.learn.weakp;

import edu.uw.cs.utils.collections.ArrayUtils;

public class WeaklySupervisedPerceptronStats {
	private double					averageGenerationParsingTime	= 0.0;
	
	private double					averageModelParsingTime			= 0.0;
	private int						generationParsingCounter		= 0;
	private int						modelParsingCounter				= 0;
	private final int				numIterations;
	private final int				numSamples;
	private final int[]				numUpdates;
	final private SampleStat[][]	sampleStat;
	
	public WeaklySupervisedPerceptronStats(int numIterations, int numSamples) {
		this.numIterations = numIterations;
		this.numSamples = numSamples;
		this.sampleStat = new SampleStat[numSamples][numIterations];
		for (int i = 0; i < numSamples; ++i) {
			for (int j = 0; j < numIterations; ++j) {
				this.sampleStat[i][j] = new SampleStat();
			}
		}
		this.numUpdates = new int[numIterations];
	}
	
	public void goldIsOptimal(int itemNumber, int iterationNumber) {
		sampleStat[itemNumber][iterationNumber].goldIsOptimal = true;
	}
	
	public void hasValidParse(int itemNumber, int iterationNumber) {
		sampleStat[itemNumber][iterationNumber].hasValidParse = true;
	}
	
	public void numNewLexicalEntries(int itemNumber, int iterationNumber,
			int num) {
		sampleStat[itemNumber][iterationNumber].numNewLexicalEntries = num;
	}
	
	public void processed(int itemNumber, int iterationNumber) {
		sampleStat[itemNumber][iterationNumber].processed = true;
	}
	
	public void recordGenerationParsing(long time) {
		averageGenerationParsingTime = (averageGenerationParsingTime
				* generationParsingCounter + time)
				/ (generationParsingCounter + 1);
		++generationParsingCounter;
	}
	
	public void recordModelParsing(long time) {
		averageModelParsingTime = (averageModelParsingTime
				* modelParsingCounter + time)
				/ (modelParsingCounter + 1);
		++modelParsingCounter;
	}
	
	@Override
	public String toString() {
		final StringBuilder ret = new StringBuilder();
		ret.append(String
				.format("Performed %d lexical generation parses with average time of %.4fsec\n",
						generationParsingCounter,
						averageGenerationParsingTime / 1000.0));
		ret.append("Updates per iteration: ")
				.append(ArrayUtils.join(numUpdates, ", ")).append("\n");
		ret.append(String.format(
				"Performed %d model parses with average time of %.4fsec\n",
				modelParsingCounter, averageModelParsingTime / 1000.0));
		ret.append("Gold standard as optimal model parse:\n");
		final int[] counters = new int[numIterations];
		final int[] lexicalGenerationCounters = new int[numIterations];
		for (int itemCounter = 0; itemCounter < numSamples; ++itemCounter) {
			ret.append(
					String.format("%1$-" + String.valueOf(numSamples).length()
							+ "s", String.valueOf(itemCounter))).append(" :: ");
			int iterationCounter = 0;
			for (final SampleStat stat : sampleStat[itemCounter]) {
				ret.append(stat.toString());
				counters[iterationCounter] += stat.goldIsOptimal ? 1 : 0;
				lexicalGenerationCounters[iterationCounter] += stat.numNewLexicalEntries;
				++iterationCounter;
			}
			ret.append('\n');
		}
		ret.append("New lexical entries per iteration: ")
				.append(ArrayUtils.join(lexicalGenerationCounters, ", "))
				.append("\n");
		ret.append("Correct per iteration: ")
				.append(ArrayUtils.join(counters, ", ")).append("\n");
		
		return ret.toString();
	}
	
	public void triggeredUpdate(int itemNumber, int iterationNumber) {
		numUpdates[iterationNumber]++;
		sampleStat[itemNumber][iterationNumber].triggeredUpdate = true;
	}
	
	private static class SampleStat {
		boolean	goldIsOptimal			= false;
		boolean	hasValidParse			= false;
		int		numNewLexicalEntries	= 0;
		boolean	processed				= false;
		boolean	triggeredUpdate			= false;
		
		@Override
		public String toString() {
			if (numNewLexicalEntries == 0) {
				return String.format("%s  ", Character.toString(toChar()));
			} else {
				return String.format("%s%2d", Character.toString(toChar()),
						numNewLexicalEntries);
			}
		}
		
		private char toChar() {
			if (goldIsOptimal) {
				if (triggeredUpdate) {
					return 'G';
				} else {
					return 'g';
				}
			} else if (hasValidParse) {
				if (triggeredUpdate) {
					return 'V';
				} else {
					return 'v';
				}
			} else if (processed) {
				return '.';
			} else {
				return ' ';
			}
		}
	}
}
