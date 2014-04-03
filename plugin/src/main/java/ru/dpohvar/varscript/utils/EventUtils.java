package ru.dpohvar.varscript.utils;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.*;
import ru.dpohvar.varscript.event.EventHandler;
import ru.dpohvar.varscript.event.OneEventExecutor;

import java.lang.reflect.Method;

public class EventUtils {

    public static Class<? extends Event> getRegistrationClass(Class<? extends Event> clazz) {
        try {
            clazz.getDeclaredMethod("getHandlerList");
            return clazz;
        } catch (NoSuchMethodException e) {
            if (clazz.getSuperclass() != null
                    && !clazz.getSuperclass().equals(Event.class)
                    && Event.class.isAssignableFrom(clazz.getSuperclass())) {
                return getRegistrationClass(clazz.getSuperclass().asSubclass(Event.class));
            } else {
                throw new IllegalPluginAccessException("Unable to find handler list for event " + clazz.getName());
            }
        }
    }

    public static HandlerList getHandlerList(Class<? extends Event> type) {
        try {
            Method method = getRegistrationClass(type).getDeclaredMethod("getHandlerList");
            method.setAccessible(true);
            return (HandlerList) method.invoke(null);
        } catch (Exception e) {
            throw new IllegalPluginAccessException(e.toString());
        }
    }

    public static RegisteredListener createRegisteredListener
            (Plugin plugin, Class<? extends Event> eventClass, EventPriority priority, boolean ignoreCancelled, EventHandler<? extends Event> handler){
        boolean useTimings = Bukkit.getServer().getPluginManager().useTimings();
        EventExecutor executor = new OneEventExecutor(eventClass, handler);
        if (useTimings) {
            return new TimedRegisteredListener(handler, executor, priority, plugin, ignoreCancelled);
        } else {
            return new RegisteredListener(handler, executor, priority, plugin, ignoreCancelled);
        }
    }
}
