package com.github.zandy.playerborderapi.versionsupport;

import com.github.zandy.playerborderapi.api.PlayerBorderAPI;
import com.github.zandy.playerborderapi.api.PlayerBorderAPI.BorderColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public abstract class VersionSupport {
    private static VersionSupport instance = null;

    public abstract void sendBorder(Player player, BorderColor borderColor, int size, int centerX, int centerZ);

    public void remove(Player player) {
        sendBorder(player, BorderColor.BLUE, Integer.MAX_VALUE, 0, 0);
        if (!PlayerBorderAPI.getCache().containsKey(player.getUniqueId())) return;
        PlayerBorderAPI.getCache().get(player.getUniqueId()).cancel();
        PlayerBorderAPI.getCache().remove(player.getUniqueId());
    }

    @SuppressWarnings("deprecation")
    public static VersionSupport getInstance() {
        if (instance == null) {
            String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            try {
                Class<?> versionClass = Class.forName("com.github.zandy.playerborderapi.versionsupport." + version);
                instance = (VersionSupport) versionClass.newInstance();
            } catch (Exception ignored) {
                Bukkit.getLogger().severe("PlayerBorderAPI is not supported on " + version);
            }
        }
        return instance;
    }
}