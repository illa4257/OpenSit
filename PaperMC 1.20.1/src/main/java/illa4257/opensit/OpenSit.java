package illa4257.opensit;

import org.bukkit.plugin.java.JavaPlugin;

public final class OpenSit extends JavaPlugin {
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new OpenSitListener(), this);
        getCommand("sit").setExecutor(new SitCommand());
    }
}
