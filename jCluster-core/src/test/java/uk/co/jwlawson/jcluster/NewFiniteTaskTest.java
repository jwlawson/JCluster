/**
 * 
 */
package uk.co.jwlawson.jcluster;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author John
 *
 */
public class NewFiniteTaskTest {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Test
	public void test() {
		log.debug("Starting test");
		NewFiniteTask task = new NewFiniteTask(DynkinDiagram.A7.getMatrix());
		ExecutorService exec = Executors.newSingleThreadExecutor();

		Future<QuiverMatrix> future = exec.submit(task);
		try {
			future.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

	}
}
