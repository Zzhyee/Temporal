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
package edu.uw.cs.lil.tiny.learn.ubl.resources;

import edu.uw.cs.lil.tiny.ccg.categories.ICategoryServices;
import edu.uw.cs.lil.tiny.data.IDataCollection;
import edu.uw.cs.lil.tiny.data.ILabeledDataItem;
import edu.uw.cs.lil.tiny.data.sentence.Sentence;
import edu.uw.cs.lil.tiny.explat.IResourceRepository;
import edu.uw.cs.lil.tiny.explat.ParameterizedExperiment.Parameters;
import edu.uw.cs.lil.tiny.learn.ubl.LexicalSplittingCountScorer;
import edu.uw.cs.lil.tiny.learn.ubl.splitting.IUBLSplitter;
import edu.uw.cs.lil.tiny.mr.lambda.LogicalExpression;
import edu.uw.cs.lil.tiny.parser.ccg.factoredlex.Lexeme;
import edu.uw.cs.lil.tiny.parser.ccg.features.basic.resources.AbstractScaledScorerCreator;

public class LexicalSplittingCountScorerCreator extends
		AbstractScaledScorerCreator<Lexeme, LexicalSplittingCountScorer> {
	
	@SuppressWarnings("unchecked")
	@Override
	public LexicalSplittingCountScorer createScorer(Parameters parameters,
			IResourceRepository resourceRepo) {
		return new LexicalSplittingCountScorer.Builder(
				(IUBLSplitter) resourceRepo.getResource(parameters
						.get("splitter")),
				(IDataCollection<? extends ILabeledDataItem<Sentence, LogicalExpression>>) resourceRepo
						.getResource(parameters.get("data")),
				(ICategoryServices<LogicalExpression>) resourceRepo
						.getResource("categoryServices")).build();
	}
	
	@Override
	public String resourceTypeName() {
		return "scorer.lexeme.allsplits";
	}
	
}