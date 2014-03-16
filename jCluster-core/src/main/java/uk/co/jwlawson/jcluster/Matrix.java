package uk.co.jwlawson.jcluster;

/**
 * 
 * @author John Lawson
 * 
 * @param <T>
 */
public interface Matrix<T extends Number> {

	/**
	 * Create a copy of this matrix which is then completely independent of this.
	 * 
	 * @return A copy of this matrix.
	 */
	Matrix<T> copyMatrix();

	/**
	 * Set all values in this matrix. Assumes that the data is provided with rows grouped together.
	 * 
	 * @param numRows Number of rows
	 * @param numCols Number of columns
	 * @param data The numbers to enter into the matrix
	 */
	void set(int numRows, int numCols, T... data);

	/**
	 * Remove any instances of -0 in the matrix.
	 */
	void removeNegZero();

	/**
	 * Flush the matrix.
	 */
	void reset();

	/**
	 * Get the value stored in the matrix at the specified position.
	 * 
	 * @param row Row index to get
	 * @param col Column index to get
	 * @return Value in the matrix
	 */
	T get(int row, int col);

	/**
	 * Same as get(int, int) but no bounds checking is done. This should be a little faster, but be
	 * careful that the indices are within the matrix.
	 * 
	 * @param row Row index to get
	 * @param col Column index to get
	 * @return Value stored at position
	 */
	T unsafeGet(int row, int col);

	/**
	 * Set the value in the matrix at row and col to be a.
	 * 
	 * @param row Row
	 * @param col Column
	 * @param a New value to store
	 */
	void set(int row, int col, T a);

	/**
	 * Same as set(int, int, T) but no bounds check is done. Ensure that row and col are within the
	 * matrix.
	 * 
	 * @param row Row index to set
	 * @param col Column index to set
	 * @param a Value to put at position
	 */
	void unsafeSet(int row, int col, T a);

	/**
	 * @return The number of rows in the matrix.
	 */
	int getNumRows();

	/**
	 * @return The number of columns in the matrix.
	 */
	int getNumCols();

	/**
	 * Multiply this matrix on the left with mat.
	 * 
	 * @param mat Matrix to multiply by
	 * @return this * mat
	 */
	Matrix<T> multLeft(Matrix<?> mat);

	/**
	 * Multiply this matrix on the right with mat.
	 * 
	 * @param mat Matrix to multiply by. (not modified)
	 * @return mat * this
	 */
	Matrix<T> multRight(Matrix<?> mat);

	/**
	 * Multiply this matrix on the left with mat. The result is stored in the supplied matrix
	 * container.
	 * 
	 * @param <S> Class to return the result as
	 * @param <V> Type of Number stored in S
	 * @param mat Matrix to multiply by (not modified)
	 * @param container The matrix to store the result in. (modified)
	 * @return this * mat
	 */
	<V extends Number, S extends Matrix<V>> S multLeft(Matrix<?> mat, S container);

	/**
	 * Multiply this matrix on the left with mat. The result is stored in the supplied matrix
	 * container.
	 * 
	 * @param <S> Class to return the result as
	 * @param <V> Type of Number stored in S
	 * @param mat Matrix to multiply by (not modified)
	 * @param container The matrix to store the result in. (modified)
	 * @return mat * this
	 */
	<V extends Number, S extends Matrix<V>> S multRight(Matrix<?> mat, S container);
}
