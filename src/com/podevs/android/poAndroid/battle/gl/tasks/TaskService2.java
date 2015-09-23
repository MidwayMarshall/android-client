package com.podevs.android.poAndroid.battle.gl.tasks;

import java.util.concurrent.ConcurrentLinkedQueue;

public class TaskService2 {
    private ConcurrentLinkedQueue<Event> tasks;
    private boolean busy;

    public TaskService2() {
        this.tasks = new ConcurrentLinkedQueue<Event>();
    }

    public Event take() {
        while (busy) {
            try {
                Thread.sleep(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Event task = tasks.poll();
        return task;
    }

    public void offer(Event event) {
        busy = true;
        tasks.add(event);
        busy = false;
    }
}
