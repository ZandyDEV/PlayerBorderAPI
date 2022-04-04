package com.github.zandy.playerborderapi;

import com.github.zandy.playerborderapi.api.PlayerBorderAPI;
import com.github.zandy.playerborderapi.bstats.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

@Plugin(name = "PlayerBorderAPI", version = "Build 5")
@Author("Zandy")
@Description("PlayerBorderAPI allows to bring visual borders to players.")
@SuppressWarnings("unused")
public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        new Metrics(this);
        PlayerBorderAPI.getInstance().setJavaPlugin(this);
    }

    @Override
    public void onDisable() {
        PlayerBorderAPI.getInstance().removeBorders();
    }
}