package ru.dpohvar.varscript.event;

import org.bukkit.event.Event;
import org.bukkit.event.Listener;

/**
 * Handler for Bukkit event
 */
public interface EventHandler<T extends Event> extends Listener {

    /**
     * Method for handling event
     *
     * @param event Bukkit event
     */
    public void handle(T event);
}
