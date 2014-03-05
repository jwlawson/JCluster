/**
 * 
 */
package uk.co.jwlawson.jcluster.demos;

import java.util.Arrays;
import java.util.Scanner;

import org.apache.commons.lang3.math.NumberUtils;

import uk.co.jwlawson.jcluster.DynkinDiagram;
import uk.co.jwlawson.jcluster.PolynomialQuiver;

/**
 * @author John
 * 
 */
public class Mutator {

	private PolynomialQuiver mMatrix;

	public Mutator(String[] args) {
		mMatrix = parseArgs(args);
	}

	/**
	 * Validate that the supplied arguments represent a matrix.
	 * 
	 * @param args Supplied arguments
	 * @return true if the args are valid
	 */
	public static boolean isValidArgs(String[] args) {
		if (args.length == 1) {
			return isDynkin(args[0]);
		} else if (args.length > 2 && isAllInts(args)) {
			return isValidMatrix(args);
		} else {
			return false;
		}
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

	/** Returns whether all elements in the array can be parsed as ints. */
	private static boolean isAllInts(String[] arr) {
		boolean result = true;
		for (int i = 0; i < arr.length; i++) {
			result = result && isInt(arr[i]);
		}
		return result;
	}

	/** Returns whether the string can be parsed to an integer. */
	private static boolean isInt(String arg) {
		int r = NumberUtils.toInt(arg, Integer.MAX_VALUE);
		return r != Integer.MAX_VALUE;
	}

	/**
	 * Check whether the array contains the right number of elements to define a
	 * matrix.
	 */
	private static boolean isValidMatrix(String[] args) {
		int rows = NumberUtils.toInt(args[0]);
		int cols = NumberUtils.toInt(args[1]);
		return (rows * cols == args.length - 2);
	}

	/**
	 * Parse the arguments and return the QuiverMatrix defined by them.
	 * 
	 * @param args The arguments to parse
	 * @return The QuiverMatrix defined by the args
	 */
	private PolynomialQuiver parseArgs(String[] args) {
		PolynomialQuiver result;
		if (args.length == 1) {
			String matrix = args[0];
			DynkinDiagram d = getDynkinDiagram(matrix);
			result = new PolynomialQuiver(d.getMatrix());
		} else {
			int[] intArr = toIntArray(args);
			result = getMatrix(intArr);
		}
		return result;
	}

	/** Get the Dynkin diagram identified by the string. */
	private DynkinDiagram getDynkinDiagram(String matrix) {
		try {
			return DynkinDiagram.valueOf(matrix);
		} catch (IllegalArgumentException e) {
			// Should never happen if isDynkin is checked first
			throw new RuntimeException("No Dynkin diagram named " + matrix
					+ " is available.");
		}
	}

	/** Convert a string array to an int array. */
	private int[] toIntArray(String[] arr) {
		int[] result = new int[arr.length];
		for (int i = 0; i < arr.length; i++) {
			result[i] = NumberUtils.toInt(arr[i]);
		}
		return result;
	}

	/**
	 * Get the QuiverMatrix defined in the array.
	 * The array should be of the form {@code rows, cols, data... }
	 * 
	 * @param arr The array containing the matrix data.
	 * @return The QuiverMatrix defined by the array.
	 */
	private PolynomialQuiver getMatrix(int[] arr) {
		int rows = arr[0];
		int cols = arr[1];
		int[] values = Arrays.copyOfRange(arr, 2, arr.length);
		return new PolynomialQuiver(rows, cols, values);
	}

	/**
	 * Mutate the matrix at vertex k.
	 * 
	 * @param k The vertex to mutate at
	 */
	public void mutate(int k) {
		mMatrix = (PolynomialQuiver) mMatrix.mutate(k);
	}

	/**
	 * Print the matrix to System.out
	 */
	public void printMatrix() {
		String str = mMatrix.toString();
		// Remove the first line as it only contains technical info
		String[] arr = str.split(System.lineSeparator());
		for (int i = 1; i < arr.length; i++) {
			System.out.println(arr[i]);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out
				.println("Mutator demo from jCluster  Copyright (C) 2014 John Lawson");
		System.out
				.println("This program comes with ABSOLUTELY NO WARRANTY. This is free");
		System.out
				.println("software, and you are welcome to redistribute it under certain");
		System.out.println("conditions.");
		if (Mutator.isValidArgs(args)) {
			Mutator mut = new Mutator(args);
			Scanner scan = new Scanner(System.in);
			System.out.println("Initial matrix:");
			mut.printMatrix();
			while (true) {
				System.out.print("Enter vertex to mutate at or q to quit: ");
				if (scan.hasNextInt()) {
					int k = scan.nextInt();
					mut.mutate(k);
					mut.printMatrix();
					System.out.println();
				} else {
					break;
				}
			}
			scan.close();
		} else {
			System.out.println("Usage:");
			System.out.println("    Mutator <diagram>");
			System.out
					.println("    Where diagram is the name of a Dynkin diagram");
			System.out.println();
			System.out.println("    e.g. Mutator A6");
		}
	}

}
