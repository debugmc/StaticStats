package org.staticstudios.commands;

import org.staticstudios.StaticStats;
import org.staticstudios.manager.StatsManager;
import org.staticstudios.utility.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public final class SetStatsCommand implements CommandExecutor, TabCompleter {

    private final StaticStats plugin;
    private final StatsManager statsManager;
    private static final Map<String, String> ALIASES = new HashMap<>();

    static {
        ALIASES.put("kills", "PLAYER_KILLS");
        ALIASES.put("player_kills", "PLAYER_KILLS");
        ALIASES.put("deaths", "DEATHS");
        ALIASES.put("playtime", "PLAY_ONE_MINUTE");
        ALIASES.put("time_played", "PLAY_ONE_MINUTE");
        ALIASES.put("walk", "WALK_ONE_CM");
        ALIASES.put("walk_one_cm", "WALK_ONE_CM");
        ALIASES.put("walk_meter", "WALK_ONE_CM");
        ALIASES.put("sprint", "SPRINT_ONE_CM");
        ALIASES.put("sprint_one_cm", "SPRINT_ONE_CM");
        ALIASES.put("swim", "SWIM_ONE_CM");
        ALIASES.put("swim_one_cm", "SWIM_ONE_CM");
        ALIASES.put("fly", "FLY_ONE_CM");
        ALIASES.put("fly_one_cm", "FLY_ONE_CM");
        ALIASES.put("fall", "FALL_ONE_CM");
        ALIASES.put("fall_one_cm", "FALL_ONE_CM");
        ALIASES.put("climb", "CLIMB_ONE_CM");
        ALIASES.put("climb_one_cm", "CLIMB_ONE_CM");
        ALIASES.put("jump", "JUMP");
        ALIASES.put("mob_kills", "MOB_KILLS");
        ALIASES.put("animals_bred", "ANIMALS_BRED");
        ALIASES.put("fish_caught", "FISH_CAUGHT");
        ALIASES.put("damage_dealt", "DAMAGE_DEALT");
        ALIASES.put("damage_taken", "DAMAGE_TAKEN");
        ALIASES.put("damage_block", "DAMAGE_BLOCK");
        ALIASES.put("damage_entity", "DAMAGE_ENTITY");
        ALIASES.put("mine_block", "MINE_BLOCK");
        ALIASES.put("place_block", "PLACE_BLOCK");
        ALIASES.put("break_item", "BREAK_ITEM");
        ALIASES.put("use_item", "USE_ITEM");
        ALIASES.put("open_chest", "OPEN_CHEST");
        ALIASES.put("open_enderchest", "OPEN_ENDERCHEST");
        ALIASES.put("pickup", "PICKUP");
        ALIASES.put("drop", "DROP");
        ALIASES.put("interact", "INTERACT");
        ALIASES.put("interact_at_entity", "INTERACT_AT_ENTITY");
        ALIASES.put("kill_entity", "KILL_ENTITY");
        ALIASES.put("pvp_kills", "PLAYER_KILLS");
        ALIASES.put("pve_kills", "MOB_KILLS");
        ALIASES.put("time_since_death", "TIME_SINCE_DEATH");
        ALIASES.put("use_cake_slice", "USE_CAKE");
        ALIASES.put("sleep_in_bed", "SLEEP_IN_BED");
        ALIASES.put("trigger_trapped_chest", "TRIGGER_TRAPPED_CHEST");
        ALIASES.put("leave_game", "LEAVE_GAME");
        ALIASES.put("balance", "balance");
        ALIASES.put("kd", "kd");
    }

    public SetStatsCommand(StaticStats plugin, StatsManager statsManager) {
        this.plugin = plugin;
        this.statsManager = statsManager;
    }

    public void register() {
        if (plugin.getCommand("setstats") != null) {
            plugin.getCommand("setstats").setExecutor(this);
            plugin.getCommand("setstats").setTabCompleter(this);
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

        String basePerm = cfg("permissions.setstats.permission", "stats.set");
        String othersPerm = cfg("permissions.setstats.others-permission", "stats.set.others");

        String prefix = ColorUtil.color(msg("prefix", "&#55FB00&lSTATS &7➠ "));

        if (!sender.hasPermission(basePerm)) {
            sender.sendMessage(prefix + ColorUtil.color(msg("no-permission", "&cYou don't have permission.")));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(prefix + ColorUtil.color(msg("usage", "&cUsage: /setstats <player> <stat> <value>")));
            return true;
        }

        String targetName = args[0];
        String statKey = args[1].toLowerCase(Locale.ROOT);
        String valueStr = args[2];

        OfflinePlayer targetOff = Bukkit.getOfflinePlayer(targetName);
        if (targetOff == null || targetOff.getName() == null) {
            sender.sendMessage(prefix + ColorUtil.color(msg("player-not-found", "&cPlayer not found.")));
            return true;
        }

        boolean isOther = !(sender instanceof Player p && p.getName().equalsIgnoreCase(targetOff.getName()));
        if (isOther && !sender.hasPermission(othersPerm)) {
            sender.sendMessage(prefix + ColorUtil.color(msg("no-permission-others", "&cYou cannot set other players' stats.")));
            return true;
        }

        long valueLong;
        double valueDouble;
        try {
            if (statKey.equals("kd")) {
                valueDouble = Double.parseDouble(valueStr);
            } else {
                valueLong = Long.parseLong(valueStr);
                valueDouble = valueLong;
            }
        } catch (NumberFormatException ex) {
            sender.sendMessage(prefix + ColorUtil.color(msg("invalid-number", "&cInvalid number: %value%")).replace("%value%", valueStr));
            return true;
        }

        if (statKey.equals("balance")) {
            setBalance(targetOff, valueDouble);
            sender.sendMessage(prefix + ColorUtil.color(msg("stat-set", "&aSet %stat% for %player% to %value%"))
                    .replace("%stat%", "balance")
                    .replace("%player%", targetOff.getName())
                    .replace("%value%", String.valueOf(valueDouble)));
            return true;
        }

        if (statKey.equals("kd")) {
            setKd(targetOff, valueDouble);
            sender.sendMessage(prefix + ColorUtil.color(msg("stat-set", "&aSet %stat% for %player% to %value%"))
                    .replace("%stat%", "kd")
                    .replace("%player%", targetOff.getName())
                    .replace("%value%", String.valueOf(valueDouble)));
            return true;
        }

        String mapped = ALIASES.getOrDefault(statKey, statKey.toUpperCase(Locale.ROOT));
        Statistic statistic = null;
        try {
            statistic = Statistic.valueOf(mapped);
        } catch (IllegalArgumentException ignored) {
        }

        if (statistic != null && targetOff.isOnline()) {
            Player online = (Player) targetOff;
            try {
                int safeVal = (int) Math.max(0, Math.min(Integer.MAX_VALUE, Math.floor(valueDouble)));
                online.setStatistic(statistic, safeVal);
                sender.sendMessage(prefix + ColorUtil.color(msg("stat-set", "&aSet %stat% for %player% to %value%"))
                        .replace("%stat%", statKey)
                        .replace("%player%", online.getName())
                        .replace("%value%", String.valueOf(safeVal)));
                return true;
            } catch (Exception ignored) {
            }
        }

        saveStoredStat(targetOff.getUniqueId().toString(), statKey, valueDouble);

        sender.sendMessage(prefix + ColorUtil.color(msg("stat-saved-offline", "&aSaved %stat% for %player% (will apply when available)."))
                .replace("%stat%", statKey)
                .replace("%player%", targetOff.getName()));
        return true;
    }

    private void setBalance(OfflinePlayer target, double amount) {
        plugin.getConfig().set("balances." + target.getUniqueId(), amount);
        plugin.saveConfig();
    }

    private void setKd(OfflinePlayer target, double targetKd) {
        if (targetKd < 0) targetKd = 0;
        if (target.isOnline()) {
            Player p = (Player) target;
            int kills = 0;
            int deaths = 0;
            try {
                kills = p.getStatistic(Statistic.PLAYER_KILLS);
                deaths = p.getStatistic(Statistic.DEATHS);
            } catch (Exception ignored) {}
            if (targetKd == 0) {
                deaths = Math.max(0, kills);
            } else {
                deaths = (int) Math.max(0, Math.round(kills / targetKd));
            }
            try { p.setStatistic(Statistic.DEATHS, deaths); } catch (Exception ignored) {}
        } else {
            saveStoredStat(target.getUniqueId().toString(), "kd", targetKd);
        }
    }

    private void saveStoredStat(String uuid, String key, double value) {
        plugin.getConfig().set("stored-stats." + uuid + "." + key, value);
        plugin.saveConfig();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String basePerm = cfg("permissions.setstats.permission", "stats.set");
        String othersPerm = cfg("permissions.setstats.others-permission", "stats.set.others");

        if (args.length == 1) {
            if (sender instanceof Player p && !sender.hasPermission(othersPerm)) {
                String name = p.getName();
                if (name.toLowerCase(Locale.ROOT).startsWith(args[0].toLowerCase(Locale.ROOT))) return List.of(name);
                return Collections.emptyList();
            }
            String cur = args[0].toLowerCase(Locale.ROOT);
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(cur))
                    .sorted()
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String cur = args[1].toLowerCase(Locale.ROOT);
            Set<String> suggestions = new HashSet<>();
            suggestions.addAll(ALIASES.keySet());
            for (Statistic s : Statistic.values()) suggestions.add(s.name().toLowerCase(Locale.ROOT));
            suggestions.addAll(ALIASES.values().stream().map(v -> v.toLowerCase(Locale.ROOT)).collect(Collectors.toSet()));
            return suggestions.stream()
                    .filter(s -> s.startsWith(cur))
                    .sorted()
                    .collect(Collectors.toList());
        }

        if (args.length == 3) {
            String cur = args[2].toLowerCase(Locale.ROOT);
            List<String> nums = Arrays.asList("0", "1", "5", "10", "50", "100", "1000", "10000");
            return nums.stream().filter(n -> n.startsWith(cur)).collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
