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

    public static VersionSupport getInstance() {
        if (instance == null) {
            String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            switch (version) {
                case "v1_8_R3": instance = new v1_8_R3(); break;
                case "v1_9_R2": instance = new v1_9_R2(); break;
                case "v1_10_R1": instance = new v1_10_R1(); break;
                case "v1_11_R1": instance = new v1_11_R1(); break;
                case "v1_12_R1": instance = new v1_12_R1(); break;
                case "v1_13_R2": instance = new v1_13_R2(); break;
                case "v1_14_R1": instance = new v1_14_R1(); break;
                case "v1_15_R1": instance = new v1_15_R1(); break;
                case "v1_16_R3": instance = new v1_16_R3(); break;
                case "v1_17_R1": instance = new v1_17_R1(); break;
                case "v1_18_R1": instance = new v1_18_R1(); break;
                default: Bukkit.getLogger().severe("PlayerBorderAPI is not supported on " + version); break;
            }
        }
        return instance;
    }
}