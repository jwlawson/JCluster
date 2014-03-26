/**
 * Copyright 2014 John Lawson
 * 
 * NamingThreadFactory.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package uk.co.jwlawson.jcluster;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Pattern;

/**
 * @author John Lawson
 * 
 */
public class NamingThreadFactory implements ThreadFactory {

	private final ThreadFactory mFactory;
	private final String mName;
	private final Pattern mPattern = Pattern.compile("^pool-\\d+");

	/**
	 * Replaces {@code pool-N-thread-M} with {@code name-thread-M} as the name of each thread in this
	 * pool.
	 * 
	 * @param name your pool name
	 */
	public NamingThreadFactory(String name) {
		mName = name;
		mFactory = Executors.defaultThreadFactory();
	}

	@Override
	public Thread newThread(Runnable run) {
		Thread result = mFactory.newThread(run);
		result.setName(rename(result.getName()));
		return result;
	}

	protected String rename(String oldName) {
		return mPattern.matcher(oldName).replaceAll(mName);
	}

}
