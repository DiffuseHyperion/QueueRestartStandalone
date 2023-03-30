package me.diffusehyperion.queuerestartstandalone;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static me.diffusehyperion.queuerestartstandalone.QueueRestartStandalone.*;

public class QueueRestart implements CommandExecutor {

    private String reason;

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
        reason = reasonBuilder.toString();

        ongoingTimer = new Timer("Restarting in " + duration + " seconds: " + reason, duration, this::method, BarColor.YELLOW, BarStyle.SEGMENTED_10, Action.RESTART);
        ongoingTimer.showAllBossBar(commandSender, duration, reason);

        sendDiscordMessage(":warning: Server is restarting in " + duration + " for the reason: " + reason);

        commandSender.sendMessage(ChatColor.GREEN + "Done!");
        return true;
    }

    public void method() {
        Bukkit.getLogger().info("Restarting the server now...");
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.kickPlayer(ChatColor.GOLD + "" + ChatColor.BOLD + "Server is restarting! " +
                    ChatColor.RESET + "" + ChatColor.GOLD + "No progress has been lost. Join back soon!");
        }

        sendDiscordMessage(":octagonal_sign: Server is restarting now for the reason: " + reason);

        ConfigurationSection pteroConfig = config.getConfigurationSection("pterodactyl");
        if (Objects.nonNull(pteroConfig) && pteroConfig.getBoolean("enabled", false)) {
            List<QueueRestartStandalone.Pair<String, String>> urlParameters = new ArrayList<>();
            urlParameters.add(new QueueRestartStandalone.Pair<>("Accept", "application/json"));
            urlParameters.add(new QueueRestartStandalone.Pair<>("Content-Type", "application/json"));
            //urlParameters.add(new Pair<>("Authorization", "Bearer " + config.getString("apiKey")));
            urlParameters.add(new QueueRestartStandalone.Pair<>("Authorization", "Bearer " + pteroConfig.getString("apiKey")));
            makeOutputRequest(pteroConfig.getString("host") + "/api/client/servers/" + pteroConfig.getString("identifier") + "/power", "POST", urlParameters,
                    "{\n" +
                            "  \"signal\": \"restart\"\n" +
                            "}");
        } else {
            Bukkit.spigot().restart();
        }
    }
}