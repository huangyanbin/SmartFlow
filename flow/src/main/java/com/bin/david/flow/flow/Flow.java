package com.bin.david.flow.flow;

import com.bin.david.flow.ILifeObservable;
import com.bin.david.flow.await.Await;
import com.bin.david.flow.event.ConditionEvent;
import com.bin.david.flow.event.ConverterEvent;
import com.bin.david.flow.event.EachEvent;
import com.bin.david.flow.event.Event;
import com.bin.david.flow.event.FirstEvent;
import com.bin.david.flow.event.MergeEvent;
import com.bin.david.flow.event.NoneEvent;
import com.bin.david.flow.event.ResultEvent;
import com.bin.david.flow.exception.FlowException;
import com.bin.david.flow.log.Log;
import com.bin.david.flow.transform.Condition;
import com.bin.david.flow.transform.Converter;
import com.bin.david.flow.transform.VoidCondition;

import java.util.Collection;
import java.util.List;

/**
 * 流
 * @param <P> parameter入参
 * @param <R> return 返回值
 *@author huangYanbin
 */
public  class Flow<P,R> implements Event<P, R> {

    /**
     * 日志开关
     */
    public static boolean LOG_ENABLE = false;

    /**
     * 下一个流
     */
    private Flow<R, ?> next;
    /**
     * 原始事件
     */
    private Event<P, R> originEvent;
    /**
     * 是否取消
     */
    private boolean isCancel;


    /**
     * 异常事件
     */
    private Event<FlowException,Void> exceptionEvent;

    /**
     * 最后回调事件
     */
    private Event<Void,Void>  finallyEvent;

    /**
     * 是否执行了最后回调事件
     */
    private boolean isRunFinally;
    /**
     * 前一个流
     */
    private Flow<?, P> per;

    /**
     * 临时放置下个转换器
     */
    private Converter tempNextConverter;

    /**
     * 是否设置了转换器
     */
    private boolean isSetConverter;

    /**
     * 入参
     */
    private P p;


    /**
     * 管道顺序
     */
    private int position;








    protected Flow(Event<P, R> event) {
        this.originEvent = event;
    }



    /**
     * 创建流
     * @param event 事件
     * @param <P> 入参
     * @param <R> 出参
     * @return 开始流
     */
    public static <P, R> Flow<P, R> create(final Event<P, R> event) {

        return new Flow<>(event);
    }


    /**
     * 创建流将列表转换单个for发送
     * @param <P> 入参
     * @param <R> 出参
     * @return 开始流
     */
    public static <P,R,C extends Collection<R>> Flow<P, R> each(Event<P, C> nextEvent) {
        EachEvent<P,R,C> flatEvent = new EachEvent<>(nextEvent);
        return create(flatEvent);
    }


    /**
     * 创建流
     * @param p 入参
     * @param event 事件
     * @param <P> 入参
     * @param <R> 出参
     * @return 开始流
     */
    public static <P, R> Flow<P, R> create(P p,final Event<P, R> event) {

        Flow<P, R>  flow = create(event);
        flow.p = p;
        return flow;
    }





    /**
     * 创建合并多个任务
     * @param firstEvents 第一个任务组
     * @param <R> 返回值
     * @return 第一个任务
     */
    public static<R> Flow<Void,List<R>> merge(FirstEvent<R>... firstEvents){
        return Flow.create(MergeEvent.covert(firstEvents));
    }



    /**
     * 创建条件判断选择
     * @return 第一个任务
     */
    public static<R> Flow<Void,R> condition2(VoidCondition condition, FirstEvent<R> trueEvent, FirstEvent<R> falseEvent){
        return Flow.create(ConditionEvent.covert(condition,trueEvent,falseEvent));

    }

    /**
     * 创建条件判断选择
     * @return 第一个任务
     */
    public static<R1,R2> Flow<Void,Void> condition(VoidCondition condition, Flow<Void,R1> trueFlow, Flow<Void,R2> falseFlow){
        return new ConditionFlow<>(condition,trueFlow,falseFlow);
    }


   /**
     * 观察生命周期
     */
    public Flow<P, R> watch(ILifeObservable observable){
        FlowObserver flowObserver = new FlowObserver(getStartFlow());
        observable.watch(flowObserver);
        return this;
    }



    /**
     * 放置下一个事件
     *
     * @param nextEvent 下一个事件
     * @param <T>  下个流返回参数
     * @return 下一个流
     */
    public <T> Flow<R, T> then(Event<R, T> nextEvent) {
        Flow<R,T> nextFlow = create(nextEvent);
        return then(nextFlow);
    }

    /**
     * 放置下一个事件 将列表转换单个for发送
     *
     * @param nextEvent 下一个事件
     * @param <T>  下个流返回参数
     * @return 下一个流
     */
    public <T,C extends Collection<T>> Flow<R, T> eachThen(Event<R,C > nextEvent) {
        EachEvent<R,T,C> flatEvent = new EachEvent<>(nextEvent);
        return then(flatEvent);
    }

    /**
     * 放置下一个流
     *
     * @param nextFlow 下一个流
     * @param <T>  下个流返回参数
     * @return 下一个流
     */
    public <T> Flow<R, T> then(Flow<R, T> nextFlow) {
        this.next = nextFlow;
        this.next.setPerFlow(this);
        //统一转换线程
        nextFlow.originEvent = covertEvent(nextFlow.originEvent);
        this.next.position = position+1;
        return nextFlow;
    }

    /**
     * 创建条件判断选择
     * @return 第一个任务
     */
    public <R1,R2> Flow<R,Void> conditionThen(Condition<R> condition, Flow<Void,R1> trueFlow, Flow<Void,R2> falseFlow){
        Flow<R,Void> nextFlow = new ConditionFlow<>(condition,trueFlow,falseFlow);
        return then(nextFlow);
    }

    /**
     * 条件判断执行下个任务
     *
     * @param trueEvent true事件
     * @param falseEvent false事件
     * @param <T>  下个流返回参数
     * @return 下一个流
     */
    public <T> Flow<R, T> conditionThen2(Condition<R> condition, Event<R, T> trueEvent, Event<R, T> falseEvent) {
        ConditionEvent<R,T> conditionEvent = ConditionEvent.covert(condition,trueEvent,falseEvent);
        Flow<R,T> nextFlow = create(conditionEvent);
        return then(nextFlow);
    }


    /**
     * 合并多个事件放入下一个事件
     *
     * @param nextEvent 下一个事件
     * @param <T>  下个流返回参数
     * @return 下一个流
     */
    public <T> Flow<R, List<T>> mergeThen(Event<R, T>... nextEvent) {
        Flow<R,List<T>> nextFlow = create(MergeEvent.covert(nextEvent));
        return then(nextFlow);
    }


    /**
     * 回调当前数据结果
     * @param resultEvent
     */
    public Flow<R, R> resultThen(ResultEvent<R> resultEvent) {
        return then((flow, r, await) -> {
            resultEvent.run(flow,r,null);
            await.exec(r);
        });
    }

    /**
     * 异常处理
     */
    public Flow<P, R> catchThen(ResultEvent<FlowException> exceptionEvent) {
        this.exceptionEvent = covertEvent(exceptionEvent);
        return this;
    }

    /**
     * 最后回调
     */
    public Flow<P, R> finallyThen(NoneEvent finallyEvent) {
        getStartFlow().finallyEvent = covertEvent(finallyEvent);
        return this;
    }

    /**
     * 转换事件
     * @param event 事件
     * @param <P1> 入参
     * @param <R1> 出参
     * @return 返回代理事件
     */
    private<P1, R1> Event<P1, R1> covertEvent(final Event<P1, R1> event){
        if(isSetConverter) {
            isSetConverter = false;
            return  ConverterEvent.covert(tempNextConverter,event);
        }
        return event;
    }

    /**
     * 转换原始事件
     * @param converter 转换器
     */
    private void covertOriginEvent(Converter converter){
        this.originEvent = ConverterEvent.covert(converter, getOriginEvent());
    }



    /**
     * 切换线程 第一次设置
     * @param perConverter 前任务转换器
     * @param nextConverter 后任务转换器
     */
    public  Flow<P, R> on(Converter perConverter, Converter nextConverter){
        if(per == null){
            covertOriginEvent(perConverter);
        }else{
            per.covertOriginEvent(perConverter);
        }
        on(nextConverter);
        return this;
    }


    /**
     * 切换线程
     * @param nextConverter 后续转换
     */
    public  Flow<P, R> on(Converter nextConverter){
        this.tempNextConverter = nextConverter;
        isSetConverter = true;
        return this;
    }




    /**
     * 执行
     *
     * @param p     入参
     * @param await 返回等待监听
     */

    @Override
    public void run(Flow flow, P p, Await<R> await) {
        getOriginEvent().run( flow,p, await);
    }



    /**
     * 获取下一个流
     *
     * @return 下一个流
     */
    public Flow<R, ?> getNext() {
        return next;
    }

    /**
     * 获取真实的事件
     */
    public Event<P, R> getOriginEvent() {
        return originEvent;
    }



    /**
     * 设置前个流
     */
    public void setPerFlow(Flow<?, P> per) {
        this.per = per;
    }

    /**
     * 获取前个流
     * @return 流
     */
    public Flow<?, P> getPer() {
        return per;
    }

    /**
     * 前个事件调用执行
     */
    public void exc(P p) {
        //保留入参数据
        this.p = p;
        //取消
        if(isCancel()){
            Log.w(getDesc()+"isCancel");
            return;
        }
        try {
            run(this,p, r -> {
                if(isCancel()){
                    Log.w(getDesc()+"isCancel");
                    return;
                }
                excNext(r);
            });
        } catch (Exception e) {
            //捕获异常
            throwException(new FlowException(e),true);
        }

    }

    /**
     * 执行下个任务
     * @param r 入参
     */
    protected void excNext(R r) {
        if (getNext() != null) {
            getNext().exc(r);
        }else{
            //没有其他任务则通知最后回调事件
            finallyEvent(this);
        }
    }

    /**
     * 调用任务栈启动执行
     */
    public void start() {
        isCancel = false;
       if(per != null){
           per.isRunFinally = false;
           per.start();
       }else{
           exc(p);
       }
    }

    /**
     * 停止执行任务
     */
    public void stop(){
        setCancel(true);
    }


    /**
     * 抛出异常
     * @param e 异常
     * @param useComm 是否使用公共处理（如果开启使用会递归后续事件查找设置了异常接收事件）
     */
    public void throwException(FlowException e,boolean useComm) {
        finallyEvent(this);
        Event<FlowException, Void> event = getExceptionEvent(this,useComm);
        if (event != null) {
            event.run(this,e, null);
        }
    }

    /**
     * 获取处理异常
     */
    private Event<FlowException, Void> getExceptionEvent(Flow<?,?> flow,boolean useComm){
        if(!useComm){
            return getExceptionEvent();
        }
        if(flow.getExceptionEvent()  == null){
            Flow nextFlow = flow.getNext();
            if(nextFlow != null) {
                return nextFlow.getExceptionEvent();
            }
            return null;
        }
        return flow.getExceptionEvent();
    }


    /**
     * 获取处理错误处理
     */
    public Event<FlowException, Void> getExceptionEvent() {
        return exceptionEvent;
    }

    /**
     * 抛出异常
     * @param tag 异常标识
     *@param useComm 是否使用后续异常处理事件
     */
    public  void throwException(Object tag,boolean useComm){
        throwException(new FlowException(tag),useComm);
    }

    /**
     * 抛出异常（使用后续异常处理事件）
     * @param message 错误信息
     */
    public  void throwException(String message){
        throwException(new FlowException(message),true);
    }

    /**
     * 抛出异常
     * @param message 错误信息
     * @param useComm 是否使用后续异常处理事件
     */
    public  void throwException(String message,boolean useComm){
        throwException(new FlowException(message),useComm);
    }
    /**
     * 是否取消
     * @return 取消状态
     */
    public boolean isCancel() {
        return isCancel;
    }

    /**
     * 设置取消
     * @param cancel 取消
     */
    public void setCancel(boolean cancel) {
        isCancel = cancel;
        finallyEvent(this);
        if(next != null){
            next.setCancel(cancel);
        }
    }

    /**
     * 相关描述
     */
    public String getDesc() {
        return "Flow"+position;
    }

    /**
     * 获取开始flow
     * @return 第一个flow
     */
    private Flow getStartFlow(){
        return getStartFlow(this);
    }

    /**
     * 获取开始flow
     * @param flow 流
     * @return 第一个flow
     */
    private static Flow getStartFlow(Flow flow){
        Flow per = flow.getPer();
        if(per == null){
            return flow;
        }
        return getStartFlow(per);
    }

  private void finallyEvent(Flow flow){
       Flow firstFlow = getStartFlow();
       if(!firstFlow.isRunFinally) {
           Event<Void, Void> finallyEvent = firstFlow.finallyEvent;
           firstFlow.isRunFinally = true;
           if (finallyEvent != null) {
               finallyEvent.run(flow, null, null);
           }
       }

  }
}


