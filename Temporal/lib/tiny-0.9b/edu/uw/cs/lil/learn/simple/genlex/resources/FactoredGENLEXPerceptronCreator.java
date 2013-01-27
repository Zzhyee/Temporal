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
package edu.uw.cs.lil.learn.simple.genlex.resources;

import edu.uw.cs.lil.learn.simple.genlex.FactoredGENLEXPerceptron;
import edu.uw.cs.lil.tiny.data.IDataCollection;
import edu.uw.cs.lil.tiny.data.ILabeledDataItem;
import edu.uw.cs.lil.tiny.data.sentence.Sentence;
import edu.uw.cs.lil.tiny.explat.IResourceRepository;
import edu.uw.cs.lil.tiny.explat.ParameterizedExperiment.Parameters;
import edu.uw.cs.lil.tiny.explat.resources.IResourceObjectCreator;
import edu.uw.cs.lil.tiny.mr.lambda.LogicalExpression;
import edu.uw.cs.lil.tiny.parser.IParser;
import edu.uw.cs.lil.tiny.test.Tester;

public class FactoredGENLEXPerceptronCreator implements
		IResourceObjectCreator<FactoredGENLEXPerceptron> {
	
	private static final String	DEFAULT_NAME	= "learn.simple.genlex";
	private final String		resourceName;
	
	public FactoredGENLEXPerceptronCreator() {
		this(DEFAULT_NAME);
	}
	
	public FactoredGENLEXPerceptronCreator(String resourceName) {
		this.resourceName = resourceName;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public FactoredGENLEXPerceptron create(Parameters params,
			IResourceRepository repo) {
		
		return new FactoredGENLEXPerceptron(
				Integer.parseInt(params.get("iter")),
				Integer.parseInt(params.get("trainingMaxSentenceLength")),
				(Tester<Sentence, LogicalExpression>) repo.getResource(params
						.get("tester")),
				(IDataCollection<? extends ILabeledDataItem<Sentence, LogicalExpression>>) repo
						.getResource(params.get("data")),
				(IParser<Sentence, LogicalExpression>) repo
						.getResource("parser"));
	}
	
	@Override
	public String resourceTypeName() {
		return resourceName;
	}
	
}
