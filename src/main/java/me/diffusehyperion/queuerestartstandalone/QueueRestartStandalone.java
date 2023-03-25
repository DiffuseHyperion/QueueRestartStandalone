package me.diffusehyperion.queuerestartstandalone;

import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class QueueRestartStandalone extends JavaPlugin implements Listener {
    public static Plugin plugin;
    public static Timer ongoingTimer;

    @Override
    public void onEnable() {
        plugin = this;
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
}
