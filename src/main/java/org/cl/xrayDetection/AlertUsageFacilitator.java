package org.cl.xrayDetection;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.cl.xrayDetection.util.VariableString;

import java.util.HashMap;
import java.util.Map;

public final class AlertUsageFacilitator implements CommandExecutor {
    private final XrayDetection plugin;

    AlertUsageFacilitator(XrayDetection plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player player)) {
            return true;
        }

        if (args.length != XrayDetection.ORDERED_PLACEHOLDERS.size()) {
            Bukkit.getLogger().severe("Insufficient quantity of passed arguments to alert usage facilitator. This should never happen. Please contact the developer.");
            return true;
        }

        Map<String, String> mapper = new HashMap<>();

        for (int i = 0; i < args.length; i++) {
            mapper.put(XrayDetection.ORDERED_PLACEHOLDERS.get(i), args[i]);
        }

        for (VariableString varCmd : plugin.getConfigCache().clickCommands()) {
            player.performCommand(varCmd.build(mapper));
        }

        return true;
    }
}
