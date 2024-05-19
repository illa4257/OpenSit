package io.github.illa4257.opensit;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public final class OpenSit extends JavaPlugin {
    public final OpenSitListener listener = new OpenSitListener();

    @Override
    public void onEnable() {
        listener.register(this);
        getCommand("sit").setExecutor(new SitCommand());
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }
}
