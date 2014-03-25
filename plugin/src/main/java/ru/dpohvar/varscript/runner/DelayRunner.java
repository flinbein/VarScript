package ru.dpohvar.varscript.runner;

/**
 * Created by DPOH-VAR on 05.03.14
 */
public class DelayRunner extends Thread implements Runner{

    private final Runnable runnable;
    private final long delay;
    private boolean running = false;

    public DelayRunner(Runnable runnable, long delayMillis) {
        if (delayMillis < 0) delayMillis = 0;
        this.runnable = runnable;
        this.delay = delayMillis;
    }

    @Override
    public boolean stopRunner(){
        synchronized (this) {
            if (!this.running) return false;
            this.running = true;
        }
        this.interrupt();
        return true;
    }

    @Override
    public void run() {
        synchronized (this) {
            this.running = true;
        }
        if (delay > 0) try {
            sleep(delay);
        } catch (InterruptedException ignored) {
            if (!this.running) return;
        }
        runnable.run();
    }
}
