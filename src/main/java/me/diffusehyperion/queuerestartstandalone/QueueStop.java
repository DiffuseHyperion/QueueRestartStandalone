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

public class QueueStop implements CommandExecutor {

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
        ongoingTimer = new Timer("Shutting down in " + duration + " seconds: " + reason, duration, this::method, BarColor.RED, BarStyle.SEGMENTED_10, Action.RESTART);
        ongoingTimer.showAllBossBar(commandSender, duration, reason);

        sendDiscordMessage(":warning: Server is shutting down in " + duration + " for the reason: " + reason);

        commandSender.sendMessage(ChatColor.GREEN + "Done!");
        return true;
    }

    public void method() {
        Bukkit.getLogger().info("Shutting down the server now...");
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.kickPlayer(ChatColor.RED + "" + ChatColor.BOLD + "Server is shutting down! " +
                    ChatColor.RESET + "" + ChatColor.GOLD + "No progress has been lost. Join back later!");
        }

        sendDiscordMessage(":octagonal_sign: Server is shutting down now for the reason: " + reason);

        ConfigurationSection pteroConfig = config.getConfigurationSection("pterodactyl");

        if (Objects.nonNull(pteroConfig) && pteroConfig.getBoolean("enabled", false)) {
            List<Pair<String, String>> urlParameters = new ArrayList<>();
            urlParameters.add(new Pair<>("Accept", "application/json"));
            urlParameters.add(new Pair<>("Content-Type", "application/json"));
            //urlParameters.add(new Pair<>("Authorization", "Bearer " + config.getString("apiKey")));
            urlParameters.add(new Pair<>("Authorization", "Bearer " + pteroConfig.getString("apiKey")));
            makeOutputRequest(pteroConfig.getString("host") + "/api/client/servers/" + pteroConfig.getString("identifier") + "/power", "POST", urlParameters,
                    "{\n" +
                            "  \"signal\": \"stop\"\n" +
                            "}");
        } else {
            Bukkit.getServer().shutdown();
        }
    }
}
