package com.github.zandy.playerborderapi.versionsupport;

import com.github.zandy.playerborderapi.api.PlayerBorderAPI;
import com.github.zandy.playerborderapi.api.PlayerBorderAPI.BorderColor;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.level.border.WorldBorder;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class v1_17_R1 extends VersionSupport {

    @Override
    public void sendBorder(Player player, BorderColor borderColor, int size, int centerX, int centerZ) {
        WorldBorder worldBorder = new WorldBorder();
        try {
            worldBorder.getClass().getMethod("setCenter", double.class, double.class).invoke(worldBorder, centerX, centerZ);
            worldBorder.getClass().getMethod("setSize", double.class).invoke(worldBorder, size);
            worldBorder.getClass().getMethod("setWarningDistance", double.class).invoke(worldBorder, 0);
            PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().b;
            EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
            worldBorder.world = (WorldServer) entityPlayer.getClass().getMethod("getWorld").invoke(entityPlayer);
            if (!(borderColor.equals(BorderColor.RED) || borderColor.equals(BorderColor.GREEN))) {
                PlayerBorderAPI.getCache().put(player.getUniqueId(), new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (PlayerBorderAPI.getInstance().hasBorder(player.getUniqueId())) {
                            try {
                                worldBorder.getClass().getMethod("transitionSizeBetween", double.class, double.class, long.class).invoke(worldBorder, borderColor.formatSize(size), borderColor.formatSizeTo(size), 20000000);
                            } catch (Exception ignored) {}
                        }
                    }
                }.runTaskTimerAsynchronously(PlayerBorderAPI.getInstance().getJavaPlugin(), 120, 120));
            }
            playerConnection.getClass().getMethod("sendPacket", Packet.class).invoke(playerConnection, new ClientboundInitializeBorderPacket(worldBorder));
        } catch (Exception ignored) {}
    }
}