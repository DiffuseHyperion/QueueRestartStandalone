package me.diffusehyperion.queuerestartstandalone;

import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Objects;

import static me.diffusehyperion.queuerestartstandalone.QueueRestartStandalone.ongoingTimer;

public class QueueCancel implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (Objects.isNull(ongoingTimer)) {
            commandSender.sendMessage("Theres no restart/shutdown queued!");
            return true;
        }
        String reason;
        if (args.length == 0) {
            reason = "No reason provided.";
        } else {
            StringBuilder reasonBuilder = new StringBuilder();
            for (String arg : args) {
                reasonBuilder.append(arg).append(" ");
            }
            reasonBuilder.deleteCharAt(reasonBuilder.length() - 1);
            reason = reasonBuilder.toString();
        }

        Action action = ongoingTimer.getAction();

        ongoingTimer.getTimerBossBar().removeAll();
        ongoingTimer.getTimerTask().cancel();
        ongoingTimer = null;

        if (action.equals(Action.RESTART)) {
            CancelTimer timer = new CancelTimer("Restart was cancelled: " + reason, 10, this::method, BarColor.GREEN, BarStyle.SEGMENTED_10, action);
            timer.showAllBossBar(commandSender, reason);
        } else if (action.equals(Action.STOP)) {
            Timer timer = new Timer("Shutdown was cancelled.", 10, this::method, BarColor.GREEN, BarStyle.SEGMENTED_10, action);
            timer.showAllBossBar(commandSender, 10, reason);
        }
        commandSender.sendMessage(ChatColor.GREEN + "Done!");
        return true;
    }

    public void method() {}
}
