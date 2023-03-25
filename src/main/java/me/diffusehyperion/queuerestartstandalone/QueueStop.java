package me.diffusehyperion.queuerestartstandalone;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

import static me.diffusehyperion.queuerestartstandalone.QueueRestartStandalone.ongoingTimer;

public class QueueStop implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (Objects.nonNull(ongoingTimer)) {
            commandSender.sendMessage(ChatColor.RED + "A restart/shutdown is already ongoing!");
            return true;
        }
        if (args.length < 2) {
            commandSender.sendMessage(ChatColor.RED + "Not enough arguments!");
            return false;
        }
        int duration = Integer.parseInt(args[0]);
        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }
        reasonBuilder.deleteCharAt(reasonBuilder.length() - 1);
        String reason = reasonBuilder.toString();
        ongoingTimer = new Timer("Shutting down in " + duration + " seconds: " + reason, duration, this::method, BarColor.RED, BarStyle.SEGMENTED_10, Action.RESTART);
        ongoingTimer.showAllBossBar(commandSender, duration, reason);
        commandSender.sendMessage(ChatColor.GREEN + "Done!");
        return true;
    }

    public void method() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.kickPlayer(ChatColor.RED + "" + ChatColor.BOLD + "Server is shutting down! " +
                    ChatColor.RESET + "" + ChatColor.GOLD + "No progress has been lost. Join back later!");
        }
        Bukkit.spigot().restart();
    }
}
