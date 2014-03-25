package ru.dpohvar.varscript.event;

import org.bukkit.event.Event;
import org.bukkit.event.Listener;

/**
 * Created by DPOH-VAR on 26.02.14
 */
public interface EventHandler<T extends Event> extends Listener {

    public void handle(T event);
}
