/**
 * Copyright 2014 John Lawson
 * 
 * QuiverMatrixResourceLoader.java is part of JCluster. Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package uk.co.jwlawson.jcluster.pool.multiway;

import java.lang.reflect.Constructor;

import uk.co.jwlawson.jcluster.QuiverKey;
import uk.co.jwlawson.jcluster.QuiverMatrix;

import com.github.benmanes.multiway.ResourceLoader;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * @author John Lawson
 * 
 */
public class QuiverMatrixResourceLoader<T extends QuiverMatrix> implements
		ResourceLoader<QuiverKey<T>, T> {

	private final LoadingCache<QuiverKey<T>, Constructor<T>> constructorCache;

	public QuiverMatrixResourceLoader() {
		constructorCache = getConstructorCache();
	}

	private LoadingCache<QuiverKey<T>, Constructor<T>> getConstructorCache() {
		return CacheBuilder.newBuilder().maximumSize(10)
				.build(new CacheLoader<QuiverKey<T>, Constructor<T>>() {

					@Override
					public Constructor<T> load(QuiverKey<T> key) throws Exception {
						Class<T> clazz = key.getClassObject();
						Constructor<T> constructor;
						try {
							constructor = clazz.getConstructor(Integer.TYPE, Integer.TYPE);
						} catch (NoSuchMethodException e) {
							throw new RuntimeException("No constructor found for class " + clazz, e);
						} catch (SecurityException e) {
							throw new RuntimeException("Cannot access constructor for class " + clazz, e);
						}
						return constructor;
					}
				});
	}

	public T load(QuiverKey<T> key) throws Exception {
		Constructor<T> cons = constructorCache.get(key);
		return cons.newInstance(key.getRows(), key.getCols());
	}

}
