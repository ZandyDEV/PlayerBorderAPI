package com.github.zandy.playerborderapi.versionsupport;

import com.github.zandy.playerborderapi.api.PlayerBorderAPI;
import com.github.zandy.playerborderapi.api.PlayerBorderAPI.BorderColor;
import net.minecraft.server.v1_16_R3.PacketPlayOutWorldBorder;
import net.minecraft.server.v1_16_R3.PacketPlayOutWorldBorder.EnumWorldBorderAction;
import net.minecraft.server.v1_16_R3.PlayerConnection;
import net.minecraft.server.v1_16_R3.WorldBorder;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

public class v1_16_R3 extends VersionSupport {

    @Override
    public void sendBorder(Player player, BorderColor borderColor, int size, int centerX, int centerZ) {
        WorldBorder worldBorder = new WorldBorder();
        worldBorder.setCenter(centerX, centerZ);
        worldBorder.setSize(size);
        worldBorder.setWarningDistance(0);
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
        worldBorder.world = (WorldServer) ((CraftPlayer) player).getHandle().world;
        sendPackets(playerConnection, worldBorder);
        if (!(borderColor.equals(BorderColor.RED) || borderColor.equals(BorderColor.GREEN))) return;
        PlayerBorderAPI.getCache().put(player.getUniqueId(), new BukkitRunnable() {
            @Override
            public void run() {
                worldBorder.transitionSizeBetween(borderColor.formatSize(size), borderColor.formatSizeTo(size), 20000000);
                playerConnection.sendPacket(new PacketPlayOutWorldBorder(worldBorder, EnumWorldBorderAction.LERP_SIZE));
            }
        }.runTaskTimerAsynchronously(PlayerBorderAPI.getInstance().getJavaPlugin(), 0, 120));
    }

    private void sendPackets(PlayerConnection playerConnection, WorldBorder worldBorder) {
        for (EnumWorldBorderAction action : Arrays.asList(EnumWorldBorderAction.SET_SIZE, EnumWorldBorderAction.SET_CENTER, EnumWorldBorderAction.SET_WARNING_BLOCKS)) playerConnection.sendPacket(new PacketPlayOutWorldBorder(worldBorder, action));
    }
}