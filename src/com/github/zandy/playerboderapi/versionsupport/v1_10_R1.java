package com.github.zandy.playerboderapi.versionsupport;

import com.github.zandy.playerboderapi.api.PlayerBorderAPI;
import com.github.zandy.playerboderapi.api.PlayerBorderAPI.BorderColor;
import net.minecraft.server.v1_10_R1.PacketPlayOutWorldBorder;
import net.minecraft.server.v1_10_R1.PacketPlayOutWorldBorder.EnumWorldBorderAction;
import net.minecraft.server.v1_10_R1.PlayerConnection;
import net.minecraft.server.v1_10_R1.WorldBorder;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

public class v1_10_R1 extends VersionSupport {

    @Override
    public void sendBorder(Player player, BorderColor borderColor, int size, int centerX, int centerZ) {
        WorldBorder worldBorder = new WorldBorder();
        worldBorder.setCenter(centerX, centerZ);
        worldBorder.setSize(size);
        worldBorder.setWarningDistance(0);
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
        sendPackets(playerConnection, worldBorder);
        if (!(borderColor.equals(BorderColor.RED) || borderColor.equals(BorderColor.GREEN))) return;
        PlayerBorderAPI.getCache().put(player.getUniqueId(), new BukkitRunnable() {
            @Override
            public void run() {
                if (PlayerBorderAPI.getInstance().hasBorder(player.getUniqueId())) {
                    worldBorder.transitionSizeBetween(borderColor.formatSize(size), borderColor.formatSizeTo(size), 20000000);
                    playerConnection.sendPacket(new PacketPlayOutWorldBorder(worldBorder, EnumWorldBorderAction.LERP_SIZE));
                }
            }
        }.runTaskTimerAsynchronously(PlayerBorderAPI.getInstance().getJavaPlugin(), 120, 120));
    }

    private void sendPackets(PlayerConnection playerConnection, WorldBorder worldBorder) {
        for (EnumWorldBorderAction action : Arrays.asList(EnumWorldBorderAction.SET_SIZE, EnumWorldBorderAction.SET_CENTER, EnumWorldBorderAction.SET_WARNING_BLOCKS)) playerConnection.sendPacket(new PacketPlayOutWorldBorder(worldBorder, action));
    }
}