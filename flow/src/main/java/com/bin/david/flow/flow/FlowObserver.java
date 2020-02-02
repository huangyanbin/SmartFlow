package com.bin.david.flow.flow;


import com.bin.david.flow.ILifer;

/**
 * 流观察者
 */
public class FlowObserver implements ILifer {

    private Flow firstFlow;


    public FlowObserver(Flow flow) {
        this.firstFlow = flow;
    }


    @Override
    public void unSubscribe() {
        firstFlow.setCancel(true);
    }

    @Override
    public boolean isUnSubscribe() {
        return firstFlow.isCancel();
    }
}
