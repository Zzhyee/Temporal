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
package edu.uw.cs.lil.learn.simple.joint;

import java.util.LinkedList;
import java.util.List;

import edu.uw.cs.lil.tiny.data.IDataCollection;
import edu.uw.cs.lil.tiny.data.IDataItem;
import edu.uw.cs.lil.tiny.data.ILabeledDataItem;
import edu.uw.cs.lil.tiny.learn.ILearner;
import edu.uw.cs.lil.tiny.parser.joint.IJointOutput;
import edu.uw.cs.lil.tiny.parser.joint.IJointParse;
import edu.uw.cs.lil.tiny.parser.joint.IJointParser;
import edu.uw.cs.lil.tiny.parser.joint.model.JointModel;
import edu.uw.cs.lil.tiny.utils.hashvector.HashVectorFactory;
import edu.uw.cs.lil.tiny.utils.hashvector.IHashVector;
import edu.uw.cs.utils.composites.Pair;
import edu.uw.cs.utils.log.ILogger;
import edu.uw.cs.utils.log.LoggerFactory;

/**
 * Joint perceptron learner for parameter update only.
 * 
 * @author Yoav Artzi
 * @param <LANG>
 * @param <ERESULT>
 */
public class JointSimplePerceptron<LANG extends IDataItem<LANG>, STATE, LF, ESTEP, ERESULT>
		implements ILearner<LANG, LF, JointModel<LANG, STATE, LF, ESTEP>> {
	private static final ILogger															LOG	= LoggerFactory
																										.create(JointSimplePerceptron.class);
	
	private final int																		numIterations;
	private final IJointParser<LANG, STATE, LF, ESTEP, ERESULT>								parser;
	private final IDataCollection<? extends ILabeledDataItem<Pair<LANG, STATE>, ERESULT>>	trainingData;
	
	public JointSimplePerceptron(
			int numIterations,
			IDataCollection<? extends ILabeledDataItem<Pair<LANG, STATE>, ERESULT>> trainingData,
			IJointParser<LANG, STATE, LF, ESTEP, ERESULT> parser) {
		this.numIterations = numIterations;
		this.trainingData = trainingData;
		this.parser = parser;
	}
	
	@Override
	public void train(JointModel<LANG, STATE, LF, ESTEP> model) {
		for (int iterationNumber = 0; iterationNumber < numIterations; ++iterationNumber) {
			// Training iteration, go over all training samples
			LOG.info("=========================");
			LOG.info("Training iteration %d", iterationNumber);
			LOG.info("=========================");
			int itemCounter = -1;
			
			for (final ILabeledDataItem<Pair<LANG, STATE>, ERESULT> dataItem : trainingData) {
				final long startTime = System.currentTimeMillis();
				
				LOG.info("%d : ================== [%d]", ++itemCounter,
						iterationNumber);
				LOG.info("Sample type: %s", dataItem.getClass().getSimpleName());
				LOG.info("%s", dataItem);
				
				final IJointOutput<LF, ERESULT> parserOutput = parser.parse(
						dataItem, model.createJointDataItemModel(dataItem));
				final List<? extends IJointParse<LF, ERESULT>> bestParses = parserOutput
						.getBestJointParses();
				
				// Best correct parses
				final List<IJointParse<LF, ERESULT>> correctParses = parserOutput
						.getBestParsesForZ(dataItem.getLabel());
				
				// Violating parses
				final List<IJointParse<LF, ERESULT>> violatingBadParses = new LinkedList<IJointParse<LF, ERESULT>>();
				for (final IJointParse<LF, ERESULT> parse : bestParses) {
					if (!dataItem.getLabel().equals(parse.getResult().second())) {
						violatingBadParses.add(parse);
						LOG.info("Bad parse: %s", parse.getY());
					}
				}
				
				if (!violatingBadParses.isEmpty() && !correctParses.isEmpty()) {
					// Case we have bad best parses and a correct parse, need to
					// update.
					
					// Create the parameter update
					final IHashVector update = HashVectorFactory.create();
					
					// Positive update
					for (final IJointParse<LF, ERESULT> parse : correctParses) {
						parse.getAverageMaxFeatureVector().addTimesInto(
								(1.0 / correctParses.size()), update);
					}
					
					// Negative update
					for (final IJointParse<LF, ERESULT> parse : violatingBadParses) {
						parse.getAverageMaxFeatureVector().addTimesInto(
								-1.0 * (1.0 / violatingBadParses.size()),
								update);
					}
					
					// Prune small entries from the update
					update.dropSmallEntries();
					
					// Update the parameters vector
					LOG.info("Update: %s", update);
					update.addTimesInto(1.0, model.getTheta());
				} else if (correctParses.isEmpty()) {
					LOG.info("No correct parses. No update.");
				} else {
					LOG.info("Correct. No update.");
				}
				LOG.info("Sample processing time %.4f",
						(System.currentTimeMillis() - startTime) / 1000.0);
			}
		}
	}
	
}
