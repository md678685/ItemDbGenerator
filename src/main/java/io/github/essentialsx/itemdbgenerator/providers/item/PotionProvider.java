package io.github.essentialsx.itemdbgenerator.providers.item;

import java.util.*;
import java.util.stream.Stream;
import org.bukkit.Material;
import org.bukkit.potion.PotionType;

public class PotionProvider implements ItemProvider {
    private static final Material[] MATERIALS = {
        Material.POTION,
        Material.SPLASH_POTION,
        Material.LINGERING_POTION,
        Material.TIPPED_ARROW
    };

    private static final Map<PotionType, String> MOJANG_NAMES = new HashMap<>();
    static {
        MOJANG_NAMES.put(PotionType.UNCRAFTABLE, "empty");
        MOJANG_NAMES.put(PotionType.JUMP, "leaping");
        MOJANG_NAMES.put(PotionType.SPEED, "swiftness");
        MOJANG_NAMES.put(PotionType.INSTANT_HEAL, "healing");
        MOJANG_NAMES.put(PotionType.INSTANT_DAMAGE, "harming");
        MOJANG_NAMES.put(PotionType.REGEN, "regeneration");
    }

    @Override
    public Stream<Item> get() {
        return Arrays.stream(PotionType.values())
            .flatMap(PotionProvider::getPotionsForType);
    }

    public static Stream<Item> getPotionsForType(PotionType type) {
        return Arrays.stream(MATERIALS)
            .flatMap(material -> {
                Set<PotionItem> items = new HashSet<>();
                items.add(new PotionItem(material, type, false, false));
                if (type.isUpgradeable()) {
                    items.add(new PotionItem(material, type, true, false));
                }
                if (type.isExtendable()) {
                    items.add(new PotionItem(material, type, false, true));
                }
                return items.stream();
            });
    }

    public static class PotionItem extends Item {
        private final PotionData potionData;

        public PotionItem(Material material, PotionType type, boolean upgraded, boolean extended) {
            super(material);
            potionData = new PotionData(type, upgraded, extended);
        }

        @Override
        public String getName() {
            return potionData.getMojangName() + "_" + getMaterial().name().toLowerCase();
        }

        public PotionData getPotionData() {
            return potionData;
        }
    }

    public static class PotionData {
        private final PotionType type;
        private final boolean upgraded;
        private final boolean extended;

        PotionData(PotionType type, boolean upgraded, boolean extended) {
            this.type = type;
            this.upgraded = upgraded;
            this.extended = extended;
        }

        public PotionType getType() {
            return type;
        }

        public boolean isUpgraded() {
            return upgraded;
        }

        public boolean isExtended() {
            return extended;
        }

        public String getMojangName() {
            String baseName;

            if (MOJANG_NAMES.containsKey(type)) {
                baseName = MOJANG_NAMES.get(type);
            } else {
                baseName = type.name().toLowerCase();
            }

            if (isExtended()) {
                return "long_" + baseName;
            } else if (isUpgraded()) {
                return "strong_" + baseName;
            }

            return baseName;
        }
    }
}
