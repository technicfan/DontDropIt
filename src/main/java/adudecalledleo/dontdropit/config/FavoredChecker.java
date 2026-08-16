package adudecalledleo.dontdropit.config;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import adudecalledleo.dontdropit.ModKeyBindings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;

public class FavoredChecker {
    private static final HashSet<Item> FAVORED_ITEMS = new HashSet<>();
    private static final HashSet<TagKey<Item>> FAVORED_ITEM_TAGS = new HashSet<>();
    private static final HashSet<Enchantment> FAVORED_ENCHANTMENTS = new HashSet<>();
    private static final HashSet<TagKey<Enchantment>> FAVORED_ENCHANTMENT_TAGS = new HashSet<>();

    public static void updateFavoredSets(ModConfig config) {
        updateFavoredSet(FAVORED_ITEMS, config.favorites.items, Registries.ITEM::getOptionalValue);
        updateFavoredSet(FAVORED_ITEM_TAGS, config.favorites.itemTags, id -> {
            var key = TagKey.of(Registries.ITEM.getKey(), id);
            if (Registries.ITEM.containsId(id)) {
                return Optional.of(key);
            } else {
                return Optional.empty();
            }
        });
        if (MinecraftClient.getInstance().world != null) {
            try {
                Registry<Enchantment> enchantmentRegistry = MinecraftClient.getInstance().world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
                updateFavoredSet(FAVORED_ENCHANTMENTS, config.favorites.enchantments, enchantmentRegistry::getOptionalValue);
                updateFavoredSet(FAVORED_ENCHANTMENT_TAGS, config.favorites.enchantmentTags, id -> {
                    var key = TagKey.of(RegistryKeys.ENCHANTMENT, id);
                    if (enchantmentRegistry.containsId(id)) {
                        return Optional.of(key);
                    } else {
                        return Optional.empty();
                    }
                });
            } catch (IllegalStateException e) {}
        }
    }

    private static <T> void updateFavoredSet(HashSet<T> set, List<String> keys, Function<Identifier, Optional<T>> function) {
        set.clear();
        set.addAll(keys.stream()
                .map(s -> {
                    Identifier id = Identifier.tryParse(s);
                    if (id == null)
                        return null;
                    return function.apply(id).orElse(null);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
    }

    public static boolean isStackFavored(ItemStack stack) {
        if (stack.isEmpty())
            return false;
        ModConfig config = ModConfig.get();
        if (!config.favorites.enabled)
            return false;
        if (FAVORED_ITEMS.contains(stack.getItem()))
            return true;
        if (FAVORED_ITEM_TAGS.stream().anyMatch(stack::isIn))
            return true;
        Set<RegistryEntry<Enchantment>> enchs = EnchantmentHelper.getEnchantments(stack).getEnchantments();
        for (RegistryEntry<Enchantment> enchEntry : enchs) {
            boolean matching = FAVORED_ENCHANTMENTS.contains(enchEntry.value());
            if (!matching) {
                matching = FAVORED_ENCHANTMENT_TAGS.stream().anyMatch(enchEntry::isIn);
            }
            if (!matching) {
                continue;
            }

            boolean valid = true;
            if (config.favorites.enchIgnoreInvalidTargets && !(stack.getItem() == Items.ENCHANTED_BOOK)) {
                valid = enchEntry.value().isAcceptableItem(stack);
            }
            if (valid) {
                return true;
            }
        }
        return false;
    }

    public static boolean canDropStack(ItemStack stack) {
        if (ModKeyBindings.isDown(ModKeyBindings.keyForceDrop))
            return true;
        return !isStackFavored(stack);
    }
}
