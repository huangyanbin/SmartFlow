package com.bin.david.flow.transform;


/**
 * Void条件控制
 */
public interface VoidCondition extends Condition<Void> {

     @Override
     default boolean condition(Void r){
          return condition();
     }

      boolean condition();
}
