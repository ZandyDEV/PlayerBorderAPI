package com.github.zandy.playerboderapi.versionsupport;

import com.github.zandy.playerboderapi.api.PlayerBorderAPI;
import com.github.zandy.playerboderapi.api.PlayerBorderAPI.BorderColor;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.level.border.WorldBorder;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class v1_18_R1 extends VersionSupport {

    @Override
    public void sendBorder(Player player, BorderColor borderColor, int size, int centerX, int centerZ) {
        WorldBorder worldBorder = new WorldBorder();
        worldBorder.c(centerX, centerZ);
        worldBorder.a(size);
        worldBorder.c(0);
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().b;
        worldBorder.world = ((CraftPlayer) player).getHandle().x();
        if (!(borderColor.equals(BorderColor.RED) || borderColor.equals(BorderColor.GREEN))) {
            PlayerBorderAPI.getCache().put(player.getUniqueId(), new BukkitRunnable() {
                @Override
                public void run() {
                    if (PlayerBorderAPI.getInstance().hasBorder(player.getUniqueId())) worldBorder.a(borderColor.formatSize(size), borderColor.formatSizeTo(size), 20000000);
                }
            }.runTaskTimerAsynchronously(PlayerBorderAPI.getInstance().getJavaPlugin(), 120, 120));
        }
        playerConnection.a(new ClientboundInitializeBorderPacket(worldBorder));
    }
}