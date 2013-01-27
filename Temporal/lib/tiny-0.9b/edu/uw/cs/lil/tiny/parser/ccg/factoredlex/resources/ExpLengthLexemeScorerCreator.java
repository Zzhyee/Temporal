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
package edu.uw.cs.lil.tiny.parser.ccg.factoredlex.resources;

import edu.uw.cs.lil.tiny.explat.IResourceRepository;
import edu.uw.cs.lil.tiny.explat.ParameterizedExperiment.Parameters;
import edu.uw.cs.lil.tiny.explat.resources.IResourceObjectCreator;
import edu.uw.cs.lil.tiny.parser.ccg.factoredlex.features.scorers.ExpLengthLexemeScorer;
import edu.uw.cs.lil.tiny.parser.ccg.features.basic.scorer.ExpLengthLexicalEntryScorer;

/**
 * Creator for {@link ExpLengthLexicalEntryScorer}.
 * 
 * @author Luke Zettlemoyer
 */
public class ExpLengthLexemeScorerCreator implements
		IResourceObjectCreator<ExpLengthLexemeScorer> {
	
	@Override
	public ExpLengthLexemeScorer create(Parameters parameters,
			IResourceRepository resourceRepo) {
		final Double base = Double.valueOf(parameters.get("base"));
		final Double exp = Double.valueOf(parameters.get("exponent"));
		return new ExpLengthLexemeScorer(base, exp);
	}
	
	@Override
	public String resourceTypeName() {
		return "scorer.lexeme.explength";
	}
	
}
