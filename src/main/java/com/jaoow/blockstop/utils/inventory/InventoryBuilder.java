package com.jaoow.blockstop.utils.inventory;

import javafx.util.Pair;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.IntPredicate;

/**
 * class to facilitate the construction of inventories.
 *
 * @param <T> the type of builder
 * @author Jaoow
 * @version 1.0
 */
public class InventoryBuilder<T extends InventoryBuilder.InventoryItem> {

    @Getter
    private final String inventoryName;
    @Getter
    private final Inventory inventory;
    @Getter
    private final Map<Integer, T> map = new HashMap<>();

    private final Map<ButtonType, Pair<Integer, ItemStack>> PAGES = new ConcurrentHashMap<>();
    private final List<InventoryFormat<T>> FORMATS = new CopyOnWriteArrayList<>();

    @Getter
    private int page = 1;
    @Getter
    private int maxPage = 0;
    @Getter
    private int size = 0;

    private int exit;
    private int start = 0;
    private int value = 0;

    private IntPredicate scape;

    /**
     * @param name  the name of inventory
     * @param lines lines of inventory
     */
    public InventoryBuilder(String name, int lines) {
        int size = Math.min(6, Math.max(1, lines)) * 9;
        this.exit = size - 1;
        this.inventoryName = name.replace("&", "§");

        this.inventory = Bukkit.createInventory(new CustomHolder(event -> {
            int slot = event.getRawSlot();

            for (Map.Entry<ButtonType, Pair<Integer, ItemStack>> entry : PAGES.entrySet()) {
                if (slot == entry.getValue().getKey()) {

                    ButtonType type = entry.getKey();
                    if (type == ButtonType.BACK && this.hasPrevPage()) {
                        this.page += type.value;
                    } else if (type == ButtonType.NEXT && this.hasNextPage()) {
                        this.page += type.value;
                    }

                    formatInventory();
                    return;
                }
            }

            for (InventoryFormat<T> format : FORMATS) {
                if (format.isValid(event.getRawSlot())) {
                    T value = map.get(slot);
                    format.accept(event, value);
                    break;
                }
            }

        }), size, inventoryName.replace("{page}", String.valueOf(page)));
    }

    /**
     * build inventory
     *
     * @return the inventory
     */
    public Inventory build() {
        formatInventory();
        return this.inventory;
    }

    /**
     * set the page of inventory and size of objects in pages
     *
     * @param page the page
     * @param size the size
     * @return the builder
     */
    public InventoryBuilder<T> withPage(int page, int size) {
        this.page = page;
        this.size = size;

        return this;
    }

    /**
     * define the slot in which the items will start to be defined
     *
     * @param start the slot
     * @return the builder
     */
    public InventoryBuilder<T> withSlotStart(int start) {
        this.start = start;
        return this;
    }

    /**
     * skip slots and add to the current slot
     *
     * @param value the value to be added
     * @param scape the slots to skip
     * @return the builder
     */
    public InventoryBuilder<T> withSlotSkip(int value, int... scape) {
        this.value = value;
        this.scape = integer -> Arrays.stream(scape).anyMatch(slot -> slot == integer);

        return this;
    }

    /**
     * skip slots and add to the current slot
     *
     * @param value the value to be added
     * @param scape the function to skip
     * @return the builder
     */
    public InventoryBuilder<T> withSlotSkip(int value, IntPredicate scape) {
        this.value = value;
        this.scape = scape;

        return this;
    }

    /**
     * slot to stop setting items
     *
     * @param exit the exit
     * @return the builder
     */
    public InventoryBuilder<T> withSlotEnd(int exit) {
        this.exit = exit;
        return this;
    }

    /**
     * set the item of inventory
     *
     * @param slot      the slot
     * @param itemStack the itemstack
     * @param consumer  the consumer
     * @return the builder
     */
    public InventoryBuilder<T> withItem(int slot, ItemStack itemStack, ClickableItem<T> consumer) {
        FORMATS.add(new SingleInventoryFormat(slot, null, itemStack, consumer));
        return this;
    }

    /**
     * set the item of inventory
     *
     * @param slot      the slot
     * @param itemStack the itemstack
     * @return the builder
     */
    public InventoryBuilder<T> withItem(int slot, ItemStack itemStack) {
        FORMATS.add(new SingleInventoryFormat(slot, null, itemStack, null));
        return this;
    }

    /**
     * set the item of inventory with parameter T
     *
     * @param slot     the slot
     * @param value    the parameter
     * @param consumer the consumer
     * @return the builder
     */
    public InventoryBuilder<T> withItem(int slot, T value, ClickableItem<T> consumer) {
        FORMATS.add(new SingleInventoryFormat(slot, value, null, consumer));
        return this;
    }

    /**
     * set the items of inventory
     *
     * @param items    the items
     * @param consumer the consumer
     * @return the builder
     */
    public InventoryBuilder<T> withItemStacks(List<ItemStack> items, ClickableItem<T> consumer) {
        this.FORMATS.add(new MultiItemInventoryFormat(items, consumer));
        return this;
    }


    /**
     * set the items of inventory with parameter T
     *
     * @param items    the items
     * @param consumer the consumer
     * @return the builder
     */
    public InventoryBuilder<T> withItems(List<T> items, ClickableItem<T> consumer) {
        this.FORMATS.add(new MultiValueInventoryFormat(items, consumer));
        return this;
    }

    /**
     * set the item to skip the inventory pages
     *
     * @param slot      the slot
     * @param itemStack the item
     * @return the builder
     */
    public InventoryBuilder<T> withNextPage(int slot, ItemStack itemStack) {
        PAGES.put(ButtonType.NEXT, new Pair<>(slot, itemStack));
        return this;
    }

    /**
     * set the item to return the inventory pages
     *
     * @param slot      the slot of item
     * @param itemStack the item
     * @return the builder
     */
    public InventoryBuilder<T> withBackPage(int slot, ItemStack itemStack) {
        PAGES.put(ButtonType.BACK, new Pair<>(slot, itemStack));
        return this;
    }

    /**
     * set the parent to open when there no more back page
     *
     * @param parent the parent
     * @return the builder
     */
    public InventoryBuilder<T> withParent(Consumer<?> parent) {
        return this;
    }

    /**
     * Check if inventory has next page
     *
     * @return has next page
     */
    public boolean hasNextPage() {
        return page < maxPage;
    }

    /**
     * Check if inventory has previous page
     *
     * @return has previous page
     */
    public boolean hasPrevPage() {
        return page > 1;
    }


    /**
     * open inventory to player
     *
     * @param player the player
     * @return the builder
     */
    public InventoryBuilder<T> open(Player player) {
        formatInventory();
        player.openInventory(this.inventory);
        return this;
    }

    /**
     * Format the inventory and update items
     *
     * @return the builder
     */
    public InventoryBuilder<T> formatInventory() {
        inventory.clear();
        map.clear();

        FORMATS.forEach(format -> {
            if (format instanceof InventoryBuilder.MultiValueInventoryFormat) {
                MultiValueInventoryFormat value = (MultiValueInventoryFormat) format;
                value.map.clear();

                int slot = this.start;

                List<T> items = size <= 0 ? value.items : ListUtils.getSublist(value.items, page, size);
                for (int index = 0; index < items.size(); slot++) {
                    if (slot > this.exit) {
                        break;
                    }

                    if (this.scape != null && this.scape.test(slot)) {
                        slot += this.value - 1;
                        continue;
                    }

                    T item = items.get(index);

                    inventory.setItem(slot, item.getItem());
                    map.put(slot, item);
                    value.map.put(slot, item.getItem());

                    index++;
                }

                createPages(value.items.size());

            } else if (format instanceof InventoryBuilder.SingleInventoryFormat) {
                SingleInventoryFormat singleFormat = (SingleInventoryFormat) format;
                ItemStack itemStack = singleFormat.itemStack;

                if (singleFormat.value != null) {
                    itemStack = singleFormat.value.getItem();
                    map.put(singleFormat.slot, singleFormat.value);
                }

                inventory.setItem(singleFormat.slot, itemStack);

            } else if (format instanceof InventoryBuilder.MultiItemInventoryFormat) {
                MultiItemInventoryFormat value = (MultiItemInventoryFormat) format;
                value.map.clear();

                int slot = this.start;

                List<ItemStack> items = size <= 0 ? value.items : ListUtils.getSublist(value.items, page, size);
                for (int index = 0; index < items.size(); slot++) {
                    if (slot > this.exit) {
                        break;
                    }

                    if (this.scape != null && this.scape.test(slot)) {
                        slot += this.value - 1;
                        continue;
                    }

                    ItemStack item = items.get(index);

                    inventory.setItem(slot, item);
                    value.map.put(slot, item);

                    index++;
                }

                createPages(value.items.size());
            }
        });

        return this;
    }

    /**
     * Create a pages of inventory
     *
     * @param size the size of list
     */
    private void createPages(int size) {

        maxPage = Math.max((int) Math.ceil((double) size / this.size), 1);

        if (PAGES.containsKey(ButtonType.BACK)) {
            Pair<Integer, ItemStack> pair = PAGES.get(ButtonType.BACK);
            inventory.setItem(pair.getKey(), pair.getValue());
        }

        if (PAGES.containsKey(ButtonType.NEXT) && this.hasNextPage()) {
            Pair<Integer, ItemStack> pair = PAGES.get(ButtonType.NEXT);
            inventory.setItem(pair.getKey(), pair.getValue());
        }
    }

    /**
     * Private enum of buttons to easy
     * skip and back pages of the inventory
     */
    @AllArgsConstructor
    private enum ButtonType {

        BACK(-1),
        NEXT(1);

        private final int value;
    }

    /**
     * Interface that objects need to implement to be accepted by the builder
     */
    @FunctionalInterface
    public interface InventoryItem {

        ItemStack getItem();

    }

    /**
     * Private interface to format inventory
     *
     * @param <T> the type of builder
     */
    private interface InventoryFormat<T> {

        boolean isValid(int slot);

        void accept(InventoryClickEvent event, T value);
    }

    /**
     * Class called when player click in a valid item of inventory
     */
    @FunctionalInterface
    public interface ClickableItem<T> {

        void accept(InventoryClickEvent event, T value);

    }


    private static class ListUtils {

        /**
         * get a sublist of a list with size of list and pagination
         *
         * @param list  the list
         * @param value the pagination
         * @param size  the size of list
         * @return the list
         */
        public static <T> List<T> getSublist(List<T> list, int value, int size) {
            if (list.isEmpty()) return list;

            int first = Math.min(value * size - size, list.size() - 1);
            int end = Math.min(list.size(), first + size);

            return list.subList(first, end);
        }
    }

    /**
     * A class to padronize the creation of inventory
     */
    public abstract static class InventoryProvider<T extends InventoryItem> {

        private final InventoryBuilder<T> builder;

        /**
         * @param name  the name of inventory
         * @param lines lines of inventory
         */
        public InventoryProvider(String name, int lines) {
            this.builder = new InventoryBuilder<>(name, lines);
        }

        public abstract void initialize(Player player, InventoryBuilder<T> builder);

        public void open(@NotNull Player player) {
            initialize(player, builder);
            builder.open(player);
        }
    }

    /**
     * Class to make builder parameter empty
     */
    @AllArgsConstructor
    public static class VoidItem implements InventoryItem {

        private final ItemStack item;

        @Override
        public ItemStack getItem() {
            return item;
        }
    }

    @Getter
    @AllArgsConstructor
    public class CustomHolder implements InventoryHolder {

        private final Consumer<InventoryClickEvent> consumer;

        public Consumer<InventoryClickEvent> getConsumer() {
            return consumer;
        }

        @Override
        public @NotNull Inventory getInventory() {
            return InventoryBuilder.this.getInventory();
        }
    }

    /**
     * Private class to format the inventory with more than one parameter T
     */
    private class MultiValueInventoryFormat implements InventoryFormat<T> {

        private final List<T> items;
        private final ClickableItem<T> consumer;
        private final Map<Integer, ItemStack> map = new HashMap<>();

        public MultiValueInventoryFormat(List<T> items, ClickableItem<T> consumer) {
            this.items = items;
            this.consumer = consumer;
        }

        @Override
        public boolean isValid(int value) {
            return map.containsKey(value);
        }

        public void accept(InventoryClickEvent event, T value) {
            if (this.consumer == null) return;
            consumer.accept(event, value);
        }
    }

    /**
     * Private class to format the inventory with more than one item
     */
    private class MultiItemInventoryFormat implements InventoryFormat<T> {

        private final List<ItemStack> items;
        private final ClickableItem<T> consumer;
        private final Map<Integer, ItemStack> map = new HashMap<>();

        public MultiItemInventoryFormat(List<ItemStack> items, ClickableItem<T> consumer) {
            this.items = items;
            this.consumer = consumer;
        }

        @Override
        public boolean isValid(int value) {
            return map.containsKey(value);
        }

        public void accept(InventoryClickEvent event, T value) {
            consumer.accept(event, value);
        }
    }

    /**
     * Private class to format the inventory with just one item
     */
    @AllArgsConstructor
    private class SingleInventoryFormat implements InventoryFormat<T> {

        private final int slot;
        private final T value;
        private final ItemStack itemStack;
        private final ClickableItem<T> consumer;

        @Override
        public boolean isValid(int value) {
            return slot == value;
        }

        @Override
        public void accept(InventoryClickEvent event, T value) {
            if (this.consumer == null) return;
            consumer.accept(event, value);
        }
    }

    public static class Listener implements org.bukkit.event.Listener {

        @EventHandler
        public void onClick(InventoryClickEvent event) {
            if (!(event.getInventory().getHolder() instanceof InventoryBuilder.CustomHolder)) return;

            event.setCancelled(true);
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
                InventoryBuilder<?>.CustomHolder holder = (InventoryBuilder<?>.CustomHolder) event.getInventory().getHolder();
                holder.getConsumer().accept(event);
            }
        }
    }
}