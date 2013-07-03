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
package edu.uw.cs.lil.tiny.learn.ubl.splitting;

import java.util.HashSet;
import java.util.Set;

import edu.uw.cs.lil.tiny.ccg.categories.Category;
import edu.uw.cs.lil.tiny.ccg.categories.ICategoryServices;
import edu.uw.cs.lil.tiny.learn.ubl.splitting.SplittingServices.SplittingPair;
import edu.uw.cs.lil.tiny.mr.lambda.LogicalExpression;

/**
 * Splitting service object.
 * 
 * @author Yoav Artzi
 */
public class UnconstrainedSplitter implements IUBLSplitter {
	
	private final ICategoryServices<LogicalExpression>	categoryServices;
	
	public UnconstrainedSplitter(ICategoryServices<LogicalExpression> categoryServices) {
		this.categoryServices = categoryServices;
	}
	
	public Set<SplittingPair> getSplits(Category<LogicalExpression> category) {
		final Set<SplittingPair> splits = new HashSet<SplittingPair>();
		splits.addAll(MakeApplicationSplits.of(category, categoryServices));
		splits.addAll(MakeCompositionSplits.of(category, categoryServices));
		return splits;
	}
}
