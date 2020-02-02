# SmartFlow
Android streaming response 
##### [中文说明](README.md)
### Cause
In Android, we often encounter asynchronous method nesting. For example, after submitting a file, submit a form, and then make other logical processing according to whether the data is successful or not. Kotlin puts forward the concept of CO process and uses grammar sugar to solve this problem. There is also async / await in JavaScript to make asynchrony work like synchronization. In Java (before Java 9), there is no such feature, which makes writing asynchronous nesting feel like hell. During the Spring Festival, I tried to alleviate the problem, so I wrote a flow box.
### Idea
Thinking about code from life, the principle of method nesting and water flow is very similar. We treat each asynchrony as a water pipe, and water flows through each pipe, and each pipe can process and transform water. This process of transformation is regarded as an event. In the wrapper event, we can transform it by thread, event, merge and split. If an exception is encountered, the flow is terminated directly.

### Function
###### Simple example
Create a flow through the flow static create method, then concatenate the next flow, if you don't need to return the void generics. Event has two generics, P and R. the first is the return value type of the previous flow, and the second is the return type of the current flow. The await exec method is to end the current event flow and substitute the result into the next flow.

>Print two sentences

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

> Lambda simple

```
Flow.create((NoneEvent) (flow, await) -> {
                    System.out.println("this is first flow");
                    await.exec(); 
                }).then((NoneEvent) (flow, await) -> {
                        System.out.println("this is two flow");
                        await.exec();
                }).start();
```
> Addition of two numbers

```
 Flow.create((FirstEvent<Integer>) (flow, await) -> 
                        await.exec(3))
                     .then((Event<Integer, Integer>) (flow, integer, await) -> 
                             await.exec(integer + 5))
                     .resultThen((flow, result) -> 
                             System.out.println("total is"+result))
                     .start();
```
The resultthen method returns the result of the current flow, which can be obtained by using resultthen after each flow. If an exception is encountered, it can be thrown through the flow throwexception method. It can be processed immediately after the flow or at the end of the flow. Finally then is a notification that the event flow ends.


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

###### Switching thread
Use the on method to switch threads. On passes a switch parameter, representing the next flow switch. If two parameters, it means that the current flow and the next flow switch threads. Of course, you can also implement the converter interface to achieve other functions.
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

###### The collection result is converted to multiple streams

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
###### Multiple stream results converted to one stream


```
 Flow.merge((flow, await) -> await.exec(1),
                        (flow, await) -> await.exec(2),
                        (flow, await) -> await.exec(2)).resultThen((flow, result)
                        ->  System.out.println"result"+result)).start();
```

###### condition selection
Reinitiate flow flow according to condition judgment (return parameters can be different)

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
The flow flow is executed according to the condition judgment and can be combined. (return parameters must be consistent)
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


###### Lifecycle unbound
Through the flow watch method. The observed must implement the ilifeobservable interface.

```
  Flow.create((FirstEvent<Integer>) (flow, await) ->await.exec(0)) 
      .watch(this).start();
```


### summary
flow has been introduced into the actual project, which provides some simplified classes, and can also abstract its own Event with the project network request framework, which is almost the same as that of JS's then. Later, adjust according to the actual needs, in the test.













