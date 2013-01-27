/*******************************************************************************
 * tiny - a semantic parsing framework. Copyright (C) 2013 Yoav Artzi
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 ******************************************************************************/
package edu.uw.cs.lil.tiny.learn.weakp.loss;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.uw.cs.lil.tiny.ccg.categories.Category;
import edu.uw.cs.lil.tiny.ccg.categories.syntax.Syntax;
import edu.uw.cs.lil.tiny.data.IDataCollection;
import edu.uw.cs.lil.tiny.data.IDataItem;
import edu.uw.cs.lil.tiny.data.ILossDataItem;
import edu.uw.cs.lil.tiny.data.lexicalgen.ILexicalGenerationLossDataItem;
import edu.uw.cs.lil.tiny.data.sentence.Sentence;
import edu.uw.cs.lil.tiny.data.singlesentence.SingleSentence;
import edu.uw.cs.lil.tiny.learn.ILearner;
import edu.uw.cs.lil.tiny.learn.weakp.WeaklySupervisedPerceptronStats;
import edu.uw.cs.lil.tiny.mr.lambda.ccg.SimpleFullParseFilter;
import edu.uw.cs.lil.tiny.parser.IParseResult;
import edu.uw.cs.lil.tiny.parser.IParserOutput;
import edu.uw.cs.lil.tiny.parser.Pruner;
import edu.uw.cs.lil.tiny.parser.ccg.cky.AbstractCKYParser;
import edu.uw.cs.lil.tiny.parser.ccg.cky.chart.AbstractCellFactory;
import edu.uw.cs.lil.tiny.parser.ccg.lexicon.LexicalEntry;
import edu.uw.cs.lil.tiny.parser.ccg.lexicon.Lexicon;
import edu.uw.cs.lil.tiny.parser.ccg.model.IDataItemModel;
import edu.uw.cs.lil.tiny.parser.ccg.model.Model;
import edu.uw.cs.lil.tiny.utils.hashvector.HashVectorFactory;
import edu.uw.cs.lil.tiny.utils.hashvector.IHashVector;
import edu.uw.cs.lil.tiny.weakp.loss.parser.IScoreFunction;
import edu.uw.cs.lil.tiny.weakp.loss.parser.ccg.cky.chart.ScoreSensitiveCellFactory;
import edu.uw.cs.utils.collections.SetUtils;
import edu.uw.cs.utils.composites.Pair;
import edu.uw.cs.utils.filter.IFilter;
import edu.uw.cs.utils.log.ILogger;
import edu.uw.cs.utils.log.LoggerFactory;

/**
 * Loss-sensitive perceptron.
 * <p>
 * Parameter update step inspired by: Natasha Singh-Miller and Michael Collins.
 * 2007. Trigger-based Language Modeling using a Loss-sensitive Perceptron
 * Algorithm. In proceedings of ICASSP 2007.
 * </p>
 * <p>
 * Lexical generation step inspired by: Luke S. Zettlemoyer and Michael Collins.
 * Online Learning of Relaxed CCG Grammars for Parsing to Logical Form. In
 * Proceedings of the Joint Conference on Empirical Methods in Natural Language
 * Processing and Computational Natural Language Learning (EMNLP-CoNLL), 2007.
 * </p>
 * 
 * @author Yoav Artzi
 * @param <Y>
 */
public class LossSensitivePerceptronCKY<Y> implements
		ILearner<Sentence, Y, Model<Sentence, Y>> {
	private static final ILogger															LOG	= LoggerFactory
																										.create(LossSensitivePerceptronCKY.class
																												.getName());
	private final IFilter<Category<Y>>														completeParseFilter;
	private final IScoreFunction<Y>															lexicalGenerationSecondaryPruningFunction;
	/**
	 * Generator for lexical entries from evidence.
	 */
	private final int																		lexiconGenerationBeamSize;
	private final IValidator<Y>																lexiconGenerationValidator;
	private final double																	margin;
	private final int																		maxSentenceLength;
	private final int																		numIterations;
	private final AbstractCKYParser<Y>														parser;
	private final WeaklySupervisedPerceptronStats											stats;
	private final IDataCollection<? extends ILexicalGenerationLossDataItem<Sentence, Y, Y>>	trainingData;
	private final Map<Sentence, Y>															trainingDataDebug;
	
	public LossSensitivePerceptronCKY(
			int numIterations,
			double margin,
			IDataCollection<? extends ILexicalGenerationLossDataItem<Sentence, Y, Y>> trainingData,
			Map<Sentence, Y> trainingDataDebug, int maxSentenceLength,
			int lexiconGenerationBeamSize,
			IScoreFunction<Y> lexicalGenerationSecondaryPruningFunction,
			IValidator<Y> lexiconGenerationValidator,
			AbstractCKYParser<Y> parser,
			IFilter<Category<Y>> completeParseFilter) {
		this.numIterations = numIterations;
		this.margin = margin;
		this.trainingData = trainingData;
		this.trainingDataDebug = trainingDataDebug;
		this.maxSentenceLength = maxSentenceLength;
		this.lexicalGenerationSecondaryPruningFunction = lexicalGenerationSecondaryPruningFunction;
		this.lexiconGenerationValidator = lexiconGenerationValidator;
		this.lexiconGenerationBeamSize = lexiconGenerationBeamSize;
		this.parser = parser;
		this.completeParseFilter = completeParseFilter;
		this.stats = new WeaklySupervisedPerceptronStats(numIterations,
				trainingData.size());
		LOG.info(
				"Init LossSensitivePerceptron: numIterations=%d, margin=%f, trainingData.size()=%d, trainingDataDebug.size()=%d, maxSentenceLength=%d ...",
				numIterations, margin, trainingData.size(),
				trainingDataDebug.size(), maxSentenceLength);
		LOG.info(
				"Init LossSensitivePerceptron: ... lexiconGenerationBeamSize=%d, lexicalGenerationSecondaryPruningFunction=%s, lexiconGenerationValidator=%s, completeParseFilter=%s",
				lexiconGenerationBeamSize,
				lexicalGenerationSecondaryPruningFunction,
				lexiconGenerationValidator, completeParseFilter);
	}
	
	@Override
	public void train(Model<Sentence, Y> model) {
		for (int iterationNumber = 0; iterationNumber < numIterations; ++iterationNumber) {
			// Training iteration, go over all training samples
			LOG.info("=========================");
			LOG.info("Training iteration %d", iterationNumber);
			LOG.info("=========================");
			int itemCounter = -1;
			
			for (final ILexicalGenerationLossDataItem<Sentence, Y, Y> dataItem : trainingData) {
				// Process a specific training sample
				
				final long startTime = System.currentTimeMillis();
				
				LOG.info("%d : ================== [%d]", ++itemCounter,
						iterationNumber);
				LOG.info("Sample type: %s", dataItem.getClass().getSimpleName());
				LOG.info("%s", dataItem);
				
				if (dataItem.getSample().getTokens().size() > maxSentenceLength) {
					LOG.warn("Training sample too long, skipping");
					continue;
				}
				
				final IDataItemModel<Y> dataItemModel = model
						.createDataItemModel(dataItem);
				
				// First parse: parse with a generated lexicon and add the best
				// parses to the lexicon.
				
				// Generate lexical entries
				final Lexicon<Y> generatedLexicon = new Lexicon<Y>(
						dataItem.generateLexicon());
				LOG.info("Generated lexicon size = %d", generatedLexicon.size());
				
				// Cell factory for this parse
				final AbstractCellFactory<Y> lexiconGenerationCellFactory = new ScoreSensitiveCellFactory<Y>(
						lexicalGenerationSecondaryPruningFunction, false,
						dataItemModel, dataItem.getSample().getTokens().size(),
						completeParseFilter);
				
				final IParserOutput<Y> generateLexiconParserOutput = parser
						.parse(dataItem, Pruner.create(dataItem),
								dataItemModel, false, generatedLexicon,
								lexiconGenerationBeamSize,
								lexiconGenerationCellFactory);
				
				stats.recordGenerationParsing(generateLexiconParserOutput
						.getParsingTime());
				final List<IParseResult<Y>> allGenerationParses = generateLexiconParserOutput
						.getAllParses();
				
				LOG.info("Lexicon generation parsing time: %.4fsec",
						generateLexiconParserOutput.getParsingTime() / 1000.0);
				LOG.info(
						"Created %d lexicon generation parses for training sample",
						allGenerationParses.size());
				
				// Collect all valid parses and their scores
				final List<IParseResult<Y>> validBestGenerationParses = new LinkedList<IParseResult<Y>>();
				double currentMinLoss = Double.MAX_VALUE;
				double currentMaxModelScore = -Double.MAX_VALUE;
				LOG.info("Generation parses:");
				for (final IParseResult<Y> parse : allGenerationParses) {
					// Check if parse is valid -- different handling for
					// supervised samples
					final boolean isValid;
					if (dataItem instanceof SingleSentence) {
						isValid = dataItem.calculateLoss(parse.getY()) == 0.0;
					} else {
						isValid = lexiconGenerationValidator.isValid(dataItem,
								parse.getY());
					}
					
					final double loss = dataItem.calculateLoss(parse.getY());
					if (isValid) {
						logParse(dataItem, parse, loss, false, dataItemModel);
						if (loss < currentMinLoss) {
							currentMinLoss = loss;
							currentMaxModelScore = parse.getScore();
							validBestGenerationParses.clear();
							validBestGenerationParses.add(parse);
						} else if (loss == currentMinLoss) {
							if (parse.getScore() > currentMaxModelScore) {
								currentMinLoss = loss;
								currentMaxModelScore = parse.getScore();
								validBestGenerationParses.clear();
								validBestGenerationParses.add(parse);
							} else if (parse.getScore() == currentMaxModelScore) {
								validBestGenerationParses.add(parse);
							}
						}
					} else {
						logParse(dataItem, parse, loss, false,
								"Removed invalid: ", dataItemModel);
					}
				}
				
				// Log lexicon generation parses
				LOG.info("%d valid best parses for lexical generation:",
						validBestGenerationParses.size());
				for (final IParseResult<Y> parse : validBestGenerationParses) {
					logParse(dataItem, parse,
							dataItem.calculateLoss(parse.getY()), true,
							dataItemModel);
					LOG.info("Feature weights: %s", model.getTheta()
							.printValues(parse.getAverageMaxFeatureVector()));
				}
				for (final IParseResult<Y> parse : allGenerationParses) {
					if (!validBestGenerationParses.contains(parse)
							&& isGoldDebugCorrect(dataItem.getSample(),
									parse.getY())) {
						LOG.info("The gold parse was present but wasn't the best:");
						logParse(dataItem, parse,
								dataItem.calculateLoss(parse.getY()), true,
								dataItemModel);
						LOG.info("Features: %s",
								parse.getAverageMaxFeatureVector());
						for (final IParseResult<Y> bestParse : validBestGenerationParses) {
							final IHashVector diff = parse
									.getAverageMaxFeatureVector()
									.addTimes(
											-1.0,
											bestParse
													.getAverageMaxFeatureVector());
							diff.dropSmallEntries();
							LOG.info("Best: %s\n\t%s", bestParse,
									bestParse.getAverageMaxFeatureVector());
							LOG.info("DIFF: %s",
									model.getTheta().printValues(diff));
						}
					}
				}
				
				// Add the lexical items that were the best during
				// lexical
				// generation and were included in the optimal parses
				int newLexicalEntries = 0;
				for (final IParseResult<Y> parse : validBestGenerationParses) {
					for (final LexicalEntry<Y> entry : parse
							.getMaxLexicalEntries()) {
						if (model.addLexEntry(entry)) {
							++newLexicalEntries;
						}
						// Add the linked entries
						for (final LexicalEntry<Y> linkedEntry : entry
								.getLinkedEntries()) {
							if (model.addLexEntry(linkedEntry)) {
								++newLexicalEntries;
							}
						}
					}
				}
				stats.numNewLexicalEntries(itemCounter, iterationNumber,
						newLexicalEntries);
				
				// Second parse: using the model with current lexicon. Prune
				// based on the model. Create optimal/non-optimal sets and
				// update on violations.
				final IParserOutput<Y> modelParserOutput = parser.parse(
						dataItem, dataItemModel);
				stats.recordModelParsing(modelParserOutput.getParsingTime());
				final List<IParseResult<Y>> modelParses = modelParserOutput
						.getAllParses();
				
				LOG.info("Created %d model parses for training sample",
						modelParses.size());
				LOG.info("Model parsing time: %.4fsec",
						modelParserOutput.getParsingTime() / 1000.0);
				
				// Record if the best is the gold standard, if known
				final List<IParseResult<Y>> bestModelParses = modelParserOutput
						.getBestParses();
				if (bestModelParses.size() == 1
						&& isGoldDebugCorrect(dataItem.getSample(),
								bestModelParses.get(0).getY())) {
					stats.goldIsOptimal(itemCounter, iterationNumber);
				}
				
				if (modelParses.isEmpty()) {
					LOG.warn("No model parses for: %s", dataItem);
					continue;
				}
				
				// Create the good and bad sets. Each set includes pairs of
				// (loss, parse)
				final Pair<List<Pair<Double, IParseResult<Y>>>, List<Pair<Double, IParseResult<Y>>>> goodBadSetsPair = createOptimalNonOptimalSets(
						dataItem, modelParses);
				final List<Pair<Double, IParseResult<Y>>> optimalParses = goodBadSetsPair
						.first();
				final List<Pair<Double, IParseResult<Y>>> nonOptimalParses = goodBadSetsPair
						.second();
				
				if (!optimalParses.isEmpty()) {
					stats.hasValidParse(itemCounter, iterationNumber);
				}
				
				LOG.info("%d optimal parses, %d non optimal parses",
						optimalParses.size(), nonOptimalParses.size());
				
				LOG.info("Optimal parses:");
				for (final Pair<Double, IParseResult<Y>> pair : optimalParses) {
					logParse(dataItem, pair.second(), pair.first(), false,
							dataItemModel);
				}
				
				LOG.info("Non-optimal parses:");
				for (final Pair<Double, IParseResult<Y>> pair : nonOptimalParses) {
					logParse(dataItem, pair.second(), pair.first(), false,
							dataItemModel);
				}
				
				if (optimalParses.isEmpty() || nonOptimalParses.isEmpty()) {
					LOG.info("No optimal/non-optimal parses -- skipping");
					continue;
				}
				
				// Create the relative loss function
				final RelativeLossFunction deltaLossFunction = new RelativeLossFunction(
						optimalParses.get(0).first());
				
				// Create the violating sets
				final List<Pair<Double, IParseResult<Y>>> violatingOptimalParses = new LinkedList<Pair<Double, IParseResult<Y>>>();
				final List<Pair<Double, IParseResult<Y>>> violatingNonOptimalParses = new LinkedList<Pair<Double, IParseResult<Y>>>();
				
				// These flags are used to mark that we inserted a parse into
				// the violating sets, so no need to check for its violation
				// against others
				final boolean[] optimalParsesFlags = new boolean[optimalParses
						.size()];
				final boolean[] nonOptimalParsesFlags = new boolean[nonOptimalParses
						.size()];
				int optimalParsesCounter = 0;
				for (final Pair<Double, IParseResult<Y>> optimalParse : optimalParses) {
					int nonOptimalParsesCounter = 0;
					for (final Pair<Double, IParseResult<Y>> nonOptimalParse : nonOptimalParses) {
						if (!optimalParsesFlags[optimalParsesCounter]
								|| !nonOptimalParsesFlags[nonOptimalParsesCounter]) {
							// Create the delta vector if needed, we do it only
							// once. This is why we check if we are going to
							// need it in the above 'if'.
							final IHashVector featureDelta = optimalParse
									.second()
									.getAverageMaxFeatureVector()
									.addTimes(
											-1.0,
											nonOptimalParse
													.second()
													.getAverageMaxFeatureVector());
							final double deltaScore = featureDelta
									.vectorMultiply(model.getTheta());
							
							// Test optimal parse for insertion into violating
							// optimal parses
							if (!optimalParsesFlags[optimalParsesCounter]) {
								// Case this optimal sample is still not in the
								// violating set
								if (deltaScore < margin
										* deltaLossFunction
												.loss(nonOptimalParse.first())) {
									// Case of violation
									// Add to the violating set
									violatingOptimalParses.add(optimalParse);
									// Mark flag, so we won't test it again
									optimalParsesFlags[optimalParsesCounter] = true;
								}
							}
							
							// Test non-optimal parse for insertion into
							// violating non-optimal parses
							if (!nonOptimalParsesFlags[nonOptimalParsesCounter]) {
								// Case this non-optimal sample is still not in
								// the violating set
								if (deltaScore < margin
										* deltaLossFunction
												.loss(nonOptimalParse.first())) {
									// Case of violation
									// Add to the violating set
									violatingNonOptimalParses
											.add(nonOptimalParse);
									// Mark flag, so we won't test it again
									nonOptimalParsesFlags[nonOptimalParsesCounter] = true;
								}
							}
						}
						
						// Increase the counter, as we move to the next sample
						++nonOptimalParsesCounter;
					}
					// Increase the counter, as we move to the next sample
					++optimalParsesCounter;
				}
				
				LOG.info(
						"%d violating optimal parses, %d violating non optimal parses",
						violatingOptimalParses.size(),
						violatingNonOptimalParses.size());
				
				if (violatingOptimalParses.isEmpty()) {
					LOG.info("There are no violating optimal/non-optiomal parses -- skipping");
					continue;
				}
				
				LOG.info("Violating optimal parses: ");
				for (final Pair<Double, IParseResult<Y>> pair : violatingOptimalParses) {
					logParse(dataItem, pair.second(), pair.first(), true,
							dataItemModel);
				}
				
				LOG.info("Violating non-optimal parses: ");
				for (final Pair<Double, IParseResult<Y>> pair : violatingNonOptimalParses) {
					logParse(dataItem, pair.second(), pair.first(), false,
							dataItemModel);
				}
				
				// Create tau function
				final IUpdateWeightFunction<Y> tauFunction = new UniformWeightFunction<Y>(
						violatingOptimalParses.size(),
						violatingNonOptimalParses.size());
				
				// Create the parameter update
				final IHashVector update = HashVectorFactory.create();
				
				// Get the update for optimal violating samples
				for (final Pair<Double, IParseResult<Y>> pair : violatingOptimalParses) {
					pair.second()
							.getAverageMaxFeatureVector()
							.addTimesInto(tauFunction.evalOptimalParse(pair),
									update);
				}
				
				// Get the update for the non-optimal violating samples
				for (final Pair<Double, IParseResult<Y>> pair : violatingNonOptimalParses) {
					pair.second()
							.getAverageMaxFeatureVector()
							.addTimesInto(
									-1.0
											* tauFunction
													.evalNonOptimalParse(pair),
									update);
				}
				
				// Prune small entries from the update
				update.dropSmallEntries();
				
				// Update the parameters vector
				LOG.info("Update weight: %f", dataItem.quality());
				LOG.info("Update: %s", update);
				update.addTimesInto(dataItem.quality(), model.getTheta());
				stats.triggeredUpdate(itemCounter, iterationNumber);
				
				stats.processed(itemCounter, iterationNumber);
				LOG.info("Total sample handling time: %.4fsec",
						(System.currentTimeMillis() - startTime) / 1000.0);
			}
			
			LOG.info("Iteration stats:");
			LOG.info("%s", stats);
		}
	}
	
	/**
	 * Calculates the G_i (good parses) and B_i (bad parses) sets. All theg good
	 * parses will have a relative loss of zero. Meaning, the all have the
	 * minimum loss relatively to the entire given set.
	 * 
	 * @param dataItem
	 * @param parseResults
	 * @return Pair of (good parses, bad parses)
	 */
	private Pair<List<Pair<Double, IParseResult<Y>>>, List<Pair<Double, IParseResult<Y>>>> createOptimalNonOptimalSets(
			ILossDataItem<Sentence, Y> dataItem,
			Collection<IParseResult<Y>> parseResults) {
		double minLoss = Double.MAX_VALUE;
		final List<Pair<Double, IParseResult<Y>>> optimalParses = new LinkedList<Pair<Double, IParseResult<Y>>>();
		final List<Pair<Double, IParseResult<Y>>> nonOptimalParses = new LinkedList<Pair<Double, IParseResult<Y>>>();
		for (final IParseResult<Y> parseResult : parseResults) {
			
			// Calculate the loss, accumulated from all given loss functions
			final double parseLoss = dataItem.calculateLoss(parseResult.getY());
			
			if (parseLoss < minLoss) {
				minLoss = parseLoss;
				nonOptimalParses.addAll(optimalParses);
				optimalParses.clear();
				optimalParses.add(Pair.of(parseLoss, parseResult));
			} else if (parseLoss == minLoss) {
				optimalParses.add(Pair.of(parseLoss, parseResult));
			} else {
				nonOptimalParses.add(Pair.of(parseLoss, parseResult));
			}
		}
		return Pair.of(optimalParses, nonOptimalParses);
	}
	
	private boolean isGoldDebugCorrect(Sentence sentence, Y label) {
		if (trainingDataDebug.containsKey(sentence)) {
			return trainingDataDebug.get(sentence).equals(label);
		} else {
			return false;
		}
	}
	
	private void logParse(IDataItem<Sentence> dataItem, IParseResult<Y> parse,
			Double loss, boolean logLexicalItems,
			IDataItemModel<Y> dataItemModel) {
		logParse(dataItem, parse, loss, logLexicalItems, null, dataItemModel);
	}
	
	private void logParse(IDataItem<Sentence> dataItem, IParseResult<Y> parse,
			Double loss, boolean logLexicalItems, String tag,
			IDataItemModel<Y> dataItemModel) {
		final boolean isGold;
		if (isGoldDebugCorrect(dataItem.getSample(), parse.getY())) {
			isGold = true;
		} else {
			isGold = false;
		}
		LOG.info("%s%s[S%.2f%s] %s", isGold ? "* " : "  ", tag == null ? ""
				: tag + " ", parse.getScore(),
				loss == null ? "" : String.format(", L%.2f", loss), parse);
		if (logLexicalItems) {
			for (final LexicalEntry<Y> entry : parse.getMaxLexicalEntries()) {
				LOG.info("\t[%f] %s", dataItemModel.score(entry), entry);
			}
		}
	}
	
	/**
	 * Builder for {@link LossSensitivePerceptronCKY}.
	 * 
	 * @author Yoav Artzi
	 */
	public static class Builder<Y> {
		private final IFilter<Category<Y>>														completeParseFilter							= new SimpleFullParseFilter<Y>(
																																					SetUtils.createSingleton((Syntax) Syntax.S));
		
		/**
		 * Used to break ties during lexical generation parse.
		 */
		private IScoreFunction<Y>																lexicalGenerationSecondaryPruningFunction	= new IScoreFunction<Y>() {
																																				
																																				@Override
																																				public double score(
																																						Y label) {
																																					return 0;
																																				};
																																				
																																				@Override
																																				public String toString() {
																																					return "DEFAULT_STUB";
																																				}
																																			};
		
		/**
		 * Beam size to use when doing loss sensitive pruning with generated
		 * lexicon.
		 */
		private int																				lexiconGenerationBeamSize					= 20;
		
		/**
		 * Validator to validate lexical generation parses.
		 */
		private IValidator<Y>																	lexiconGenerationValidator					= new IValidator<Y>() {
																																				
																																				@Override
																																				public boolean isValid(
																																						IDataItem<Sentence> dataItem,
																																						Y label) {
																																					return true;
																																				};
																																				
																																				@Override
																																				public String toString() {
																																					return "STUB_DEFAULT";
																																				}
																																			};
		
		/** Margin to scale the relative loss function */
		private double																			margin										= 1.0;
		
		/**
		 * Max sentence length. Sentence longer than this value will be skipped
		 * during training
		 */
		private int																				maxSentenceLength							= 50;
		
		/** Number of training iterations */
		private int																				numTrainingIterations						= 4;
		
		private final AbstractCKYParser<Y>														parser;
		
		/** Data used for training */
		private final IDataCollection<? extends ILexicalGenerationLossDataItem<Sentence, Y, Y>>	trainingData;
		
		/**
		 * Mapping a subset of training samples into their gold label for debug.
		 */
		private Map<Sentence, Y>																trainingDataDebug							= new HashMap<Sentence, Y>();
		
		public Builder(
				IDataCollection<? extends ILexicalGenerationLossDataItem<Sentence, Y, Y>> trainingData,
				AbstractCKYParser<Y> parser) {
			this.trainingData = trainingData;
			this.parser = parser;
		}
		
		public LossSensitivePerceptronCKY<Y> build() {
			return new LossSensitivePerceptronCKY<Y>(numTrainingIterations,
					margin, trainingData, trainingDataDebug, maxSentenceLength,
					lexiconGenerationBeamSize,
					lexicalGenerationSecondaryPruningFunction,
					lexiconGenerationValidator, parser, completeParseFilter);
		}
		
		public Builder<Y> setLexicalGenerationSecondaryPruningFunction(
				IScoreFunction<Y> lexicalGenerationSecondaryPruningFunction) {
			this.lexicalGenerationSecondaryPruningFunction = lexicalGenerationSecondaryPruningFunction;
			return this;
		}
		
		public Builder<Y> setLexiconGenerationBeamSize(
				int lexiconGenerationBeamSize) {
			this.lexiconGenerationBeamSize = lexiconGenerationBeamSize;
			return this;
		}
		
		public Builder<Y> setLexiconGenerationValidator(
				IValidator<Y> lexiconGenerationValidator) {
			this.lexiconGenerationValidator = lexiconGenerationValidator;
			return this;
		}
		
		public Builder<Y> setMargin(double margin) {
			this.margin = margin;
			return this;
		}
		
		public Builder<Y> setMaxSentenceLength(int maxSentenceLength) {
			this.maxSentenceLength = maxSentenceLength;
			return this;
		}
		
		public Builder<Y> setNumTrainingIterations(int numTrainingIterations) {
			this.numTrainingIterations = numTrainingIterations;
			return this;
		}
		
		public Builder<Y> setTrainingDataDebug(
				Map<Sentence, Y> trainingDataDebug) {
			this.trainingDataDebug = trainingDataDebug;
			return this;
		}
	}
	
	/**
	 * A relative loss function as given in the paper with the notation
	 * \delta_i. It's always non-negative and scores all the "good" parse
	 * results with zero, while all the rest have a score that is bigger than
	 * one.
	 * 
	 * @author Yoav Artzi
	 * @param <Y>
	 *            Parser representation.
	 */
	public static class RelativeLossFunction {
		final double	minLoss;
		
		public RelativeLossFunction(double minLoss) {
			this.minLoss = minLoss;
		}
		
		public double loss(double absLoss) {
			return absLoss - minLoss;
		}
	}
	
}
