package me.diffusehyperion.queuerestartstandalone;

import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.util.DiscordUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Objects;

public final class QueueRestartStandalone extends JavaPlugin implements Listener {
    public static Plugin plugin;
    public static Timer ongoingTimer;
    public static FileConfiguration config;

    @Override
    public void onEnable() {
        plugin = this;
        this.saveDefaultConfig();
        config = this.getConfig();

        if (config.getBoolean("discord.discordsrv.enabled") && !getServer().getPluginManager().isPluginEnabled("DiscordSRV")) {
            getLogger().severe("DiscordSRV support was enabled, but DiscordSRV was not installed!");
            this.getPluginLoader().disablePlugin(this);
        }

        Objects.requireNonNull(getCommand("queuerestart")).setExecutor(new QueueRestart());
        Objects.requireNonNull(getCommand("queuestop")).setExecutor(new QueueStop());
        Objects.requireNonNull(getCommand("queuecancel")).setExecutor(new QueueCancel());
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (Objects.nonNull(ongoingTimer)) {
            ongoingTimer.getTimerBossBar().addPlayer(e.getPlayer());
            e.getPlayer().playSound(e.getPlayer(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        }
    }

    public static Pair<Integer, String> makeOutputRequest(String targetURL, String method, List<Pair<String, String>> urlParameters, String output) {
        try {
            URL url = new URL(targetURL);
            HttpURLConnection uc = (HttpURLConnection) url.openConnection();
            uc.setDoOutput(true);
            uc.setRequestMethod(method);

            for (Pair<String, String> parameter : urlParameters) {
                uc.setRequestProperty(parameter.getValue1(), parameter.getValue2());
            }

            OutputStream os = uc.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);
            osw.write(output);
            osw.flush();
            osw.close();
            os.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            String line = in.readLine();
            in.close();
            uc.disconnect();
            return new Pair<>(uc.getResponseCode(), line);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sendDiscordMessage(String message) {
        ConfigurationSection discordSRVConfig = config.getConfigurationSection("discord.discordsrv");
        ConfigurationSection webhookConfig = config.getConfigurationSection("discord.webhook");
        if (Objects.nonNull(discordSRVConfig) && discordSRVConfig.getBoolean("enabled", false)) {
            String channelID = discordSRVConfig.getString("channelID");
            TextChannel channel = DiscordUtil.getTextChannelById(channelID);
            if (Objects.nonNull(channel)) {
                DiscordUtil.sendMessage(channel, message);
            } else {
                Bukkit.getLogger().severe("Could not find a discord channel with the ID: " + channelID);
            }
        } else if (Objects.nonNull(webhookConfig) && webhookConfig.getBoolean("enabled", false)) {
            // not supported yet
            //String webhookURL = webhookConfig.getString("webhookUrl");
            //makeOutputRequest()
        }
    }

    public static class Pair<X, Y> {
        private X value1;
        private Y value2;

        public Pair(X value1, Y value2) {
            this.value1 = value1;
            this.value2 = value2;
        }

        public X getValue1() {
            return value1;
        }

        public Y getValue2() {
            return value2;
        }

        public void setValue1(X value1) {
            this.value1 = value1;
        }

        public void setValue2(Y value2) {
            this.value2 = value2;
        }
    }
}
