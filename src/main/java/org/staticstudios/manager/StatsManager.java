package org.staticstudios.manager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Objects;

public final class StatsManager {

    private final Map<UUID, StatsData> stats = new ConcurrentHashMap<>();

    public StatsData getStats(UUID playerId) {
        Objects.requireNonNull(playerId);
        return stats.computeIfAbsent(playerId, this::loadStats);
    }

    public void resetStats(UUID playerId) {
        Objects.requireNonNull(playerId);
        StatsData zero = new StatsData(0, 0, 0L);
        stats.put(playerId, zero);
        saveStatsAsync(playerId, zero);
    }

    public StatsData incrementKills(UUID playerId, int amount) {
        Objects.requireNonNull(playerId);
        return stats.compute(playerId, (id, old) -> {
            StatsData base = old == null ? loadStats(id) : old;
            StatsData updated = base.addKills(amount);
            saveStatsAsync(id, updated);
            return updated;
        });
    }

    public StatsData incrementDeaths(UUID playerId, int amount) {
        Objects.requireNonNull(playerId);
        return stats.compute(playerId, (id, old) -> {
            StatsData base = old == null ? loadStats(id) : old;
            StatsData updated = base.addDeaths(amount);
            saveStatsAsync(id, updated);
            return updated;
        });
    }

    public StatsData addPlaytime(UUID playerId, long seconds) {
        Objects.requireNonNull(playerId);
        return stats.compute(playerId, (id, old) -> {
            StatsData base = old == null ? loadStats(id) : old;
            StatsData updated = base.addPlaytime(seconds);
            saveStatsAsync(id, updated);
            return updated;
        });
    }

    public void unloadStats(UUID playerId) {
        Objects.requireNonNull(playerId);
        stats.remove(playerId);
    }

    public CompletableFuture<Void> saveStatsAsync(UUID playerId, StatsData data) {
        Objects.requireNonNull(playerId);
        Objects.requireNonNull(data);
        return CompletableFuture.runAsync(() -> saveStats(playerId, data));
    }

    protected void saveStats(UUID playerId, StatsData data) {
    }

    private StatsData loadStats(UUID playerId) {
        return new StatsData(0, 0, 0L);
    }

    public static final class StatsData {
        private final int kills;
        private final int deaths;
        private final long playtime;

        public StatsData(int kills, int deaths, long playtime) {
            this.kills = Math.max(0, kills);
            this.deaths = Math.max(0, deaths);
            this.playtime = Math.max(0L, playtime);
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
            if (deaths == 0) return (double) kills;
            return (double) kills / deaths;
        }

        public StatsData withKills(int kills) {
            return new StatsData(kills, this.deaths, this.playtime);
        }

        public StatsData withDeaths(int deaths) {
            return new StatsData(this.kills, deaths, this.playtime);
        }

        public StatsData withPlaytime(long playtime) {
            return new StatsData(this.kills, this.deaths, playtime);
        }

        public StatsData addKills(int amount) {
            return new StatsData(this.kills + Math.max(0, amount), this.deaths, this.playtime);
        }

        public StatsData addDeaths(int amount) {
            return new StatsData(this.kills, this.deaths + Math.max(0, amount), this.playtime);
        }

        public StatsData addPlaytime(long seconds) {
            return new StatsData(this.kills, this.deaths, this.playtime + Math.max(0L, seconds));
        }
    }
}
