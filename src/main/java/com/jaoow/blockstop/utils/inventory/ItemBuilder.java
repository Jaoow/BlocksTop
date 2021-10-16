package com.jaoow.blockstop.utils.inventory;

import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author Jaoow
 * @version 1.0
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
public class ItemBuilder implements Cloneable {

    private final ItemStack item; // Final item.

    /**
     * Create a new ItemBuilder
     * object.
     *
     * @param item final item to add.
     */
    public ItemBuilder(ItemStack item) {
        Validate.notNull(item, "@Item cannot be null.");
        this.item = item;
    }

    /**
     * Create a new ItemBuilder
     * object.
     *
     * @param material material type to create.
     */
    public ItemBuilder(Material material) {
        this(material, 1);
    }

    /**
     * Create a new ItemBuilder
     * object.
     *
     * @param material material type to create.
     * @param amount   amount of items.
     */
    public ItemBuilder(Material material, int amount) {
        Validate.notNull(material, "@Material cannot be null.");

        if (StringUtils.equals(material.name(), "SKULL_ITEM")) {
            this.item = new ItemStack(material, amount, (short) 3);
        } else {
            this.item = new ItemStack(material, amount);
        }
    }

    /**
     * Create a new ItemBuilder
     * object.
     *
     * @param material   material type to create.
     * @param amount     amount of items.
     * @param durability item durability/data.
     */
    @Deprecated
    public ItemBuilder(Material material, int amount, short durability) {
        Validate.notNull(material, "@Material cannot be null.");

        if (StringUtils.equals(material.name(), "SKULL_ITEM") && durability != 3) {
            this.item = new ItemStack(material, amount, (short) 3);
        } else {
            item = new ItemStack(material, amount, durability);
        }
    }

    /**
     * Create a new ItemBuilder
     * from a ItemStack.
     *
     * @param item original item
     * @return {@link ItemBuilder}
     */
    public static ItemBuilder from(ItemStack item) {
        return new ItemBuilder(item);
    }

    /**
     * Builds a new {@link ItemBuilder}
     * object by the configuration
     * section item.
     *
     * @param section      section to value the item information.
     * @return {@link ItemBuilder}
     */
    public static ItemBuilder fromSection(ConfigurationSection section) {
        Validate.notNull(section, "section cannot be null.");

        ItemBuilder builder;

        try {
            String type = StringUtils.replace(section.getString("material", "AIR"), " ", "").toUpperCase();
            int amount = section.contains("amount") ? section.getInt("amount") : 1;

            if (StringUtils.contains(type, ":")) {
                String[] typeSplit = type.split(":");
                short durability = Short.parseShort(typeSplit[1]);

                builder = new ItemBuilder(getMaterial(typeSplit[0]), amount, durability);
            } else {
                builder = new ItemBuilder(getMaterial(type), amount);
            }

            if (section.contains("name")) {
                builder.withName(section.getString("name"));
            }

            if (section.contains("lore")) {
                builder.withLore(section.getStringList("lore"));
            }
        } catch (NullPointerException | NumberFormatException | IndexOutOfBoundsException ex) {
            return new ItemBuilder(Material.BARRIER).withName("&cInvalid Item").addLore("&7Where: " + section.getCurrentPath());
        }

        return builder;
    }

    @Nullable
    public static Material getMaterial(String type) {
        if (type == null || type.isEmpty()) return null;
        return Material.matchMaterial(type.toUpperCase());
    }

    private static String applyPlaceholder(String text, String[] placeholders, String[] replacers) {
        if (text == null || text.isEmpty()) return text;

        if (placeholders != null && placeholders.length > 0 && placeholders.length == replacers.length) {
            text = StringUtils.replaceEach(text, placeholders, replacers);
        }

        return text;
    }

    /**
     * Get display item name.
     *
     * @return the item display name
     */
    @Nullable
    public String getItemName() {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getDisplayName();
    }

    /**
     * Get item lore.
     *
     * @return the item lore
     */
    @Nullable
    public List<String> getItemLore() {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return new ArrayList<>();
        return meta.getLore();
    }

    public ItemBuilder setOwner(UUID owner) {
        return setOwner(Bukkit.getOfflinePlayer(owner).getName());
    }

    public ItemBuilder setOwner(String owner) {
        if (!(item.getItemMeta() instanceof SkullMeta)) return this;
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        meta.setOwner(owner);
        item.setItemMeta(meta);
        return this;
    }


    /**
     * Set display item name.
     *
     * @param name the name.
     * @return {@link ItemBuilder}
     */
    public ItemBuilder withName(String name) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return this;

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        item.setItemMeta(meta);
        return this;
    }

    /**
     * Set item lore.
     * <p>
     * Using the methods "withName" will
     * set the given name.
     * To add new lines on the
     * current item lore
     *
     * @param name         the name to set.
     * @param placeholders the custom placeholders.
     * @param replaces     the placeholder replaces.
     * @return {@link ItemBuilder}
     */
    public ItemBuilder withName(String name, String[] placeholders, String[] replaces) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || name == null) return this;

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', applyPlaceholder(name, placeholders, replaces)));
        item.setItemMeta(meta);
        return this;
    }

    /**
     * Set item lore.
     * <p>
     * Using the methods "withLore" will
     * set the given lore.
     * To add new lines on the
     * current item lore, use "addLore"
     * methods.
     *
     * @param lore the lore to set.
     * @return {@link ItemBuilder}
     */
    public ItemBuilder withLore(List<String> lore) {
        return withLore(lore, null, null);
    }

    /**
     * Set item lore.
     * <p>
     * Using the methods "withLore" will
     * set the given lore.
     * To add new lines on the
     * current item lore, use "addLore"
     * methods.
     *
     * @param lore the lore to set.
     * @return {@link ItemBuilder}
     */
    public ItemBuilder withLore(String[] lore) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || lore == null || lore.length <= 0) return this;

        List<String> ret = new ArrayList<>(lore.length);
        Collections.addAll(ret, lore);

        return withLore(ret, null, null);
    }

    /**
     * Set item lore.
     * <p>
     * Using the methods "withLore" will
     * set the given lore.
     * To add new lines on the
     * current item lore, use "addLore"
     * methods.
     *
     * @param lore         the lore to set.
     * @param placeholders the custom placeholders.
     * @param replaces     the placeholder replaces.
     * @return {@link ItemBuilder}
     */
    public ItemBuilder withLore(List<String> lore, String[] placeholders, String[] replaces) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || lore == null || lore.size() <= 0) return this;

        List<String> toAdd = new ArrayList<>(lore.size());

        for (String str : lore) {
            toAdd.add(ChatColor.translateAlternateColorCodes('&', applyPlaceholder(str, placeholders, replaces)));
        }

        meta.setLore(toAdd);
        item.setItemMeta(meta);
        return this;
    }

    /**
     * Add new lines to
     * the current item lore.
     *
     * @param lore lore lines to be added.
     * @return {@link ItemBuilder}
     */
    public ItemBuilder addLore(List<String> lore) {
        return addLore(lore, null, null);
    }

    /**
     * Add new lines to
     * the current item lore.
     *
     * @param lore line to be added.
     * @return {@link ItemBuilder}
     */
    public ItemBuilder addLore(String lore) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || lore == null || lore.isEmpty()) return this;

        return addLore(new String[]{lore});
    }

    /**
     * Add new lines to
     * the current item lore.
     *
     * @param lore lore lines to be added.
     * @return {@link ItemBuilder}
     */
    public ItemBuilder addLore(String[] lore) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || lore == null) return this;

        List<String> ret = new ArrayList<>(lore.length);

        Collections.addAll(ret, lore);

        return addLore(ret, null, null);
    }

    /**
     * Add new lines to
     * the current item lore.
     *
     * @param lore         lore lines to be added.
     * @param placeholders the custom placeholders.
     * @param replaces     the placeholder replaces
     * @return {@link ItemBuilder}
     */
    public ItemBuilder addLore(List<String> lore, String[] placeholders, String[] replaces) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || lore == null || lore.size() <= 0) return this;

        List<String> toAdd;

        if (item.hasItemMeta() && item.getItemMeta().getLore() != null) {
            List<String> oldLore = item.getItemMeta().getLore();
            toAdd = new ArrayList<>(oldLore.size() + lore.size());

            toAdd.addAll(oldLore);
        } else {
            toAdd = new ArrayList<>(lore.size());
        }

        for (String str : lore) {
            toAdd.add(ChatColor.translateAlternateColorCodes('&', applyPlaceholder(str, placeholders, replaces)));
        }

        meta.setLore(toAdd);
        item.setItemMeta(meta);
        return this;
    }

    /**
     * Build the current itemStack
     * and replace the given placeholders.
     *
     * @param placeholder placeholders
     * @param replace     replacements
     * @return the built itemStack.
     */
    public ItemStack build(String[] placeholder, String[] replace) {
        ItemStack item = this.item;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return this.item;

        if (meta.hasDisplayName()) {
            withName(meta.getDisplayName(), placeholder, replace);
        }

        if (meta.hasLore()) {
            withLore(meta.getLore(), placeholder, replace);
        }
        return this.item;
    }

    /**
     * Build the current
     * itemStack.
     *
     * @return the built itemStack.
     */
    public ItemStack build() {
        return item;
    }

    /**
     * Get the current item builder
     * as a copy.
     *
     * @return the item builder as copy
     */
    public ItemBuilder asCopy() {
        return new ItemBuilder(this.item.clone());
    }

    @SneakyThrows
    public ItemBuilder clone() {
        return (ItemBuilder) super.clone();
    }

}