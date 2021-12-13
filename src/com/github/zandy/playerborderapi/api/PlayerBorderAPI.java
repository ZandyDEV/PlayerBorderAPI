package com.github.zandy.playerborderapi.api;

import com.github.zandy.playerborderapi.versionsupport.VersionSupport;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.UUID;

public class PlayerBorderAPI {
    public enum BorderColor {
        RED(0, 1), GREEN(0.2, 0.1), BLUE;

        double size, sizeTo;

        BorderColor(double size, double sizeTo) {
            this.size = size;
            this.sizeTo = sizeTo;
        }

        BorderColor() {}

        public double formatSize(int size) {
            return size - this.size;
        }

        public double formatSizeTo(int size) {
            return size - this.sizeTo;
        }
    }
    private static final PlayerBorderAPI instance = new PlayerBorderAPI();
    private static final HashMap<UUID, BukkitTask> cache = new HashMap<>();
    private JavaPlugin javaPlugin;

    private PlayerBorderAPI() {}

    public void setJavaPlugin(JavaPlugin javaPlugin) {
        this.javaPlugin = javaPlugin;
    }

    public JavaPlugin getJavaPlugin() {
        return javaPlugin;
    }

    public boolean hasBorder(UUID uuid) {
        return getCache().containsKey(uuid);
    }

    public void removeBorder(Player player) {
        VersionSupport.getInstance().remove(player);
    }

    public void setBorder(Player player, BorderColor borderColor, int borderSize, int borderX, int borderZ) {
        VersionSupport.getInstance().remove(player);
        VersionSupport.getInstance().sendBorder(player, borderColor, borderSize, borderX, borderZ);
    }

    public void setBorder(Player player, BorderColor borderColor, int borderSize, int borderX, int borderZ, int seconds) {
        VersionSupport.getInstance().sendBorder(player, borderColor, borderSize, borderX, borderZ);
        new BukkitRunnable() {
            @Override
            public void run() {
                removeBorder(player);
            }
        }.runTaskLater(getJavaPlugin(), seconds * 20L);
    }

    public void removeBorders() {
        for (UUID uuid : getCache().keySet()) removeBorder(Bukkit.getPlayer(uuid));
    }

    public static PlayerBorderAPI getInstance() {
        return instance;
    }

    public static HashMap<UUID, BukkitTask> getCache() {
        return cache;
    }
}
