package com.jaoow.blockstop.inventory;

import com.jaoow.blockstop.BlocksTop;
import com.jaoow.blockstop.model.MineUser;
import com.jaoow.blockstop.utils.Utils;
import com.jaoow.blockstop.utils.inventory.InventoryBuilder;
import com.jaoow.blockstop.utils.inventory.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class AllInv {

    public static final String title;
    public static final int size;

    public static final String name;
    public static final List<String> lore;


    static {
        FileConfiguration config = BlocksTop.getPlugin(BlocksTop.class).getConfig();

        title = config.getString("inventories.all.title");
        size = config.getInt("inventories.all.size");

        name = config.getString("inventories.all.baseItem.name");
        lore = config.getStringList("inventories.all.baseItem.lore");
    }

    public void open(Player player) {
        new InventoryBuilder<InventoryBuilder.VoidItem>(title, size)
                .withPage(1, 15)
                .withSlotStart(10)
                .withSlotSkip(2, 17, 26, 35, 44)
                .withSlotEnd(34)
                .withItemStacks(getTopList(), (event, value) -> {})
                .open(player);
    }

    private List<ItemStack> getTopList() {
        AtomicInteger pos = new AtomicInteger(1);
        DecimalFormat format = new DecimalFormat("#,##0.#");

        return BlocksTop.getInstance().getUserManager().getUsers().stream()
                .sorted(Comparator.comparingDouble(MineUser::getMinedAmount).reversed()).limit(5)
                .map(mineUser -> {

                    String playerName = Bukkit.getOfflinePlayer(mineUser.getUniqueId()).getName();

                    ItemBuilder builder = new ItemBuilder(Utils.HEAD_MATERIAL);
                    builder.withName(name).withLore(lore).setOwner(playerName);

                    return builder.build(
                            new String[]{"%name%", "%position%", "%value%"},
                            new String[]{playerName, String.valueOf(pos.getAndIncrement()), format.format(mineUser.getMinedAmount())});

                }).collect(Collectors.toList());
    }
}
