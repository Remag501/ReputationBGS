package me.remag501.reputation.commands;

import me.remag501.reputation.core.ReputationCore;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReputationCommand implements CommandExecutor {

    private final ReputationCore reputationCore;

    public ReputationCommand(ReputationCore reputationCore) {
        this.reputationCore = reputationCore;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cUsage: /reputation <add|remove|set|view|tradebuy|tradesell> ...");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "add":        return handleAdd(sender, args);
            case "remove":     return handleRemove(sender, args);
            case "set":        return handleSet(sender, args);
            case "view":       return handleView(sender, args);
            case "tradebuy":   return handleTradeBuy(sender, args);
            case "tradesell":  return handleTradeSell(sender, args);
            default:
                sender.sendMessage("§cUnknown subcommand. Use add, remove, set, view, tradebuy, or tradesell.");
                return true;
        }
    }


    // ---------------- Subcommand Handlers ----------------

    private boolean handleAdd(CommandSender sender, String[] args) {
        // /reputation add <player> <npc> <amount>
        if (args.length < 4) {
            sender.sendMessage("§cUsage: /reputation add <player> <npc> <amount>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found.");
            return true;
        }

        String npc = args[2].toLowerCase();
        if (!reputationCore.isValidNpc(npc)) {
            sender.sendMessage("§cInvalid NPC. Valid NPCs: " + String.join(", ", reputationCore.getNpcList()));
            return true;
        }

        try {
            int amount = Integer.parseInt(args[3]);
            reputationCore.addReputation(target, npc, amount);
            sender.sendMessage("§aAdded " + amount + " reputation with " + npc + " to " + target.getName() + ".");
        } catch (NumberFormatException e) {
            sender.sendMessage("§cAmount must be a number.");
        }
        return true;
    }

    private boolean handleRemove(CommandSender sender, String[] args) {
        // /reputation remove <player> <npc> <amount>
        if (args.length < 4) {
            sender.sendMessage("§cUsage: /reputation remove <player> <npc> <amount>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found.");
            return true;
        }

        String npc = args[2].toLowerCase();
        if (!reputationCore.isValidNpc(npc)) {
            sender.sendMessage("§cInvalid NPC. Valid NPCs: " + String.join(", ", reputationCore.getNpcList()));
            return true;
        }

        try {
            int amount = Integer.parseInt(args[3]);
            reputationCore.removeReputation(target, npc, amount);
            sender.sendMessage("§aRemoved " + amount + " reputation with " + npc + " from " + target.getName() + ".");
        } catch (NumberFormatException e) {
            sender.sendMessage("§cAmount must be a number.");
        }
        return true;
    }

    private boolean handleSet(CommandSender sender, String[] args) {
        // /reputation set <player> <npc> <amount>
        if (args.length < 4) {
            sender.sendMessage("§cUsage: /reputation set <player> <npc> <amount>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found.");
            return true;
        }

        String npc = args[2].toLowerCase();
        if (!reputationCore.isValidNpc(npc)) {
            sender.sendMessage("§cInvalid NPC. Valid NPCs: " + String.join(", ", reputationCore.getNpcList()));
            return true;
        }

        try {
            int amount = Integer.parseInt(args[3]);
            reputationCore.setReputation(target, npc, amount);
            sender.sendMessage("§aSet " + target.getName() + "'s reputation with " + npc + " to " + amount + ".");
        } catch (NumberFormatException e) {
            sender.sendMessage("§cAmount must be a number.");
        }
        return true;
    }

    private boolean handleView(CommandSender sender, String[] args) {
        // /reputation view <player> <npc>
        if (args.length < 3) {
            sender.sendMessage("§cUsage: /reputation view <player> <npc>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found.");
            return true;
        }

        String npc = args[2].toLowerCase();
        if (!reputationCore.isValidNpc(npc)) {
            sender.sendMessage("§cInvalid NPC. Valid NPCs: " + String.join(", ", reputationCore.getNpcList()));
            return true;
        }

        int rep = reputationCore.getReputation(target, npc);
        sender.sendMessage("§e" + target.getName() + " has " + rep + " reputation with " + npc + ".");
        return true;
    }

    private boolean handleTradeBuy(CommandSender sender, String[] args) {
        // /reputation tradebuy <player> <dealer> <amount>
        if (args.length < 4) {
            sender.sendMessage("§cUsage: /reputation tradebuy <player> <dealer> <amount>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found.");
            return true;
        }

        String npc = args[2].toLowerCase();
        if (!reputationCore.isValidNpc(npc)) {
            sender.sendMessage("§cInvalid dealer. Valid dealers: " + String.join(", ", reputationCore.getNpcList()));
            return true;
        }

        try {
            double currencyAmount = Double.parseDouble(args[3]);
            double buyRate = reputationCore.getBuyRate();
            int repPoints = (int) Math.floor(currencyAmount * buyRate);

            reputationCore.addReputation(target, npc, repPoints);

            sender.sendMessage("§aConverted " + currencyAmount + " currency into " + repPoints
                    + " reputation with " + npc + " for " + target.getName() + ".");
        } catch (NumberFormatException e) {
            sender.sendMessage("§cAmount must be a number.");
        }
        return true;
    }

    private boolean handleTradeSell(CommandSender sender, String[] args) {
        // /reputation tradesell <player> <dealer> <amount>
        if (args.length < 4) {
            sender.sendMessage("§cUsage: /reputation tradesell <player> <dealer> <amount>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found.");
            return true;
        }

        String npc = args[2].toLowerCase();
        if (!reputationCore.isValidNpc(npc)) {
            sender.sendMessage("§cInvalid dealer. Valid dealers: " + String.join(", ", reputationCore.getNpcList()));
            return true;
        }

        try {
            double currencyAmount = Double.parseDouble(args[3]);
            double sellRate = reputationCore.getSellRate();
            int repPoints = (int) Math.floor(currencyAmount * sellRate);

            reputationCore.addReputation(target, npc, repPoints);

            sender.sendMessage("§aConverted " + currencyAmount + " currency into " + repPoints
                    + " reputation with " + npc + " for " + target.getName() + ".");
        } catch (NumberFormatException e) {
            sender.sendMessage("§cAmount must be a number.");
        }
        return true;
    }

}
