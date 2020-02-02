package com.bin.david.flow.transform;

/**
 * 转换器
 * 主要用于线程切换操作
 * @author  huangYanbin
 */
public interface Converter {


    /**
     * 转换
     */
    void transform(Runnable runnable);


}
