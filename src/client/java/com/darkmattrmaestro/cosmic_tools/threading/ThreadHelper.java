package com.darkmattrmaestro.cosmic_tools.threading;

import com.badlogic.gdx.utils.PauseableThread;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ThreadHelper {
    private static final Map<String, Thread> threadMap = new HashMap<>();

    public static PauseableThread createPauseable(String name, Runnable runnable) {
        PauseableThread thread = new PauseableThread(runnable);
        thread.setName(name);
        threadMap.put(name, thread);
        return thread;
    }

    public static @Nullable Thread getThread(String name) {
        return threadMap.get(name);
    }
}