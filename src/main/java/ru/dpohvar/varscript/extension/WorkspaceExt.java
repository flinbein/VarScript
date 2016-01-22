package ru.dpohvar.varscript.extension;

import groovy.lang.Closure;
import org.bukkit.block.Block;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import ru.dpohvar.varscript.trigger.BukkitEventTrigger;
import ru.dpohvar.varscript.workspace.Workspace;


public class WorkspaceExt {

    public static BukkitEventTrigger<PlayerInteractEvent> onClick(Workspace self, Block block, Closure closure) {
        ClickClosure clickClosure = new ClickClosure(closure, block);
        return self.listen(PlayerInteractEvent.class, clickClosure);
    }


    private static class ClickClosure extends Closure {

        Block block;

        public ClickClosure(Closure owner, Block block) {
            super(owner, null);
            this.block = block;
        }

        @Override
        public Object call(Object arg) {
            PlayerInteractEvent event = (PlayerInteractEvent) arg;
            if (!event.getClickedBlock().equals(block)) return null;
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return null;
            event.setCancelled(true);
            ((Closure) getOwner()).call(arg);
            return null;
        }

        @Override
        public Class[] getParameterTypes() {
            return new Class[]{PlayerInteractEvent.class};
        }
    }

}
