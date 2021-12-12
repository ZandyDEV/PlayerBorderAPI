package com.github.zandy.playerboderapi;

import com.github.zandy.playerboderapi.api.PlayerBorderAPI;
import com.github.zandy.playerboderapi.bstats.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

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
