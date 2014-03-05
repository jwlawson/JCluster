package uk.co.jwlawson.jcluster;

public interface Matrix<T extends Number> {

	/**
	 * Create a copy of this matrix which is then completely independent of
	 * this.
	 * 
	 * @return A copy of this matrix.
	 */
	public Matrix<T> copyMatrix();

	/**
	 * Set all values in this matrix. Assumes that the data is provided with
	 * rows grouped together.
	 * 
	 * @param numRows Number of rows
	 * @param numCols Number of columns
	 * @param data The numbers to enter into the matrix
	 */
	public void set(int numRows, int numCols, T... data);

	/**
	 * Remove any instances of -0 in the matrix.
	 */
	public void removeNegZero();

	/**
	 * Flush the matrix.
	 */
	public void reset();

	/**
	 * Get the value stored in the matrix at the specified position.
	 * 
	 * @param row
	 * @param col
	 * @return Value in the matrix
	 */
	public T get(int row, int col);

	/**
	 * Same as get(int, int) but no bounds checking is done. This should be a
	 * little faster, but be careful that the indices are within the matrix.
	 * 
	 * @param row
	 * @param col
	 * @return
	 */
	T unsafe_get(int row, int col);

	/**
	 * Set the value in the matrix at row and col to be a.
	 * 
	 * @param row Row
	 * @param col Column
	 * @param a New value to store
	 */
	public void set(int row, int col, T a);

	/**
	 * Same as set(int, int, T) but no bounds check is done. Ensure that row and
	 * col are within the matrix.
	 * 
	 * @param row
	 * @param col
	 * @param a
	 */
	void unsafe_set(int row, int col, T a);

	/**
	 * @return The number of rows in the matrix.
	 */
	public int getNumRows();

	/**
	 * @return The number of columns in the matrix.
	 */
	public int getNumCols();

	/**
	 * Multiply this matrix on the left with mat.
	 * 
	 * @param mat Matrix to multiply by
	 * @return this * mat
	 */
	public Matrix<T> multLeft(Matrix<?> mat);

	/**
	 * Multiply this matrix on the right with mat.
	 * 
	 * @param mat Matrix to multiply by. (not modified)
	 * @return mat * this
	 */
	public Matrix<T> multRight(Matrix<?> mat);

	/**
	 * Multiply this matrix on the left with mat. The result is stored in the
	 * supplied matrix container.
	 * 
	 * @param mat Matrix to multiply by (not modified)
	 * @param container The matrix to store the result in. (modified)
	 * @return this * mat
	 */
	public <V extends Number, S extends Matrix<V>> S multLeft(Matrix<?> mat,
			S container);

	/**
	 * Multiply this matrix on the left with mat. The result is stored in the
	 * supplied matrix container.
	 * 
	 * @param mat Matrix to multiply by (not modified)
	 * @param container The matrix to store the result in. (modified)
	 * @return mat * this
	 */
	public <V extends Number, S extends Matrix<V>> S multRight(Matrix<?> mat,
			S container);
}