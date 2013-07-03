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
package edu.uw.cs.lil.tiny.explat;

import java.io.PrintStream;
import java.util.Set;

import edu.uw.cs.utils.log.Log;
import edu.uw.cs.utils.log.thread.LoggingRunnable;

public abstract class Job extends LoggingRunnable {
	
	private boolean				completed	= false;
	private final Set<String>	dependencyIds;
	private final String		id;
	private final IJobListener	jobListener;
	private final PrintStream	outputStream;
	
	public Job(String id, Set<String> dependencyIds, IJobListener jobListener,
			PrintStream outputStream, Log log) {
		super(log);
		this.id = id;
		this.dependencyIds = dependencyIds;
		this.jobListener = jobListener;
		this.outputStream = outputStream;
	}
	
	public Set<String> getDependencyIds() {
		return dependencyIds;
	}
	
	public String getId() {
		return id;
	}
	
	public PrintStream getOutputStream() {
		return outputStream;
	}
	
	public boolean isCompleted() {
		return completed;
	}
	
	@Override
	public final void loggedRun() {
		// Do the actual job
		try {
			doJob();
		} catch (final Exception e) {
			jobListener.jobException(id, e);
			return;
		}
		
		// Mark job as completed
		completed = true;
		
		// Signal job completed
		jobListener.jobCompleted(id);
	}
	
	protected abstract void doJob();
}
