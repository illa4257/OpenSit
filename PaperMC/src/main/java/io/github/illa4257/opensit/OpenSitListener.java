package io.github.illa4257.opensit;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.util.Vector;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class OpenSitListener implements Listener {
    public static boolean isSit(final Entity e) {
        return e instanceof BlockDisplay && (e.getScoreboardTags().contains("sit") || e.getScoreboardTags().contains("sit2"));
    }

    public interface RunnableBlockArg {
        void run(final Entity entity);
    }

    public static void eachSit(final Location l, final RunnableBlockArg r) {
        final double sy = l.getBlockY() - .1, ey = l.getBlockY() + 1;
        final int sx = l.getBlockX(), sz = l.getBlockZ(), ex = sx + 1, ez = sz + 1;
        for (final Entity e : l.getChunk().getEntities())
            if (isSit(e)) {
                final Location l2 = e.getLocation();
                if (
                        l2.getY() > sy && l2.getY() <= ey &&
                                l2.getX() >= sx && l2.getX() <= ex &&
                                l2.getZ() >= sz && l2.getZ() <= ez
                ) r.run(e);
            }
    }

    public static void removeSitsInBlock(final Location l) {
        final double sy = l.getBlockY() - .1, ey = l.getBlockY() + 1;
        final int sx = l.getBlockX(), sz = l.getBlockZ(), ex = sx + 1, ez = sz + 1;
        for (final Entity e : l.getChunk().getEntities())
            if (isSit(e)) {
                final Location l2 = e.getLocation();
                if (
                        l2.getY() > sy && l2.getY() <= ey &&
                                l2.getX() >= sx && l2.getX() <= ex &&
                                l2.getZ() >= sz && l2.getZ() <= ez
                ) e.remove();
            }
    }

    public final Class<Event> dismountEvent;
    public final double offset, offsetHalf, offsetFull;

    @SuppressWarnings("unchecked")
    public OpenSitListener(final OpenSit plugin) {
        Class<Event> cl = null;
        double o = 0;
        try {
            cl = (Class<Event>) Class.forName("org.bukkit.event.entity.EntityDismountEvent");
            o = 0.2;
        } catch (final Exception ex) {
            try {
                plugin.getLogger().info("Old server, I will use another event.");
                cl = (Class<Event>) Class.forName("org.spigotmc.event.entity.EntityDismountEvent");
            } catch (final Exception ex1) {
                plugin.getLogger().warning(ex.toString());
            }
        }
        dismountEvent = cl;
        offset = o;
        offsetHalf = offset + .3;
        offsetFull = offsetHalf + .5;
    }

    public void register(final OpenSit plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        final Method md, me;
        {
            Method d = null, e = null;

            if (dismountEvent != null)
                try {
                    d = dismountEvent.getMethod("getDismounted");
                    e = dismountEvent.getMethod("getEntity");
                } catch (final Exception ex) {
                    plugin.getLogger().warning(ex.toString());
                }

            md = d;
            me = e;
        }

        plugin.getServer().getPluginManager().registerEvent(dismountEvent, this, EventPriority.LOWEST, (listener, event) -> {
            if (listener != this)
                return;
            if (!dismountEvent.isInstance(event))
                return;
            try {
                final Entity d = (Entity) md.invoke(event);
                final Entity e = (Entity) me.invoke(event);

                if (d instanceof BlockDisplay) {
                    if (d.getScoreboardTags().contains("sit"))
                        e.teleport(e.getLocation().add(0, 1, 0));
                    else if (d.getScoreboardTags().contains("sit2"))
                        e.teleport(e.getLocation().add(0, 1.5, 0));
                    else
                        return;
                    final int s = d.getPassengers().size();
                    if ((s == 1 && d.getPassengers().contains(e)) || s == 0)
                        d.remove();
                }
            } catch (final Exception ex) {
                plugin.getLogger().warning(ex.toString());
            }
        }, plugin, true);
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
            final BlockDisplay r = (BlockDisplay) l.getWorld().spawnEntity(new Location(l.getWorld(), l.getX() + .5, l.getY() + (t ? offsetFull : offsetHalf), l.getZ() + .5), EntityType.BLOCK_DISPLAY);
            r.addScoreboardTag(d instanceof Slab || t ? "sit" : "sit2");
            r.addPassenger(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Entity e = event.getPlayer().getVehicle();
        if (isSit(e)) {
            final Location l = new Location(e.getWorld(), e.getLocation().getX(), e.getLocation().getY() + .2, e.getLocation().getZ());
            final Block b = l.getWorld().getBlockAt(l.getBlockX(), l.getBlockY() + 1, l.getBlockZ());
            if (
                    (!l.getBlock().isSolid() && !l.getWorld().getBlockAt(l.getBlockX(), l.getBlockY(), l.getBlockZ()).isSolid()) ||
                            (b.isSolid() && !(b.getState().getBlockData() instanceof TrapDoor)))
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

    public void moved(final Vector d, final List<Block> bl) {
        final ArrayList<Entity> el = new ArrayList<>();
        for (final Block b : bl) {
            final Location
                    tl = b.getLocation().clone().add(d),
                    ul = tl.clone().subtract(0, 1, 0),
                    ul2 = tl.clone().add(0, 1, 0)
            ;
            if (!bl.contains(ul.getBlock()) && b.isSolid())
                removeSitsInBlock(ul);
            if (!bl.contains(ul2.getBlock()) && ul2.getBlock().isSolid()) {
                removeSitsInBlock(b.getLocation());
                continue;
            }
            eachSit(b.getLocation(), e -> {
                if (el.contains(e))
                    return;
                el.add(e);
                final List<Entity> lp = e.getPassengers();
                if (!lp.isEmpty()) {
                    final BlockDisplay r = (BlockDisplay) b.getWorld().spawnEntity(e.getLocation().add(d), EntityType.BLOCK_DISPLAY);
                    for (final String s : e.getScoreboardTags())
                        r.addScoreboardTag(s);
                    for (final Entity p : lp)
                        r.addPassenger(p);
                    el.add(r);
                }
                e.remove();
            });
        }
    }

    @EventHandler
    public void onPistonRetract(final BlockPistonRetractEvent event) {
        moved(event.getDirection().getDirection(), event.getBlocks());
    }

    @EventHandler
    public void onPistonExtend(final BlockPistonExtendEvent event) {
        if (event.getDirection().getDirection().getY() == 0 && event.getBlocks().isEmpty())
            removeSitsInBlock(event.getBlock().getLocation().clone().add(event.getDirection().getDirection()));
        else if (event.getDirection().getDirection().getY() < 0 && event.getBlocks().isEmpty())
            removeSitsInBlock(event.getBlock().getLocation().clone()
                            .subtract(0, 2, 0)
                    //.add(event.getDirection().getDirection()).subtract(0, 1, 0)
            );
        moved(event.getDirection().getDirection(), event.getBlocks());
    }

    @EventHandler
    public void onBreakBlock(final BlockBreakEvent event) { removeSitsInBlock(event.getBlock().getLocation()); }
}