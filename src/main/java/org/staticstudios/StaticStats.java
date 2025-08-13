package org.staticstudios;

import org.staticstudios.commands.SetStatsCommand;
import org.staticstudios.commands.StatsCommand;
import org.staticstudios.manager.StatsManager;
import org.staticstudios.menu.StatsMenu;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class StaticStats extends JavaPlugin {

    private StatsMenu statsMenu;
    private StatsCommand statsCommand;
    private SetStatsCommand setStatsCommand;
    private StatsManager statsManager;

    private File messagesFile;
    private FileConfiguration messages;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadMessages();

        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage("§r");
        Bukkit.getConsoleSender().sendMessage("§7Thanks for downloading §aStatic Stats§7.");
        Bukkit.getConsoleSender().sendMessage("§7This plugin is free to use.");
        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage("§7Server IP: §f" + Bukkit.getIp());
        Bukkit.getConsoleSender().sendMessage("§7Server Port: §f" + Bukkit.getPort());
        Bukkit.getConsoleSender().sendMessage("§r");

        statsManager = new StatsManager();
        statsMenu = new StatsMenu(this);

        statsCommand = new StatsCommand(this, statsMenu);
        statsCommand.register();

        setStatsCommand = new SetStatsCommand(this, statsManager);
        setStatsCommand.register();
    }

    @Override
    public void onDisable() {
    }

    private void loadMessages() {
        messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) saveResource("messages.yml", false);
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public FileConfiguration getMessages() {
        if (messages == null) loadMessages();
        return messages;
    }

    public void reloadMessages() {
        if (messagesFile == null) messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) saveResource("messages.yml", false);
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        getLogger().info("messages.yml reloaded.");
    }
}
