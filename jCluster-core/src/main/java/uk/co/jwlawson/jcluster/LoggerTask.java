/**
 * Copyright 2014 John Lawson
 * 
 * LoggerTask.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.jwlawson.jcluster.data.MatrixInfo;
import uk.co.jwlawson.jcluster.data.QuiverMatrix;

/**
 * Simple task which logs the matrix provided when run.
 * 
 * <p>
 * The matrix is logged with a logger named after this class. Use the logback settings to choose
 * where the log should go.
 * 
 * e.g.
 * 
 * <pre>
 * {@code
 * <appender name="OUT_FILE" class="ch.qos.logback.core.FileAppender">
 * 	<file>out.log</file>
 * 	<append>true</append>
 * 	<encoder>
 * 		<pattern>[%thread] %-5level %logger - %msg%n</pattern>
 * 	</encoder>
 * </appender>
 * 
 * <logger name="uk.co.jwlawson.jcluster.LoggerTask" level="INFO" additivity="false">
 * 	<appender-ref ref="OUT_FILE"/>
 * </logger>
 *  }
 * </pre>
 * 
 * @author John Lawson
 * @param <T> Type of quiver to log
 * 
 */
public class LoggerTask<T extends QuiverMatrix> implements MatrixTask<T> {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private T matrix;

	@Override
	public void setMatrix(T matrix) {
		this.matrix = matrix;
	}

	@Override
	public void reset() {}

	@Override
	public void requestStop() {}

	@Override
	public MatrixInfo call() throws Exception {
		log.info(matrix.toString());
		return new MatrixInfo(matrix);
	}

}
