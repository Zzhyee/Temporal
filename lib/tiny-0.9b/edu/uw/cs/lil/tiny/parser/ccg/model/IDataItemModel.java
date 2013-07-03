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
package edu.uw.cs.lil.tiny.parser.ccg.model;

import edu.uw.cs.lil.tiny.parser.ccg.IParseStep;
import edu.uw.cs.lil.tiny.parser.ccg.lexicon.ILexiconImmutable;
import edu.uw.cs.lil.tiny.parser.ccg.lexicon.LexicalEntry;
import edu.uw.cs.lil.tiny.utils.hashvector.IHashVector;
import edu.uw.cs.lil.tiny.utils.hashvector.IHashVectorImmutable;

public interface IDataItemModel<Y> {
	
	IHashVector computeFeatures(IParseStep<Y> parseStep);
	
	IHashVector computeFeatures(IParseStep<Y> parseStep, IHashVector features);
	
	IHashVector computeFeatures(LexicalEntry<Y> lexicalEntry);
	
	IHashVector computeFeatures(LexicalEntry<Y> lexicalEntry,
			IHashVector features);
	
	ILexiconImmutable<Y> getLexicon();
	
	IHashVectorImmutable getTheta();
	
	double score(IParseStep<Y> parseStep);
	
	double score(LexicalEntry<Y> entry);
	
}
