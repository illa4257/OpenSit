package illa4257.opensit;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.util.Vector;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.ArrayList;
import java.util.List;

public class OpenSitListener implements Listener {
    public static void removeSitsInBlock(final Location l) {
        final double sy = l.getBlockY() - .1, ey = l.getBlockY() + 1;
        final int sx = l.getBlockX(), sz = l.getBlockZ(), ex = sx + 1, ez = sz + 1;
        for (final Entity e : l.getChunk().getEntities())
            if (e instanceof BlockDisplay && (e.getScoreboardTags().contains("sit") || e.getScoreboardTags().contains("sit2"))) {
                final Location l2 = e.getLocation();
                if (
                        l2.getY() > sy && l2.getY() <= ey &&
                                l2.getX() >= sx && l2.getX() <= ex &&
                                l2.getZ() >= sz && l2.getZ() <= ez
                ) e.remove();
            }
    }

    @EventHandler
    public void onDismount(final EntityDismountEvent event) {
        final Entity e = event.getDismounted();
        if (e instanceof BlockDisplay) {
            if (e.getScoreboardTags().contains("sit"))
                event.getEntity().teleport(event.getEntity().getLocation().add(0, 1, 0));
            else if (e.getScoreboardTags().contains("sit2"))
                event.getEntity().teleport(event.getEntity().getLocation().add(0, 1.5, 0));
            else
                return;
            if (e.getPassengers().isEmpty())
                e.remove();
        }
    }

    @EventHandler
    public void onInteract(final PlayerInteractEvent event) {
        final Block b = event.getClickedBlock();
        if (event.getItem() != null || b == null || event.getAction().isLeftClick() || !event.getPlayer().hasPermission("OpenSit.SitClick"))
            return;
        final Location l = b.getLocation();
        final Block b2 = b.getWorld().getBlockAt(l.getBlockX(), l.getBlockY() + 1, l.getBlockZ());
        if (b2.isSolid() && !(b2.getState().getBlockData() instanceof TrapDoor))
            return;
        final BlockData d = b.getState().getBlockData();
        if (d instanceof Slab || d instanceof Stairs) {
            final boolean t = d instanceof Slab ? ((Slab) d).getType() != Slab.Type.BOTTOM : ((Stairs) d).getHalf() == Bisected.Half.TOP;
            final BlockDisplay r = (BlockDisplay) l.getWorld().spawnEntity(new Location(l.getWorld(), l.getX() + .5, l.getY() + (t ? .8 : .3), l.getZ() + .5), EntityType.BLOCK_DISPLAY);
            r.addScoreboardTag(d instanceof Slab || t ? "sit" : "sit2");
            r.addPassenger(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Entity e = event.getPlayer().getVehicle();
        if (e instanceof BlockDisplay && (e.getScoreboardTags().contains("sit") || e.getScoreboardTags().contains("sit2"))) {
            final Location l = new Location(e.getWorld(), e.getLocation().getX(), e.getLocation().getY() + .2, e.getLocation().getZ());
            final Block b = l.getWorld().getBlockAt(l.getBlockX(), l.getBlockY() + 1, l.getBlockZ());
            if (!l.getBlock().isSolid() || (b.isSolid() && !(b.getState().getBlockData() instanceof TrapDoor)))
                e.remove();
        }
    }

    @EventHandler
    public void onExplodeBlock(final BlockExplodeEvent event) {
        for (final Block b : event.blockList())
            removeSitsInBlock(b.getLocation());
    }

    @EventHandler
    public void onExplodeBlock(final EntityExplodeEvent event) {
        for (final Block b : event.blockList())
            removeSitsInBlock(b.getLocation());
    }

    @EventHandler
    public void onPistonRetract(final BlockPistonRetractEvent event) {
        final Vector d = event.getDirection().getDirection();
        final ArrayList<Entity> dl = new ArrayList<>();
        for (final Block b : event.getBlocks()) {
            final Location l = b.getLocation();
            final double sy = l.getBlockY() - .1, ey = l.getBlockY() + 1;
            final int sx = l.getBlockX(), sz = l.getBlockZ(), ex = sx + 1, ez = sz + 1;
            for (final Entity e : l.getChunk().getEntities())
                if (e instanceof BlockDisplay && (e.getScoreboardTags().contains("sit") || e.getScoreboardTags().contains("sit2")) && !dl.contains(e)) {
                    final Location l2 = e.getLocation();
                    if (
                            l2.getY() > sy && l2.getY() <= ey &&
                            l2.getX() >= sx && l2.getX() <= ex &&
                            l2.getZ() >= sz && l2.getZ() <= ez
                    ) {
                        // Move isn't working during the event
                        dl.add(e);
                        final Block b2 = l.clone().add(d.getX(), d.getY() + 1, d.getZ()).getBlock();
                        if (b2.isSolid() && !(b.getState().getBlockData() instanceof TrapDoor))
                            e.remove();
                        else {
                            final List<Entity> lp = e.getPassengers();
                            if (!lp.isEmpty()) {
                                final BlockDisplay r = (BlockDisplay) l.getWorld().spawnEntity(l2.add(d), EntityType.BLOCK_DISPLAY);
                                for (final String s : e.getScoreboardTags())
                                    r.addScoreboardTag(s);
                                for (final Entity p : lp)
                                    r.addPassenger(p);
                                dl.add(r);
                            }
                        }
                    }
                }
        }
    }

    @EventHandler
    public void onPistonExtend(final BlockPistonExtendEvent event) {
        final Vector d = event.getDirection().getDirection();
        final ArrayList<Entity> dl = new ArrayList<>();
        for (final Block b : event.getBlocks()) {
            final Location l = b.getLocation();
            if (!(b.getState().getBlockData() instanceof TrapDoor))
                removeSitsInBlock(l.clone().subtract(0, 1, 0));
            final double sy = l.getBlockY() - .1, ey = l.getBlockY() + 1;
            final int sx = l.getBlockX(), sz = l.getBlockZ(), ex = sx + 1, ez = sz + 1;
            for (final Entity e : l.getChunk().getEntities())
                if (e instanceof BlockDisplay && (e.getScoreboardTags().contains("sit") || e.getScoreboardTags().contains("sit2")) && !dl.contains(e)) {
                    final Location l2 = e.getLocation();
                    if (
                            l2.getY() > sy && l2.getY() <= ey &&
                            l2.getX() >= sx && l2.getX() <= ex &&
                            l2.getZ() >= sz && l2.getZ() <= ez
                    ) {
                        // Move isn't working during the event
                        dl.add(e);
                        final Block b2 = l.clone().add(d.getX(), d.getY() + 1, d.getZ()).getBlock();
                        if (b2.isSolid() && !(b.getState().getBlockData() instanceof TrapDoor))
                            e.remove();
                        else {
                            final List<Entity> lp = e.getPassengers();
                            if (!lp.isEmpty()) {
                                final BlockDisplay r = (BlockDisplay) l.getWorld().spawnEntity(l2.add(d), EntityType.BLOCK_DISPLAY);
                                for (final String s : e.getScoreboardTags())
                                    r.addScoreboardTag(s);
                                for (final Entity p : lp)
                                    r.addPassenger(p);
                                dl.add(r);
                            }
                        }
                    }
                }
        }
    }

    @EventHandler public void onBreakBlock(final BlockBreakEvent event) { removeSitsInBlock(event.getBlock().getLocation()); }
}