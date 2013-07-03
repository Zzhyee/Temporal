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
package edu.uw.cs.lil.tiny.parser.joint.model;

import edu.uw.cs.lil.tiny.data.IDataItem;
import edu.uw.cs.utils.composites.Pair;

public class JointDataItemWrapper<X, W> implements IDataItem<X> {
	
	private final IDataItem<Pair<X, W>>	baseDataItem;
	private final X						sample;
	
	public JointDataItemWrapper(X sample, IDataItem<Pair<X, W>> baseDataItem) {
		this.sample = sample;
		this.baseDataItem = baseDataItem;
	}
	
	public IDataItem<Pair<X, W>> getBaseDataItem() {
		return baseDataItem;
	}
	
	@Override
	public X getSample() {
		return sample;
	}
	
}
