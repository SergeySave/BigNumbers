package com.sergeysav.bignum;

/**
 * @author sergeys
 */
public interface MutableNumber<T> extends Comparable<T> {
    T abs();
    T add(T other);
    T divide(T other);
    T modulo(T other);
    T multiply(T other);
    T negate();
    T subtract(T other);
    T copy();
}
