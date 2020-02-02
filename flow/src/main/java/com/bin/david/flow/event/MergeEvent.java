package com.bin.david.flow.event;

import com.bin.david.flow.await.Await;
import com.bin.david.flow.flow.Flow;

import java.util.Arrays;
import java.util.List;

/**
 * 合并事件
 */
public class MergeEvent<P,R> implements Event<P, List<R>> {

    private List<Event<P, R>> events;


    /**
     * 合并
     * @param events 事件组
     * @param <P> 入参
     * @param <R> 出参
     */
    public static<P, R> MergeEvent<P,R> covert(Event<P, R> ... events){
        return new MergeEvent<>(events);
    }

    private MergeEvent(Event<P, R> ... events){
        this.events = Arrays.asList(events);
    }


    @Override
    public void run(Flow flow, P p, Await<List<R>> await) {
        if(events != null && events.size()>0) {
            Object[] returnDataList = new Object[events.size()];
            final int[] count = new int[1];
            for (Event<P, R> event:events){
                event.run(flow, p, r -> {
                    returnDataList[events.indexOf(event)] = r;
                    count[0] = count[0] + 1;
                    if(count[0] == events.size()){
                        await.exec((List<R>)Arrays.asList(returnDataList));
                    }
                });
            }
        }
    }
}
