/**
 * 
 */
package uk.co.jwlawson.jcluster.demos;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.jwlawson.jcluster.LoggerTaskFactory;
import uk.co.jwlawson.jcluster.MatrixInfoResultHandler;
import uk.co.jwlawson.jcluster.MinMutInfExt;
import uk.co.jwlawson.jcluster.data.DynkinDiagram;
import uk.co.jwlawson.jcluster.data.EquivQuiverMatrix;
import uk.co.jwlawson.jcluster.data.MatrixInfo;
import uk.co.jwlawson.jcluster.data.QuiverMatrix;

/**
 * @author John Lawson
 * 
 */
public class LogMinMutInfExt {

	private final static Logger log = LoggerFactory.getLogger(LogMinMutInfExt.class);

	private final MinMutInfExt task;

	public LogMinMutInfExt(String matrix) {
		QuiverMatrix mat = getDynkinDiagram(matrix).getMatrix();
		task =
				MinMutInfExt.Builder.builder().withInitial(new EquivQuiverMatrix(mat))
						.addTaskFactory(new LoggerTaskFactory<QuiverMatrix>())
						.withResultHandler(new ResultHandler())
						.withMinMutResultHandler(new ResultHandler()).build();
	}

	public void run() {
		ExecutorService exec = Executors.newSingleThreadExecutor();
		Future<MatrixInfo> future = exec.submit(task);

		try {
			future.get();
		} catch (InterruptedException e) {
			log.error("Caught interrupt in thread {}", Thread.currentThread().getName());
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			log.error("Execution error", e);
			throw new RuntimeException(e);
		} finally {
			exec.shutdown();
		}
	}

	/** Check that the arguments supplied are correct. */
	public static boolean isValidArgs(String[] args) {
		if (args.length != 1) {
			return false;
		}
		return isDynkin(args[0]);
	}

	/** Check whether the string identifies a valid Dynkin Diagram. */
	private static boolean isDynkin(String matrix) {
		DynkinDiagram[] diagrams = DynkinDiagram.values();
		for (int i = 0; i < diagrams.length; i++) {
			if (diagrams[i].name().equalsIgnoreCase(matrix)) {
				return true;
			}
		}
		return false;
	}

	/** Get the Dynkin diagram identified by the string. */
	private DynkinDiagram getDynkinDiagram(String matrix) {
		try {
			return DynkinDiagram.valueOf(matrix);
		} catch (IllegalArgumentException e) {
			// Should never happen if isDynkin is checked first
			throw new RuntimeException("No Dynkin diagram named " + matrix + " is available.");
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (isValidArgs(args)) {
			log.info("Finding all minimally mutation infinite extensions of {}", args[0]);
			LogMinMutInfExt t = new LogMinMutInfExt(args[0]);
			t.run();
		} else {
			log.info("Arguments should be a dynkin diagram");
		}
	}

	/**
	 * Dummy result handler which does nothing with the results and just returns null at the end.
	 * 
	 * @author John Lawson
	 * 
	 */
	private class ResultHandler extends MatrixInfoResultHandler {

		public ResultHandler() {
			super(null);
		}

		@Override
		protected void handleResult(MatrixInfo matrix) {}

		@Override
		protected MatrixInfo getFinal() {
			return getInitial();
		}

	}

}
