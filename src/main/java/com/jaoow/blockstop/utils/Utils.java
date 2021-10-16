package com.jaoow.blockstop.utils;

import org.bukkit.Material;

public class Utils {

    public static Material HEAD_MATERIAL = getSkullMaterial();

    private static Material getSkullMaterial() {
        Material material;
        try {
            material = Material.valueOf("PLAYER_HEAD");
        } catch (IllegalArgumentException e) {
            material = Material.valueOf("SKULL_ITEM");
        }
        return material;
    }
}
