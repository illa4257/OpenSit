package illa4257.opensit;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class SitCommand implements CommandExecutor {
    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Command command, final @NotNull String s, final @NotNull String[] strings) {
        if(!(sender instanceof Player)) {
            sender.sendMessage("This command is only for players.");
            return true;
        }
        final Player plr = (Player) sender;
        if (!plr.hasPermission("OpenSit.Sit")) {
            sender.sendMessage("You don't have permission");
            return true;
        }
        if (plr.isInsideVehicle()) {
            sender.sendMessage("You're sitting now.");
            return true;
        }
        if (plr.isSneaking()) {
            sender.sendMessage("You're sneaking now.");
            return true;
        }
        final Location l = plr.getLocation();
        final RayTraceResult r = l.getWorld().rayTraceBlocks(l, new Vector(0, -1, 0), 0.001);
        if (r == null) {
            sender.sendMessage("You do not stand on the ground.");
            return true;
        }
        final BlockDisplay b = (BlockDisplay) l.getWorld().spawnEntity(new Location(l.getWorld(), l.getX(), r.getHitPosition().getY() - .2, l.getZ()), EntityType.BLOCK_DISPLAY);
        b.addScoreboardTag("sit");
        b.addPassenger(plr);
        return true;
    }
}