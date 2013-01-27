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
import edu.uw.cs.lil.tiny.parser.ccg.factoredlex.Lexeme;
import edu.uw.cs.lil.tiny.parser.ccg.factoredlex.features.LexemeFeatureSet;
import edu.uw.cs.utils.collections.IScorer;

/**
 * Creator for {@link LexemeFeatureSet}.
 * 
 * @author Yoav Artzi
 */
public class LexemeFeatureSetCreator<X> implements
		IResourceObjectCreator<LexemeFeatureSet<X>> {
	
	@SuppressWarnings("unchecked")
	@Override
	public LexemeFeatureSet<X> create(Parameters parameters,
			IResourceRepository resourceRepo) {
		final LexemeFeatureSet.Builder<X> builder = new LexemeFeatureSet.Builder<X>();
		
		if (parameters.contains("scale")) {
			builder.setScale(Double.valueOf(parameters.get("scale")));
		}
		
		if (parameters.contains("tag")) {
			builder.setFeatureTag(parameters.get("tag"));
		}
		
		if (parameters.contains("initFixed")) {
			builder.setInitialFixedScorer((IScorer<Lexeme>) resourceRepo
					.getResource(parameters.get("initFixed")));
		}
		
		if (parameters.contains("init")) {
			builder.setInitialScorer((IScorer<Lexeme>) resourceRepo
					.getResource(parameters.get("init")));
		}
		
		return builder.build();
	}
	
	@Override
	public String resourceTypeName() {
		return "feat.lexeme";
	}
	
}
