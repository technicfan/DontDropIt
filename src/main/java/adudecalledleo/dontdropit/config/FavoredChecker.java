package adudecalledleo.dontdropit.config;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import adudecalledleo.dontdropit.ModKeyMappings;

public class FavoredChecker {
    private static final HashSet<Item> FAVORED_ITEMS = new HashSet<>();
    private static final HashSet<TagKey<Item>> FAVORED_ITEM_TAGS = new HashSet<>();
    private static final HashSet<Enchantment> FAVORED_ENCHANTMENTS = new HashSet<>();
    private static final HashSet<TagKey<Enchantment>> FAVORED_ENCHANTMENT_TAGS = new HashSet<>();

    public static void updateFavoredSets(ModConfig config) {
        updateFavoredSet(FAVORED_ITEMS, config.favorites.items, BuiltInRegistries.ITEM::getOptional);
        updateFavoredSet(FAVORED_ITEM_TAGS, config.favorites.itemTags, id -> {
            var key = TagKey.create(BuiltInRegistries.ITEM.key(), id);
            if (BuiltInRegistries.ITEM.containsKey(id)) {
                return Optional.of(key);
            } else {
                return Optional.empty();
            }
        });
        if (Minecraft.getInstance().level != null) {
            try {
                Registry<Enchantment> enchantmentRegistry = Minecraft.getInstance().level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
                updateFavoredSet(FAVORED_ENCHANTMENTS, config.favorites.enchantments, enchantmentRegistry::getOptional);
                updateFavoredSet(FAVORED_ENCHANTMENT_TAGS, config.favorites.enchantmentTags, id -> {
                    var key = TagKey.create(Registries.ENCHANTMENT, id);
                    if (enchantmentRegistry.containsKey(id)) {
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
        if (FAVORED_ITEM_TAGS.stream().anyMatch(stack::is))
            return true;
        Set<Holder<Enchantment>> enchs = EnchantmentHelper.getEnchantmentsForCrafting(stack).keySet();
        for (Holder<Enchantment> enchEntry : enchs) {
            boolean matching = FAVORED_ENCHANTMENTS.contains(enchEntry.value());
            if (!matching) {
                matching = FAVORED_ENCHANTMENT_TAGS.stream().anyMatch(enchEntry::is);
            }
            if (!matching) {
                continue;
            }

            boolean valid = true;
            if (config.favorites.enchIgnoreInvalidTargets && !(stack.getItem() == Items.ENCHANTED_BOOK)) {
                valid = enchEntry.value().canEnchant(stack);
            }
            if (valid) {
                return true;
            }
        }
        return false;
    }

    public static boolean canDropStack(ItemStack stack) {
        if (ModKeyMappings.isDown(ModKeyMappings.keyForceDrop))
            return true;
        return !isStackFavored(stack);
    }
}
