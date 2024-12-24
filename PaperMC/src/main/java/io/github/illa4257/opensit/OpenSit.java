package io.github.illa4257.opensit;

import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public final class OpenSit extends JavaPlugin {
    public final OpenSitListener listener = new OpenSitListener(this);

    @Override
    public void onEnable() {
        if (listener.dismountEvent == null) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        listener.register(this);
        final PluginCommand sitCommand = getCommand("sit");
        if (sitCommand != null)
            sitCommand.setExecutor(new SitCommand(listener));
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }
}
