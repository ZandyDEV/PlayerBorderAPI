package com.github.zandy.playerborderapi.versionsupport;

import com.github.zandy.playerborderapi.api.PlayerBorderAPI;
import com.github.zandy.playerborderapi.api.PlayerBorderAPI.BorderColor;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderLerpSizePacket;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.level.border.WorldBorder;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

@SuppressWarnings("unused")
public class v1_17_R1 extends VersionSupport {

    @Override
    public void sendBorder(Player player, BorderColor color, int size, int centerX, int centerZ) {
        WorldBorder worldBorder = new WorldBorder();
        worldBorder.setCenter(centerX, centerZ);
        worldBorder.setSize(size);
        worldBorder.setWarningDistance(0);
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().b;
        worldBorder.world = (WorldServer) (((CraftPlayer) player).getHandle()).getWorld();
        playerConnection.sendPacket(new ClientboundInitializeBorderPacket(worldBorder));
        if (!(color.equals(BorderColor.RED) || color.equals(BorderColor.GREEN))) return;
        PlayerBorderAPI.getCache().put(player.getUniqueId(), Bukkit.getScheduler().runTaskTimerAsynchronously(
                PlayerBorderAPI.getInstance().getJavaPlugin(), () -> {
                    worldBorder.transitionSizeBetween(color.formatSize(size), color.formatSizeTo(size), 20000000);
                    playerConnection.sendPacket(new ClientboundSetBorderLerpSizePacket(worldBorder));
                }, 0, 120
        ));
    }
}