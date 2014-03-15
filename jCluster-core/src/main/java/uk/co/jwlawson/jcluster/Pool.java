/**
 * Copyright 2014 John Lawson
 * 
 * Pool.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package uk.co.jwlawson.jcluster;

/**
 * @author John Lawson
 * 
 */
public interface Pool<T> {

	/**
	 * Get an object from the pool. If no such object is already in the pool then one is created.
	 * 
	 * @return Pooled object
	 */
	public T getObj();

	/**
	 * Return an object to the pool.
	 * <p>
	 * This should only be used to return objects which were previously taken fro the pool.
	 * 
	 * @param obj Object to return
	 */
	public void returnObj(T obj);

}
