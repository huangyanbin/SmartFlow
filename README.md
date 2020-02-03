# SmartFlow
Android streaming response （Android 流式响应）
##### [点我啊](https://github.com/huangyanbin/SmartFlow)
### 原因
在```Android```中，我们经常碰到异步方法嵌套。比如提交文件之后在提交表单，提交数据根据是否成功然后做出其他逻辑处理。```kotlin```里面提出协程概念，利用语法糖来解决这个问题。在```javaScript```里面也有```async/await```来使异步用起来像同步。而在```java```中我没有找到该特性，使得写起来异步嵌套感觉就是地狱。利用这春节几天时间，试着自己去解决这个问题，造个简单的轮子，于是写了```Flow```小框子。
### 想法
从生活中思考代码，方法嵌套和水流的原理很相似，我们把每个异步当成一个水管，水从一个个管道流过，每个管道可以对水进行加工转换。转换的这个过程我们当成一个事件```Event```。在包装事件中，我们可以对它进行线程转换，事件转换，合并拆分等一系列转换。如果碰到异常，则直接终止这个流。

### 功能
###### 简单使用
通过```Flow``` 静态```create```方法创建一个流，```then```串联下个流，如果不需要返回```Void```泛型。```Event```有两个泛型```P、R```,第一个是前个流```Flow```的返回值类型，第二个是当前流```Flow```返回类型。```await exec```方法是结束当前事件流，并将结果代入下个流。

> 打印两句话

```
Flow.create(new Event<Void,Void>() {
                    @Override
                    public void run(Flow flow, Void aVoid, Await<Void> await) {
                        System.out.println("this is first flow");
                        await.exec(null);
                    }
                    
                }).then(new Event<Void, Void>() {
                    @Override
                    public void run(Flow flow, Void aVoid, Await<Void> await) {
                        System.out.println("this is two flow");
                        await.exec(null); 
                    }
                }).start();
```

> ```Lambda ```简化之后
 

 
```
Flow.create((NoneEvent) (flow, await) -> {
                    System.out.println("this is first flow");
                   
                }).then((NoneEvent) (flow, await) -> {
                        System.out.println("this is two flow");
                }).start();
```
> 两数相加

```
 Flow.create((FirstEvent<Integer>) (flow, await) -> 
                        await.exec(3))
                     .then((Event<Integer, Integer>) (flow, integer, await) -> 
                             await.exec(integer + 5))
                     .resultThen((flow, result) -> 
                             System.out.println("total is"+result))
                     .start();
```
```resultThen```方法返回是当前流的结果，每个```flow```后面使用```resultThen```都可以获取流的结果。如果遇到异常，可以通过```flow throwException```方法抛出，可以在```flow```后面```catchThen```立刻处理，也可以在最后```flow``` ```catchThen```处理。```finallyThen```是事件流结束一个通知。


```
 Flow.create((FirstEvent<Integer>) (flow, await) ->
                        await.exec(0))
                     .then((Event<Integer, Integer>) (flow, perVal, await) ->{
                         if(perVal == 0){
                             flow.throwException("Dividend cannot be 0!");
                         }else{
                             await.exec(perVal/5);
                         }
                     })
                     .resultThen((flow, result) ->
                             System.out.println("total is"+result))
                     .catchThen((flow, e) ->
                             System.out.println(e.getMessage()))
                        .finallyThen((flow, await) -> 
                              System.out.println("this is flow end")).start();
```

###### 切换线程

使用```flow on```方法可以切换线程，```on```传递一个```Converter```参数，代表下个流切换。如果两个```Converter```参数，代表当前流和下个流都切换线程。当然你也可以实现```Converter```接口来实现其他功能。
```
Flow.create((FirstEvent<Integer>) (flow, await) ->
                        await.exec(0))
                     .on(AndroidMain.get(),SingleThread.get())   
                     .then((Event<Integer, Integer>) (flow, perVal, await) ->{
                         if(perVal == 0){
                             flow.throwException("Dividend cannot be 0!");
                         }else{
                             await.exec(perVal/5);
                         }
                     })
                     .on(AndroidMain.get())
                     .resultThen((flow, result) ->
                             System.out.println("total is"+result))
                     .on(AndroidMain.get())
                     .catchThen((flow, e) ->
                             System.out.println(e.getMessage()))
                     .on(SingleThread.get())
                     .finallyThen((flow, await) ->
                              System.out.println("this is flow end")).start();
```

###### ```Collection```结果转换成多个流

```
Flow.each((FirstEvent<List<String>>) (flow, await) -> {
                    ArrayList<String> list = new ArrayList<>();
                    list.add("1");
                    list.add("2");
                    list.add("3");
                    await.exec(list);
                }).then((LastEvent<String>) (flow, s, await) -> {
                    System.out.println("this is"+s);
                }).start();
```
###### 多个流结果转换成一个流


```
 Flow.merge((flow, await) -> await.exec(1),
                        (flow, await) -> await.exec(2),
                        (flow, await) -> await.exec(2)).resultThen((flow, result)
                        ->  System.out.println"result"+result)).start();
```

###### 条件选择
根据条件判断重新发起```Flow```流（返回参数可以不一样）

```
 Flow.create((NoneEvent) (flow,await) ->{
                    System.out.println("start");
                    await.exec();
                })
                 .on(SingleThread.get())
                 .conditionThen((VoidCondition) () -> false,
                                Flow.create((NoneEvent) (flow,await) -> {
                                    System.out.println("this is true");
                                    await.exec();
                                }),
                                Flow.create((NoneEvent) (flow,await) -> {
                                    System.out.println("this is false");
                                    await.exec();
                                })).start();
```
根据条件判断执行```Flow```流，可以合并到一起。（返回参数必须一致）
```
Flow.condition2(() -> isGo, (FirstEvent<Integer>) (flow, await) -> {
                    System.out.println("this is true");
                    await.exec(1);
                }, (flow, await) -> {
                    System.out.println("this is false");
                    await.exec(0);
                }).resultThen((flow, result) ->  System.out.println("result"+result))
                        .watch(this).start();
```


###### 生命周期解绑
通过```flow watch```方法。被观察者必须实现```ILifeObservable```接口。

```
  Flow.create((FirstEvent<Integer>) (flow, await) ->await.exec(0)) 
      .watch(this).start();
```
### 使用


```
allprojects {
		repositories {
			...
			maven { url 'https://www.jitpack.io' }
		}
	}
	
	implementation 'com.github.huangyanbin:SmartFlow:Tag'
```

 * 提供简化的类：FirstEvent 入参是Void省略类， LastEvent 返回值是Void省略类。
VoidEvent入参和返回值都是Void省略类，NoneEvent和VoidEvent一样，但是不需要手动掉await

### 总结
框子也里面提供了一些简化的类，也可以和项目网络请求框架抽象自己的```Event```,这样和```js```的网络的```then```就几乎一样了。后续根据实际需求再做调整，试验中。

