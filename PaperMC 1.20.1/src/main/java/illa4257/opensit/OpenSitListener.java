package illa4257.opensit;

import org.bukkit.Location;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.spigotmc.event.entity.EntityDismountEvent;

public class OpenSitListener implements Listener {
    @EventHandler
    public void onDismount(final EntityDismountEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;
        final Entity e = event.getDismounted();
        if (e instanceof BlockDisplay && e.getScoreboardTags().contains("sit")) {
            event.getEntity().teleport(event.getEntity().getLocation().add(0, 1, 0));
            e.remove();
        } else if (e instanceof BlockDisplay && e.getScoreboardTags().contains("sit2")) {
            event.getEntity().teleport(event.getEntity().getLocation().add(0, 1.5, 0));
            e.remove();
        }
    }

    @EventHandler
    public void onInteract(final PlayerInteractEvent event) {
        if (event.getItem() != null || event.getClickedBlock() == null || event.getAction().isLeftClick() || !event.getPlayer().hasPermission("OpenSit.SitClick"))
            return;
        final BlockData d = event.getClickedBlock().getState().getBlockData();
        if (d instanceof Slab || d instanceof Stairs) {
            final boolean t = d instanceof Slab ? ((Slab) d).getType() == Slab.Type.TOP : ((Stairs) d).getHalf() == Bisected.Half.TOP;
            final Location l = event.getClickedBlock().getLocation();
            final BlockDisplay b = (BlockDisplay) l.getWorld().spawnEntity(new Location(l.getWorld(), l.getX() + .5,
                    l.getY() + (t ? .8 : .3),
            l.getZ() + .5), EntityType.BLOCK_DISPLAY);
            b.addScoreboardTag(d instanceof Slab || t ? "sit" : "sit2");
            b.addPassenger(event.getPlayer());
        }
    }
}