package com.github.zandy.playerborderapi.api;

import com.github.zandy.playerborderapi.versionsupport.VersionSupport;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("unused")
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
    @Getter private static final PlayerBorderAPI instance = new PlayerBorderAPI();
    @Getter private static final HashMap<UUID, BukkitTask> cache = new HashMap<>();
    @Getter @Setter private JavaPlugin javaPlugin;

    private PlayerBorderAPI() {}

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

    public void setBorder(Player player, BorderColor borderColor, int borderSize,
                          int borderX, int borderZ, int seconds) {
        VersionSupport.getInstance().sendBorder(player, borderColor, borderSize, borderX, borderZ);
        Bukkit.getScheduler().runTaskLater(getJavaPlugin(), () -> removeBorder(player), seconds * 20L);
    }

    public void removeBorders() {
        getCache().keySet().forEach(uuid -> removeBorder(Bukkit.getPlayer(uuid)));
    }
}