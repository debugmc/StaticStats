package org.staticstudios.commands;

import org.staticstudios.StaticStats;
import org.staticstudios.menu.StatsMenu;
import org.staticstudios.utility.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class StatsCommand implements CommandExecutor, TabCompleter {

    private final StaticStats plugin;
    private final StatsMenu statsMenu;

    public StatsCommand(StaticStats plugin, StatsMenu statsMenu) {
        this.plugin = plugin;
        this.statsMenu = statsMenu;
    }

    public void register() {
        if (plugin.getCommand("stats") != null) {
            plugin.getCommand("stats").setExecutor(this);
            plugin.getCommand("stats").setTabCompleter(this);
        }
    }

    private String cfg(String path, String def) {
        return plugin.getConfig().getString(path, def);
    }

    private String msg(String path, String def) {
        return plugin.getMessages().getString(path, def);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        String reloadPerm = cfg("permissions.stats.reload-permission", "stats.reload");
        String prefix = ColorUtil.color(msg("prefix", "&#55FB00&lSTATS &7➠ "));

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission(reloadPerm)) {
                sender.sendMessage(prefix + ColorUtil.color(msg("no-permission", "&cYou don't have permission to use this command.")));
                return true;
            }
            plugin.reloadConfig();
            plugin.reloadMessages();
            String r = msg("reload", "&aThe configuration has been reloaded.");
            if (sender instanceof Player player) {
                player.sendMessage(prefix + ColorUtil.color(r));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
            } else {
                sender.sendMessage(prefix + ColorUtil.color(r));
            }
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(prefix + ColorUtil.color(msg("only-players", "&cOnly players can use this command.")));
            return true;
        }

        String guiPerm = cfg("permissions.stats.permission", "stats.use");
        if (!player.hasPermission(guiPerm)) {
            player.sendMessage(prefix + ColorUtil.color(msg("no-permission", "&cYou don't have permission to use this command.")));
            return true;
        }

        if (args.length == 0) {
            statsMenu.open(player, player);
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target.hasPlayedBefore() || target.isOnline()) {
            statsMenu.open(player, target);
        } else {
            player.sendMessage(prefix + ColorUtil.color(msg("player-never-joined", "&cThis player never joined before.")));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) return List.of();
        String cur = args[0].toLowerCase();
        Collection<String> base = new ArrayList<>();
        String reloadPerm = cfg("permissions.stats.reload-permission", "stats.reload");
        if (sender.hasPermission(reloadPerm)) base.add("reload");
        for (Player p : plugin.getServer().getOnlinePlayers()) base.add(p.getName());
        return base.stream()
                .filter(s -> s.toLowerCase().startsWith(cur))
                .sorted()
                .collect(Collectors.toList());
    }
}
