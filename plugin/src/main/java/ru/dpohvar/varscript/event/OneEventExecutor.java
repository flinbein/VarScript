package ru.dpohvar.varscript.event;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;

public class OneEventExecutor implements EventExecutor {

    private final Class eventClass;
    private final EventHandler handler;

    public OneEventExecutor(Class eventClass, EventHandler handler) {
        this.eventClass = eventClass;
        this.handler = handler;
    }

    @Override
    public void execute(Listener listener, Event event) throws EventException {
        try {
            if (!eventClass.isInstance(event)) return;
            handler.handle(event);
        } catch (Throwable t) {
            throw new EventException(t);
        }
    }
}
