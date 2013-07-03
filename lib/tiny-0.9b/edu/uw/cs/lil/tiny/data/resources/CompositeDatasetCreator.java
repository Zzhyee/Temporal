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
package edu.uw.cs.lil.tiny.data.resources;

import edu.uw.cs.lil.tiny.data.IDataCollection;
import edu.uw.cs.lil.tiny.data.composite.CompositeDataset;
import edu.uw.cs.lil.tiny.explat.IResourceRepository;
import edu.uw.cs.lil.tiny.explat.ParameterizedExperiment.Parameters;
import edu.uw.cs.lil.tiny.explat.resources.IResourceObjectCreator;
import edu.uw.cs.utils.collections.ListUtils;

public class CompositeDatasetCreator<T> implements
		IResourceObjectCreator<CompositeDataset<T>> {
	private static final String	DEFAULT_NAME	= "data.composite";
	private final String		resourceName;
	
	public CompositeDatasetCreator() {
		this(DEFAULT_NAME);
	}
	
	public CompositeDatasetCreator(String resourceName) {
		this.resourceName = resourceName;
	}
	
	@Override
	public CompositeDataset<T> create(Parameters parameters,
			final IResourceRepository resourceRepo) {
		return new CompositeDataset<T>(ListUtils.map(
				parameters.getSplit("sets"),
				new ListUtils.Mapper<String, IDataCollection<? extends T>>() {
					
					@Override
					public IDataCollection<T> process(String obj) {
						return resourceRepo.getResource(obj);
					}
				}));
	}
	
	@Override
	public String resourceTypeName() {
		return resourceName;
	}
	
}
