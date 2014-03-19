/**
 * Copyright 2014 John Lawson
 * 
 * package-info.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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
/**
 * Contains the interfaces and requirements for pooling objects used in JCluster.
 * 
 * <p>
 * There are a number of implementations provided and the standard way to access any pool is through
 * the {@link uk.co.jwlawson.jcluster.pool.Pools} class and its factory methods.
 * 
 * <p>
 * Although using pools to prevent garbage collection might have a small improvement in laonger
 * calculations and those with larger matrices it does not seem to make much of a difference using
 * the {@link uk.co.jwlawson.jcluster.pool.dummy.DummyPoolFactory} and its associated
 * implementations which don't provide any pooling, but just a nice way of getting new Generic
 * objects which is otherwise rather hard due to Java's type erasure.
 * 
 * @author John Lawson
 * 
 */
package uk.co.jwlawson.jcluster.pool;