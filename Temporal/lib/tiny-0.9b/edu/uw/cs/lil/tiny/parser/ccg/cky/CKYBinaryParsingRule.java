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
package edu.uw.cs.lil.tiny.parser.ccg.cky;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import edu.uw.cs.lil.tiny.parser.ccg.cky.chart.AbstractCellFactory;
import edu.uw.cs.lil.tiny.parser.ccg.cky.chart.Cell;
import edu.uw.cs.lil.tiny.parser.ccg.rules.IBinaryParseRule;
import edu.uw.cs.lil.tiny.parser.ccg.rules.ParseRuleResult;

public class CKYBinaryParsingRule<Y> {
	private final IBinaryParseRule<Y>	ccgParseRule;
	
	public CKYBinaryParsingRule(IBinaryParseRule<Y> ccgParseRule) {
		this.ccgParseRule = ccgParseRule;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		@SuppressWarnings("rawtypes")
		final CKYBinaryParsingRule other = (CKYBinaryParsingRule) obj;
		if (ccgParseRule == null) {
			if (other.ccgParseRule != null) {
				return false;
			}
		} else if (!ccgParseRule.equals(other.ccgParseRule)) {
			return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((ccgParseRule == null) ? 0 : ccgParseRule.hashCode());
		return result;
	}
	
	/**
	 * Takes two cell, left and right, as input. Assumes these cells are
	 * adjacent. Adds any new cells it can produce to the result list.
	 */
	protected List<Cell<Y>> newCellsFrom(Cell<Y> left, Cell<Y> right,
			AbstractCellFactory<Y> cellFactory, boolean isCompleteSentence) {
		final Collection<ParseRuleResult<Y>> results = ccgParseRule.apply(
				left.getCategroy(), right.getCategroy(), isCompleteSentence);
		final List<Cell<Y>> result = new LinkedList<Cell<Y>>();
		for (final ParseRuleResult<Y> ruleResult : results) {
			result.add(cellFactory.create(ruleResult.getResultCategory(), left,
					right, ruleResult.getRuleName()));
		}
		return result;
	}
}
