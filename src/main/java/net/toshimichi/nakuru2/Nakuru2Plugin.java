package net.toshimichi.nakuru2;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

public class Nakuru2Plugin extends JavaPlugin {

    private JDA jda;
    private DiscordHandler handler;

    @Override
    public void onLoad() {
        try {
            // generate config
            saveDefaultConfig();

            // create JDA instance
            jda = JDABuilder.createDefault(getConfig().getString("token")).build();
            jda.awaitReady();

            // create DiscordHandler
            Set<Level> levels = new HashSet<>();
            getConfig().getStringList("levels").forEach(l -> levels.add(Level.parse(l)));
            TextChannel channel = jda.getTextChannelById(getConfig().getString("channel"));
            handler = new DiscordHandler(levels, channel, new SimpleFormatter());
            Bukkit.getLogger().addHandler(handler);
        } catch (Exception e) {
            e.printStackTrace();
            getLogger().severe("An error occurred and the plugin has been disabled");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().removeHandler(handler);
        if (jda != null) {
            jda.shutdown();
        }
    }
}
