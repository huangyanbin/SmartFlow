package com.bin.david.flow.transform;

/**
 * 条件控制
 * @param <R>
 */
public interface Condition<R> {

     boolean condition(R r);
}
