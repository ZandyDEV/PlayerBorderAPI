package com.github.zandy.playerborderapi.versionsupport;

import com.github.zandy.playerborderapi.api.PlayerBorderAPI;
import com.github.zandy.playerborderapi.api.PlayerBorderAPI.BorderColor;
import net.minecraft.server.v1_12_R1.PacketPlayOutWorldBorder;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import net.minecraft.server.v1_12_R1.WorldBorder;
import net.minecraft.server.v1_12_R1.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Arrays;

import static net.minecraft.server.v1_12_R1.PacketPlayOutWorldBorder.EnumWorldBorderAction.*;

@SuppressWarnings("unused")
public class v1_12_R1 extends VersionSupport {

    @Override
    public void sendBorder(Player player, BorderColor color, int size, int centerX, int centerZ) {
        WorldBorder worldBorder = new WorldBorder();
        worldBorder.setCenter(centerX, centerZ);
        worldBorder.setSize(size);
        worldBorder.setWarningDistance(0);
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
        worldBorder.world = (WorldServer) ((CraftPlayer) player).getHandle().world;
        sendPackets(playerConnection, worldBorder);
        if (!(color.equals(BorderColor.RED) || color.equals(BorderColor.GREEN))) return;
        PlayerBorderAPI.getCache().put(player.getUniqueId(), Bukkit.getScheduler().runTaskTimerAsynchronously(
                PlayerBorderAPI.getInstance().getJavaPlugin(), () -> {
                    worldBorder.transitionSizeBetween(color.formatSize(size), color.formatSizeTo(size), 20000000);
                    playerConnection.sendPacket(new PacketPlayOutWorldBorder(worldBorder, LERP_SIZE));
                }, 0, 120
        ));
    }

    private void sendPackets(PlayerConnection playerConnection, WorldBorder worldBorder) {
        Arrays.asList(SET_SIZE, SET_CENTER, SET_WARNING_BLOCKS).forEach(action ->
                playerConnection.sendPacket(new PacketPlayOutWorldBorder(worldBorder, action)));
    }
}