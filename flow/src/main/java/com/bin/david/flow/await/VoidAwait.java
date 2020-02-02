package com.bin.david.flow.await;

/**
 * Void等待返回
 *
 */
public interface VoidAwait extends Await<Void> {

    /**
     *默认执行
     */
    default void exec(Void v){
        exec();
    }
    /**
     *执行
     */
    void exec();
}
