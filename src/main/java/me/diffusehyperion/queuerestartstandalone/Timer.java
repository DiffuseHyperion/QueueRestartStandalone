package me.diffusehyperion.queuerestartstandalone;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static me.diffusehyperion.queuerestartstandalone.QueueRestartStandalone.plugin;

public class Timer {
    private final BossBar timer;
    private final BukkitRunnable timerTask;
    private final Action action;

    public Timer(String title, int duration, Runnable methodToRun, Action action) {
        timer = Bukkit.createBossBar(title, BarColor.WHITE, BarStyle.SEGMENTED_10, BarFlag.PLAY_BOSS_MUSIC);
        this.action = action;
        timerTask = startTimer(timer, duration, methodToRun);
        timerTask.runTaskTimer(plugin, 0, 2);
    }

    public Timer(String title, int duration, Runnable methodToRun, BarColor barColor, BarStyle barStyle, Action action) {
        timer = Bukkit.createBossBar(title, barColor, barStyle, BarFlag.PLAY_BOSS_MUSIC);
        this.action = action;
        timerTask = startTimer(timer, duration, methodToRun);
        timerTask.runTaskTimer(plugin, 0, 2);
    }

    public Timer(String title, int duration, Runnable methodToRun, BarColor barColor, BarStyle barStyle, Action action, BarFlag... barFlags) {
        timer = Bukkit.createBossBar(title, barColor, barStyle, barFlags);
        this.action = action;
        timerTask = startTimer(timer, duration, methodToRun);
        timerTask.runTaskTimer(plugin, 0, 2);
    }

    public BossBar getTimerBossBar() {
        return timer;
    }

    public BukkitRunnable getTimerTask() {
        return timerTask;
    }

    public Action getAction() {return action;}

    public void showAllBossBar(CommandSender sender, int duration, String reason) {
        switch (action) {
            case RESTART: {
                if (sender instanceof ConsoleCommandSender) {
                    Bukkit.spigot().broadcast(
                            new ComponentBuilder("Console").color(ChatColor.RED).bold(true)
                                    .append(" has triggered a ").color(ChatColor.RED).bold(false)
                                    .append("restart").color(ChatColor.GOLD).bold(true)
                                    .append(" in ").color(ChatColor.RED).bold(false)
                                    .append(duration + " seconds!").color(ChatColor.GOLD).underlined(true).create());
                } else if (sender instanceof Player) {
                    Bukkit.spigot().broadcast(
                            new ComponentBuilder(((Player) sender).getDisplayName()).color(ChatColor.RED).bold(true)
                                    .append(" has triggered a ").color(ChatColor.RED).bold(false)
                                    .append("restart").color(ChatColor.GOLD).bold(true)
                                    .append(" in ").color(ChatColor.RED).bold(false)
                                    .append(duration + " seconds!").color(ChatColor.GOLD).underlined(true).create());
                } else {
                    Bukkit.spigot().broadcast(
                            new ComponentBuilder("Server").color(ChatColor.RED).bold(true)
                                    .append(" has triggered a ").color(ChatColor.RED).bold(false)
                                    .append("restart").color(ChatColor.GOLD).bold(true)
                                    .append(" in ").color(ChatColor.RED).bold(false)
                                    .append(duration + " seconds!").color(ChatColor.GOLD).underlined(true).create());
                }
                break;
            }
            case STOP: {
                if (sender instanceof ConsoleCommandSender) {
                    Bukkit.spigot().broadcast(
                            new ComponentBuilder("Console").color(ChatColor.RED).bold(true)
                                    .append(" has triggered a ").color(ChatColor.RED).bold(false)
                                    .append("shutdown").color(ChatColor.DARK_RED).bold(true)
                                    .append(" in ").color(ChatColor.RED).bold(false)
                                    .append(duration + " seconds!").color(ChatColor.GOLD).underlined(true).create());
                } else if (sender instanceof Player) {
                    Bukkit.spigot().broadcast(
                            new ComponentBuilder(((Player) sender).getDisplayName()).color(ChatColor.RED).bold(true)
                                    .append(" has triggered a ").color(ChatColor.RED).bold(false)
                                    .append("shutdown").color(ChatColor.DARK_RED).bold(true)
                                    .append(" in ").color(ChatColor.RED).bold(false)
                                    .append(duration + " seconds!").color(ChatColor.GOLD).underlined(true).create());
                } else {
                    Bukkit.spigot().broadcast(
                            new ComponentBuilder("Server").color(ChatColor.RED).bold(true)
                                    .append(" has triggered a ").color(ChatColor.RED).bold(false)
                                    .append("shutdown").color(ChatColor.DARK_RED).bold(true)
                                    .append(" in ").color(ChatColor.RED).bold(false)
                                    .append(duration + " seconds!").color(ChatColor.GOLD).underlined(true).create());
                }
                break;
            }
        }
        Bukkit.spigot().broadcast(
                new ComponentBuilder("Reason provided: ").color(ChatColor.RED)
                        .append(reason).color(ChatColor.YELLOW).italic(true).create());
        Bukkit.spigot().broadcast(
                new ComponentBuilder("No progress will be lost.").color(ChatColor.YELLOW).underlined(true).create());
        for (Player p : Bukkit.getOnlinePlayers()) {
            timer.addPlayer(p);
            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        }
    }

    private BukkitRunnable startTimer(BossBar bossBar, int duration, Runnable method) {
        BigDecimal[] timer = {BigDecimal.valueOf(duration)};
        return new BukkitRunnable() {
            @Override
            public void run() {
                // handles timer counting down
                bossBar.setProgress(timer[0].divide(BigDecimal.valueOf(duration), 5, RoundingMode.HALF_EVEN).doubleValue());
                timer[0] = timer[0].subtract(BigDecimal.valueOf(0.1));

                // handles timer stop
                if (timer[0].doubleValue() <= 0) {
                    method.run();
                    bossBar.removeAll();
                    this.cancel();
                }

                // handles last 10 second dings
                if (timer[0].doubleValue() % 1 == 0 && timer[0].doubleValue() <= 10) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.getWorld().playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    }
                }
            }
        };
    }
}
