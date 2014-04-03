package ru.dpohvar.varscript.runner;

import java.util.Timer;
import java.util.TimerTask;

public class PeriodicRunner implements Runner {

    private final Runnable runnable;
    private final long delay;
    private final long period;
    Timer time;

    public PeriodicRunner(Runnable runnable, long periodMillis, long delayMillis) {
        if (delayMillis < 0) delayMillis = 0;
        if (periodMillis < 0) periodMillis = 0;
        this.runnable = runnable;
        this.delay = delayMillis;
        this.period = periodMillis;
        time = new Timer();
    }

    @Override
    public synchronized boolean stopRunner() {
        if (time == null) return false;
        time.cancel();
        time = null;
        return true;

    }

    public void start() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runnable.run();
            }
        };
        synchronized (this) {
            if (time != null) time.scheduleAtFixedRate(task, delay, period);
        }
    }
}
