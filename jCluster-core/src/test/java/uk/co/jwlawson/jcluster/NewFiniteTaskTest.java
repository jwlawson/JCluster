/**
 * 
 */
package uk.co.jwlawson.jcluster;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;
import org.junit.Ignore;
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
		NewFiniteTask task = new NewFiniteTask(DynkinDiagram.A4.getMatrix());
		ExecutorService exec = Executors.newSingleThreadExecutor();

		Future<Integer> future = exec.submit(task);
		try {
			int value = future.get();
			assertEquals(144, value);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	@Ignore("Takes ages and not really a test")
	@Test
	public void testAllSmallMatrices() {
		Executor exec = Executors.newSingleThreadExecutor();
		CompletionService<Integer> pool = new
				ExecutorCompletionService<Integer>(exec);
		for(DynkinDiagram d : DynkinDiagram.TEST_SET){
			NewFiniteTask task = new NewFiniteTask(d.getMatrix());
			pool.submit(task);
		}
		for(DynkinDiagram d : DynkinDiagram.TEST_SET) {
			try{
				int value = pool.take().get();
				log.info("Found {} quivers in class of {}", 
						value,
						d.toString());
			} catch (ExecutionException e){
			} catch (InterruptedException e){
			}
		}
	}
}
