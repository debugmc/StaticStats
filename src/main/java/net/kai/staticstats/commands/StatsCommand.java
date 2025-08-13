package net.kai.staticstats.commands;

import net.kai.staticstats.menu.StatsMenu;
import net.kai.staticstats.utility.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class StatsCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public StatsCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        plugin.getCommand("stats").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("stats.reload")) {
                sender.sendMessage(ColorUtil.color("&cYou don't have permission to use this command."));
                return true;
            }

            plugin.reloadConfig();

            if (sender instanceof Player player) {
                sender.sendMessage(ColorUtil.color("&#27F573Stats Config Reloaded!"));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            } else {
                sender.sendMessage("Stats Config Reloaded!");
            }

            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length == 0) {
            StatsMenu.open(player, player);
        } else {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            if (target.hasPlayedBefore() || target.isOnline()) {
                StatsMenu.open(player, target);
            } else {
                player.sendMessage(ColorUtil.color("&cThis player never joined before."));
            }
        }

        return true;
    }
}
