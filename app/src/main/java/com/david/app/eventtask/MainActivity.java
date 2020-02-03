package com.david.app.eventtask;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.bin.david.eventtask.R;
import com.bin.david.flow.ILifeObservable;
import com.bin.david.flow.ILifer;
import com.bin.david.flow.event.Event;
import com.bin.david.flow.event.FirstEvent;
import com.bin.david.flow.event.LastEvent;
import com.bin.david.flow.event.NoneEvent;
import com.bin.david.flow.flow.Flow;
import com.bin.david.flow.transform.AndroidMain;
import com.bin.david.flow.transform.SingleThread;
import com.bin.david.flow.transform.VoidCondition;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ILifeObservable {

    private List<ILifer> lifers = new CopyOnWriteArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn1).setOnClickListener(this);
        findViewById(R.id.btn2).setOnClickListener(this);
        findViewById(R.id.btn3).setOnClickListener(this);
        findViewById(R.id.btn4).setOnClickListener(this);
        findViewById(R.id.btn5).setOnClickListener(this);
        super.onCreate(savedInstanceState);


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn1:

                Flow.create((FirstEvent<Integer>) (flow, await) ->
                        await.exec(0))
                     .on(AndroidMain.get(), SingleThread.get())
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
                     .finallyThen((flow) ->
                              System.out.println("this is flow end"))
                        .watch(this).start();
                break;
            case R.id.btn2:
                boolean isGo = false;
                Flow.condition2(() -> isGo, (FirstEvent<Integer>) (flow, await) -> {
                    System.out.println("this is true");
                    await.exec(1);
                }, (flow, await) -> {
                    System.out.println("this is false");
                    await.exec(0);
                }).resultThen((flow, result) ->  System.out.println("result"+result))
                        .watch(this).start();
                break;
            case R.id.btn3:
                Flow.merge((flow, await) -> await.exec(1),
                        (flow, await) -> await.exec(2),
                        (flow, await) -> await.exec(2)).resultThen((flow, result)
                        ->  System.out.println("result"+result)).start();
                break;
            case R.id.btn4:
                Flow.each((FirstEvent<List<String>>) (flow, await) -> {
                    ArrayList<String> list = new ArrayList<>();
                    list.add("1");
                    list.add("2");
                    list.add("3");
                    await.exec(list);
                }).then((LastEvent<String>) (flow, s, await) -> {
                    System.out.println(s);
                }).start();
                break;
            case R.id.btn5: {
                Flow.create((NoneEvent) (flow) ->{
                    System.out.println("start");
                })
                 .on(SingleThread.get())
                 .conditionThen((VoidCondition) () -> false,
                                Flow.create((NoneEvent) (flow) -> {
                                    System.out.println("this is true");

                                }),
                                Flow.create((NoneEvent) (flow) -> {
                                    System.out.println("this is false");

                                })).start();
            }
                break;
        }
    }

    @Override
    public void watch(ILifer lifer) {
        lifers.add(lifer);
    }

    @Override
    protected void onDestroy() {
        for(ILifer lifer:lifers){
            if(!lifer.isUnSubscribe()) {
                lifer.unSubscribe();
            }
        }
        super.onDestroy();
    }
}
