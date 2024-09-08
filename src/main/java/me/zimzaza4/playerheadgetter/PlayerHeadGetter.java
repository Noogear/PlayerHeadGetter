package me.zimzaza4.playerheadgetter;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public final class PlayerHeadGetter extends JavaPlugin {

    public static Set<String> textures = new HashSet<>();
    public static PlayerHeadGetter plugin;


    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        Bukkit.getPluginCommand("skullgetter").setExecutor(new SkullCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
