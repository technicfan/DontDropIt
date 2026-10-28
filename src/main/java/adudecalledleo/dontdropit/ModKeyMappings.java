package adudecalledleo.dontdropit;

import com.mojang.blaze3d.platform.InputConstants;

import adudecalledleo.dontdropit.config.ModConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ModKeyMappings {
    private ModKeyMappings() { }

    private static final KeyMapping.Category MOD_CATEGORY = KeyMapping.Category.register(DontDropIt.id(DontDropIt.MOD_ID));

    public static final KeyMapping keyDropStack = new KeyMapping("key.dontdropit.dropStack",
            InputConstants.KEY_LCONTROL, MOD_CATEGORY);
    public static final KeyMapping keyForceDrop = new KeyMapping("key.dontdropit.forceDrop",
            InputConstants.KEY_LALT, MOD_CATEGORY);
    public static final KeyMapping keyToggleDropDelay = new KeyMapping("key.dontdropit.toggleDropDelay",
            InputConstants.UNKNOWN.getValue(), MOD_CATEGORY);
    public static final KeyMapping keyToggleFavourite = new KeyMapping("key.dontdropit.toggleFavourite", InputConstants.KEY_X, MOD_CATEGORY);

    public static final KeyMapping[] all = new KeyMapping[] { keyDropStack, keyForceDrop, keyToggleDropDelay, keyToggleFavourite };

    public static void register() {
        for (KeyMapping keyMapping : all)
            KeyBindingHelper.registerKeyBinding(keyMapping);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!ModConfig.get().favourites.disableFavKeyOutsideContainer) {
                while (keyToggleFavourite.consumeClick()) {
                    if (client.player != null && client/*?if >=26.2 {*//*.gui.screen()*//*?} else {*/.screen/*?}*/ == null) {
                        ItemStack stack = client.player.getMainHandItem();
                        if (stack != null && !stack.is(Items.AIR)) {
                            ModConfig.get().toggleFavouriteItem(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
                            ModConfig.save();
                        }
                    }
                }
            }
        });
    }

    public static boolean isDown(KeyMapping keyMapping) {
        if (keyMapping.isUnbound())
            return false;
        return keyMapping.isDown();
    }
}
