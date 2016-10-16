package com.pl.app.managers;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * Android轻量级工作线程管理器,创建一个带有Looper的线程,减少创建线程开销,
 * 所有需要异步执行的临时任务都可以放在这里处理,但不能处理太耗时的操作,负责后续任务会一直等待
 */
public class LightTaskManager {

    private final HandlerThread mWorkerThread;

    /**
     * 获取实例
     */
    public static LightTaskManager getInstance(){
        return ClassHolder.INSTANCE;
    }

    private Handler mHandler;

    // 静态容器
    private final static class ClassHolder{
        private final static LightTaskManager INSTANCE = new LightTaskManager();
    }

    // 构造方法
    private LightTaskManager(){
        mWorkerThread = new HandlerThread(getClass().getSimpleName());
        mWorkerThread.start();
        mHandler = new Handler(mWorkerThread.getLooper());
    }

    /**
     * 执行常规的异步任务
     * @param runnable 任务
     */
    public void post(Runnable runnable){
        mHandler.post(runnable);
    }

    /**
     * 执行优先级较高的任务,会加入队列的最前端,比如从本地io读取数据并显示
     * @param runnable 任务
     */
    public void postAtFrontOfQueue(Runnable runnable){
        mHandler.postAtFrontOfQueue(runnable);
    }

    /**
     * 延时执行任务,比如本地io写入操作,不需要界面显示
     * @param runnable 任务
     * @param delay 延时时长
     */
    public void postDelayed(Runnable runnable, long delay){
        mHandler.postDelayed(runnable, delay);
    }

    /**
     * 退出线程
     */
    public void destroy(){
        if (mWorkerThread != null){
            mWorkerThread.quit();
        }
    }
}
