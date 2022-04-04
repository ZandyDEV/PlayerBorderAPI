package com.github.zandy.playerborderapi.versionsupport;

import com.github.zandy.playerborderapi.api.PlayerBorderAPI;
import com.github.zandy.playerborderapi.api.PlayerBorderAPI.BorderColor;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderLerpSizePacket;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.level.border.WorldBorder;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class v1_18_R2 extends VersionSupport {

    @Override
    public void sendBorder(Player player, BorderColor color, int size, int centerX, int centerZ) {
        WorldBorder worldBorder = new WorldBorder();
        worldBorder.a((double) size);
        worldBorder.c(centerX, centerZ);
        worldBorder.c(0);
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().b;
        worldBorder.world = ((CraftPlayer) player).getHandle().x();
        playerConnection.a(new ClientboundInitializeBorderPacket(worldBorder));
        playerConnection.a(new ClientboundSetBorderCenterPacket(worldBorder));
        if (!(color.equals(BorderColor.RED) || color.equals(BorderColor.GREEN))) return;
        PlayerBorderAPI.getCache().put(player.getUniqueId(), Bukkit.getScheduler().runTaskTimerAsynchronously(
                PlayerBorderAPI.getInstance().getJavaPlugin(), () -> {
                    worldBorder.a(color.formatSize(size), color.formatSizeTo(size), 20000000);
                    playerConnection.a(new ClientboundSetBorderLerpSizePacket(worldBorder));
                }, 0, 120
        ));
    }
}