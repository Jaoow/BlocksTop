package com.jaoow.blockstop.commands;

import com.google.common.collect.Lists;
import com.jaoow.blockstop.inventory.AllInv;
import com.jaoow.blockstop.inventory.CategoriesInv;
import com.jaoow.blockstop.inventory.OreInv;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class BlocksTopCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("all")) {
                    new AllInv().open((Player) sender);
                    return false;
                } else if (args[0].equalsIgnoreCase("ores")) {
                    new OreInv().open((Player) sender);
                    return false;
                }
            }
            new CategoriesInv().open((Player) sender);
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return args.length == 1 ? Lists.newArrayList("all", "ores") : Collections.emptyList();
    }
}
