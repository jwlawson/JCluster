package uk.co.jwlawson.jcluster.pool.dummy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import uk.co.jwlawson.jcluster.QuiverKey;
import uk.co.jwlawson.jcluster.QuiverMatrix;
import uk.co.jwlawson.jcluster.pool.Pool;

public class DummyMatrixPool<T extends QuiverMatrix> implements Pool<T> {

	private Constructor<T> constructor;
	private final int rows;
	private final int cols;

	public DummyMatrixPool(QuiverKey<T> key) {
		Class<T> clazz = key.getClassObject();
		rows = key.getRows();
		cols = key.getCols();
		try {
			constructor = clazz.getConstructor(Integer.TYPE, Integer.TYPE);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("No constructor found for class " + clazz, e);
		} catch (SecurityException e) {
			throw new RuntimeException("Cannot access constructor for class " + clazz, e);
		}
	}

	public T getObj() {
		try {
			return constructor.newInstance(rows, cols);
		} catch (InstantiationException e) {
			throw new RuntimeException("Constructor cannot create instance", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Constructor has incorrect access modifier", e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Illegal argument for constructor", e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Invocation error", e);
		}
	}

	public void returnObj(T object) {
		object = null;
	}

}
