package ru.dpohvar.varscript.groovy.extension

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.event.Event
import ru.dpohvar.varscript.Workspace
import ru.dpohvar.varscript.event.EventHandler
import ru.dpohvar.varscript.groovy.BukkitExtUtils
import ru.dpohvar.varscript.groovy.BukkitExtUtils.EventTrigger
import ru.dpohvar.varscript.groovy.BukkitExtUtils.DelayTrigger
import ru.dpohvar.varscript.groovy.BukkitExtUtils.AsyncDelayTrigger
import ru.dpohvar.varscript.groovy.BukkitExtUtils.PeriodTrigger
import ru.dpohvar.varscript.groovy.BukkitExtUtils.AsyncPeriodTrigger
import ru.dpohvar.varscript.groovy.BukkitExtUtils.FinisherTrigger

/**
 * /g> Number.isAssignableFrom(Integer)
 */

@SuppressWarnings("GroovyUnusedDeclaration")
@CompileStatic
class WorkspaceExtension<Z extends Workspace> {

    /**
     * register new event handler. Closure should have one parameter with registering class. example:\n
     * <code> workspace.register { BlockBreakEvent event -> ... }</code>
     * @param self
     * @param closure event handler
     * @return trigger that can stop the listening of the event
     */
    public static EventTrigger register(Workspace self, Closure closure) {
        Class[] params = closure.parameterTypes
        assert params.length == 1, "wrong number of arguments in closure"
        Class eventClass = params[0]
        assert Event.isAssignableFrom(eventClass), "listen to not event class"
        long id = self.addEvent closure as EventHandler, eventClass
        new EventTrigger(id: id, workspace: self)
    }

    /**
     * register new event handler. example:\n
     * <code> workspace.register (BlockBreakEvent) { event -> ... }</code>
     * @param self
     * @param eventClass class of listening event
     * @param closure event handler
     * @return trigger that can stop the listening of the event
     */
    @CompileStatic(TypeCheckingMode.SKIP)
    public static EventTrigger register(Workspace self, Class<? extends Event> eventClass, Closure closure) {
        assert Event.isAssignableFrom(eventClass), "listen to not event class"
        long id = self.addEvent closure as EventHandler, eventClass
        new EventTrigger(id: id, workspace: self)
    }

    /**
     * register new event handler. example:\n
     * <code> workspace.register ("BlockBreakEvent") { event -> ... }</code>
     * @param self
     * @param eventClass class of listening event. if class is custom, need to specify the full name of class
     * @param closure event handler
     * @return trigger that can stop the listening of the event
     */
    public static EventTrigger register(Workspace self, String eventClassName, Closure closure) {
        register self, BukkitExtUtils.getEventClass(eventClassName), closure
    }

    /**
     * register new event handler. Closure should have one parameter with registering class. example:\n
     * <code> workspace.listen { BlockBreakEvent event -> ... }</code>\n
     * Cancelled events will be ignored
     * @param self
     * @param closure event handler
     * @return trigger that can stop the listening of the event
     */
    public static EventTrigger listen(Workspace self, Closure closure) {
        Class[] params = closure.parameterTypes
        assert params.length == 1, "wrong number of arguments in closure"
        Class eventClass = params[0]
        assert Event.isAssignableFrom(eventClass), "listen to not event class"
        long id = self.addEvent closure as EventHandler, eventClass, true
        new EventTrigger(id: id, workspace: self)
    }

    /**
     * register new event handler. example:\n
     * <code> workspace.listen (BlockBreakEvent) { event -> ... }</code>\n
     * Cancelled events will be ignored
     * @param self
     * @param eventClass class of listening event
     * @param closure event handler
     * @return trigger that can stop the listening of the event
     */
    @CompileStatic(TypeCheckingMode.SKIP)
    public static EventTrigger listen(Workspace self, Class<? extends Event> eventClass, Closure closure) {
        assert Event.isAssignableFrom(eventClass), "listen to not event class"
        long id = self.addEvent closure as EventHandler, eventClass, true
        new EventTrigger(id: id, workspace: self)
    }

    /**
     * register new event handler. example:\n
     * <code> workspace.listen ("BlockBreakEvent") { event -> ... }</code>\n
     * Cancelled events will be ignored
     * @param self
     * @param eventClass class of listening event. if class is custom, need to specify the full name of class
     * @param closure event handler
     * @return trigger that can stop the listening of the event
     */
    public static EventTrigger listen(Workspace self, String eventClassName, Closure closure) {
        listen self, BukkitExtUtils.getEventClass(eventClassName), closure
    }

    /**
     * run task synchronously after delay
     * @param self
     * @param ticks delay in ticks
     * @param closure task
     * @return trigger that can cancel execution
     */
    public static DelayTrigger delay(Workspace self, long ticks, Closure closure) {
        long id = self.addDelay closure, ticks
        new DelayTrigger(id: id, workspace: self)
    }

    /**
     * run task synchronously every N ticks
     * @param self
     * @param ticks period in ticks
     * @param closure task
     * @return trigger that can cancel execution
     */
    public static PeriodTrigger period(Workspace self, long ticks, Closure closure) {
        long id = self.addPeriod closure, ticks
        new PeriodTrigger(id: id, workspace: self)
    }

    /**
     * run task asynchronously after delay
     * @param self
     * @param ticks delay in milliseconds
     * @param closure task
     * @return trigger that can cancel execution
     */
    public static AsyncDelayTrigger wait(Workspace self, long millis, Closure closure) {
        long id = self.addAsyncDelay closure, millis
        new AsyncDelayTrigger(id: id, workspace: self)
    }

    /**
     * run task asynchronously every N milliseconds
     * @param self
     * @param ticks period in milliseconds
     * @param closure task
     * @return trigger that can cancel execution
     */
    public static AsyncPeriodTrigger timer(Workspace self, long millis, Closure closure) {
        long id = self.addAsyncPeriod closure, millis
        new AsyncPeriodTrigger(id: id, workspace: self)
    }

    /**
     * run task when workspace is stopped
     * @param self
     * @param closure task
     * @return trigger that can cancel execution
     */
    public static FinisherTrigger onStop(Workspace self, Closure closure) {
        long id = self.addFinisher closure
        new FinisherTrigger(id: id, workspace: self)
    }
}
