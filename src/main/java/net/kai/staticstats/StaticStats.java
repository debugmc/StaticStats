package net.kai.staticstats;

import net.kai.staticstats.commands.StatsCommand;
import net.kai.staticstats.menu.StatsMenu;
import org.bukkit.plugin.java.JavaPlugin;

public final class StaticStats extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        new StatsMenu(this);
        new StatsCommand(this).register();


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
