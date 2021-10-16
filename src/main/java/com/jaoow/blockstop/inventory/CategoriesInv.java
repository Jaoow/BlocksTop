package com.jaoow.blockstop.inventory;

import com.jaoow.blockstop.BlocksTop;
import com.jaoow.blockstop.utils.inventory.InventoryBuilder;
import com.jaoow.blockstop.utils.inventory.ItemBuilder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CategoriesInv {

    public static final String title;
    public static final int size;

    public static final ItemStack allItem;
    public static final Integer allItemSlot;

    public static final ItemStack oresItem;
    public static final Integer oresItemSlot;


    static {
        FileConfiguration config = BlocksTop.getPlugin(BlocksTop.class).getConfig();

        title = config.getString("inventories.categories.title");
        size = config.getInt("inventories.categories.size");

        allItem = ItemBuilder.fromSection(config.getConfigurationSection("inventories.categories.items.all")).build();
        allItemSlot = config.getInt("inventories.categories.items.all.slot");

        oresItem = ItemBuilder.fromSection(config.getConfigurationSection("inventories.categories.items.ores")).build();
        oresItemSlot = config.getInt("inventories.categories.items.ores.slot");
    }

    public void open(Player player) {
        new InventoryBuilder<InventoryBuilder.VoidItem>(title, size)
                .withItem(allItemSlot, allItem, (event, value) -> new AllInv().open(player))
                .withItem(oresItemSlot, oresItem, (event, value) -> new OreInv().open(player))
                .open(player);
    }
}
