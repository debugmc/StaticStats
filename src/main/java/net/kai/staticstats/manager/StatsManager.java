package net.kai.staticstats.manager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class StatsManager {

    private final Map<UUID, StatsData> stats = new ConcurrentHashMap<>();

    public StatsData getStats(UUID playerId) {
        return stats.computeIfAbsent(playerId, this::loadStats);
    }

    public void resetStats(UUID playerId) {
        StatsData newData = new StatsData();
        stats.put(playerId, newData);
        saveStatsAsync(playerId, newData);
    }

    public void saveStats(UUID playerId, StatsData data) {
    }

    public CompletableFuture<Void> saveStatsAsync(UUID playerId, StatsData data) {
        return CompletableFuture.runAsync(() -> saveStats(playerId, data));
    }

    private StatsData loadStats(UUID playerId) {
        return new StatsData();
    }

    public static class StatsData {

        private int kills;
        private int deaths;
        private long playtime;

        public StatsData() {}

        public int getKills() {
            return kills;
        }

        public StatsData setKills(int kills) {
            this.kills = Math.max(0, kills);
            return this;
        }

        public int getDeaths() {
            return deaths;
        }

        public StatsData setDeaths(int deaths) {
            this.deaths = Math.max(0, deaths);
            return this;
        }

        public long getPlaytime() {
            return playtime;
        }

        public StatsData setPlaytime(long playtime) {
            this.playtime = Math.max(0, playtime);
            return this;
        }

        public double getKd() {
            if (deaths == 0) return kills;
            return (double) kills / deaths;
        }

        public StatsData incrementKills(int amount) {
            return setKills(kills + amount);
        }

        public StatsData incrementDeaths(int amount) {
            return setDeaths(deaths + amount);
        }

        public StatsData addPlaytime(long seconds) {
            return setPlaytime(playtime + Math.max(0, seconds));
        }

        public StatsSnapshot snapshot() {
            return new StatsSnapshot(kills, deaths, playtime, getKd());
        }
    }

    public static class StatsSnapshot {
        private final int kills;
        private final int deaths;
        private final long playtime;
        private final double kd;

        public StatsSnapshot(int kills, int deaths, long playtime, double kd) {
            this.kills = kills;
            this.deaths = deaths;
            this.playtime = playtime;
            this.kd = kd;
        }

        public int getKills() {
            return kills;
        }

        public int getDeaths() {
            return deaths;
        }

        public long getPlaytime() {
            return playtime;
        }

        public double getKd() {
            return kd;
        }
    }
}
