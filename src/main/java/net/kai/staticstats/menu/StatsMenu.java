package net.kai.staticstats.menu;

import net.kai.staticstats.utility.ColorUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class StatsMenu {

    private static JavaPlugin plugin;

    public StatsMenu(JavaPlugin plugin) {
        StatsMenu.plugin = plugin;
        registerListener();
    }

    public static void open(Player viewer, OfflinePlayer target) {
        ConfigurationSection guiConfig = plugin.getConfig().getConfigurationSection("stats-gui");
        if (guiConfig == null) {
            plugin.getLogger().warning("Stats GUI configuration is missing in config.yml!");
            return;
        }

        String title = ColorUtil.color(guiConfig.getString("title", "ѕᴛᴀᴛѕ"));
        int size = guiConfig.getInt("size", 27);
        Inventory gui = Bukkit.createInventory(null, size, title);

        ConfigurationSection items = guiConfig.getConfigurationSection("items");
        if (items != null) {
            for (String key : items.getKeys(false)) {
                ConfigurationSection section = items.getConfigurationSection(key);
                if (section == null) continue;

                Material material = Material.matchMaterial(section.getString("material", ""));
                if (material == null) continue;

                ItemStack item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();

                if (material == Material.PLAYER_HEAD && target != null) {
                    SkullMeta skullMeta = (SkullMeta) meta;
                    skullMeta.setOwningPlayer(target);
                    meta = skullMeta;
                }

                if (meta != null) {
                    meta.setDisplayName(parsePlaceholders(target, section.getString("name", "")));
                    List<String> loreLines = new ArrayList<>();
                    for (String line : section.getStringList("lore")) {
                        loreLines.add(parsePlaceholders(target, line));
                    }
                    meta.setLore(loreLines);
                    item.setItemMeta(meta);
                }

                gui.setItem(section.getInt("slot", 0), item);
            }
        }

        viewer.openInventory(gui);
    }

    private static String parsePlaceholders(OfflinePlayer player, String text) {
        if (text == null) return "";
        text = text.replace("%player_name%", player != null ? player.getName() : "Unknown");
        if (player instanceof Player online) {
            return ColorUtil.color(PlaceholderAPI.setPlaceholders(online, text));
        }
        return ColorUtil.color(text);
    }

    private void registerListener() {
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onClick(InventoryClickEvent event) {
                if (event.getInventory().getHolder() == null) {
                    event.setCancelled(true);
                }
            }
        }, plugin);
    }
}
