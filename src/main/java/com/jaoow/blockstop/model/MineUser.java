package com.jaoow.blockstop.model;

import com.google.common.collect.Maps;
import lombok.Data;
import org.bukkit.Material;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Data
public class MineUser {

    private final UUID uniqueId;
    private final LinkedHashMap<Material, Double> materialMap = Maps.newLinkedHashMap();

    public void addMaterial(Material material) {
        materialMap.compute(material, (k, v) -> v != null ? v + 1 : 1);
    }

    public double getMinedAmount() {
        return this.materialMap.values().stream().mapToDouble(v -> v).sum();
    }

    public double getMinedOres() {
        return this.materialMap.entrySet().stream().filter(entry -> entry.getKey().name().contains("ORE")).mapToDouble(Map.Entry::getValue).sum();
    }
}
