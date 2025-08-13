package org.staticstudios.menu;

import org.staticstudios.utility.ColorUtil;
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
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class StatsMenu {

    private final JavaPlugin plugin;
    private static final InventoryHolder HOLDER = new InventoryHolder() {
        @Override
        public Inventory getInventory() {
            return null;
        }
    };

    public StatsMenu(JavaPlugin plugin) {
        this.plugin = plugin;
        registerListener();
    }

    public void open(Player viewer, OfflinePlayer target) {
        ConfigurationSection guiConfig = plugin.getConfig().getConfigurationSection("stats-gui");
        if (guiConfig == null) {
            plugin.getLogger().warning("Stats GUI configuration is missing in config.yml!");
            return;
        }

        String title = ColorUtil.color(guiConfig.getString("title", "ѕᴛᴀᴛѕ"));
        int size = Math.max(9, Math.min(54, guiConfig.getInt("size", 27)));
        Inventory gui = Bukkit.createInventory(HOLDER, size, title);

        ConfigurationSection items = guiConfig.getConfigurationSection("items");
        if (items != null) {
            for (String key : items.getKeys(false)) {
                ConfigurationSection section = items.getConfigurationSection(key);
                if (section == null) continue;

                Material material = Material.matchMaterial(section.getString("material", ""));
                if (material == null) continue;

                ItemStack item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();

                if (material == Material.PLAYER_HEAD && target != null && meta instanceof SkullMeta skullMeta) {
                    skullMeta.setOwningPlayer(target);
                    meta = skullMeta;
                }

                if (meta != null) {
                    meta.setDisplayName(parsePlaceholders(target, section.getString("name", "")));
                    List<String> loreLines = section.getStringList("lore");
                    if (!loreLines.isEmpty()) {
                        List<String> parsed = new ArrayList<>(loreLines.size());
                        for (String line : loreLines) parsed.add(parsePlaceholders(target, line));
                        meta.setLore(parsed);
                    }
                    item.setItemMeta(meta);
                }

                int slot = section.getInt("slot", 0);
                if (slot < 0) slot = 0;
                if (slot >= size) slot = size - 1;
                gui.setItem(slot, item);
            }
        }

        ItemStack fillItem = buildFillItem(target, guiConfig);
        if (fillItem != null) {
            for (int i = 0; i < size; i++) {
                if (gui.getItem(i) == null) gui.setItem(i, fillItem.clone());
            }
        }

        viewer.openInventory(gui);
    }

    private ItemStack buildFillItem(OfflinePlayer target, ConfigurationSection guiConfig) {
        if (guiConfig == null) return null;
        ConfigurationSection fill = guiConfig.getConfigurationSection("items.fill");
        if (fill == null) return null;
        Material material = Material.matchMaterial(fill.getString("material", ""));
        if (material == null) return null;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (material == Material.PLAYER_HEAD && target != null && meta instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(target);
            meta = skullMeta;
        }

        if (meta != null) {
            meta.setDisplayName(parsePlaceholders(target, fill.getString("name", "")));
            List<String> loreLines = fill.getStringList("lore");
            if (!loreLines.isEmpty()) {
                List<String> parsed = new ArrayList<>(loreLines.size());
                for (String line : loreLines) parsed.add(parsePlaceholders(target, line));
                meta.setLore(parsed);
            }
            item.setItemMeta(meta);
        }

        return item;
    }

    private String parsePlaceholders(OfflinePlayer player, String text) {
        if (text == null) return "";
        if (player instanceof Player online) text = PlaceholderAPI.setPlaceholders(online, text);
        String name = player != null ? player.getName() : "Unknown";
        return ColorUtil.color(text.replace("%player_name%", name));
    }

    private void registerListener() {
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onClick(InventoryClickEvent event) {
                if (event.getInventory() != null && event.getInventory().getHolder() == HOLDER) {
                    event.setCancelled(true);
                }
            }
        }, plugin);
    }
}
