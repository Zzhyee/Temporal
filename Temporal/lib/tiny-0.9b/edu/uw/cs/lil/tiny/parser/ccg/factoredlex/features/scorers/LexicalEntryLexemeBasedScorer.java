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
package edu.uw.cs.lil.tiny.parser.ccg.factoredlex.features.scorers;

import edu.uw.cs.lil.tiny.mr.lambda.LogicalExpression;
import edu.uw.cs.lil.tiny.parser.ccg.factoredlex.FactoredLexicon;
import edu.uw.cs.lil.tiny.parser.ccg.factoredlex.Lexeme;
import edu.uw.cs.lil.tiny.parser.ccg.lexicon.LexicalEntry;
import edu.uw.cs.utils.collections.IScorer;

public class LexicalEntryLexemeBasedScorer implements
		IScorer<LexicalEntry<LogicalExpression>> {
	
	private final IScorer<Lexeme>	lexemeScorer;
	
	public LexicalEntryLexemeBasedScorer(IScorer<Lexeme> lexemeScorer) {
		this.lexemeScorer = lexemeScorer;
	}
	
	@Override
	public double score(LexicalEntry<LogicalExpression> e) {
		return lexemeScorer.score(FactoredLexicon.factor(e).getLexeme());
	}
	
}
