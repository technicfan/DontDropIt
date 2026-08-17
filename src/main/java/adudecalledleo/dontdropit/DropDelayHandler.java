package adudecalledleo.dontdropit;

import static adudecalledleo.dontdropit.ModKeyMappings.keyDropStack;
import static adudecalledleo.dontdropit.ModKeyMappings.keyToggleDropDelay;

import adudecalledleo.dontdropit.config.DelayActivationMode;
import adudecalledleo.dontdropit.config.FavoredChecker;
import adudecalledleo.dontdropit.config.ModConfig;
import adudecalledleo.dontdropit.duck.HandledScreenHooks;
import adudecalledleo.dontdropit.mixin.KeyMappingAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class DropDelayHandler {
    private static long dropDelayCounter;
    private static ItemStack currentStack;
    private static boolean wasDropStackDown;
    private static boolean wasToggleDelayDown = false;

    public static void reset() {
        dropDelayCounter = 0;
        currentStack = ItemStack.EMPTY;
        wasDropStackDown = false;
    }

    static {
        reset();
    }

    public static void tick(Minecraft client) {
        if (client.player == null) {
            reset();
            wasToggleDelayDown = false;
            return;
        }
        if (ModKeyMappings.isDown(keyToggleDropDelay)) {
            if (!wasToggleDelayDown) {
                wasToggleDelayDown = true;
                var dropDelay = ModConfig.get().dropDelay;
                dropDelay.disabled = !dropDelay.disabled;
                DontDropItToast.show(client.getToastManager(), dropDelay.disabled);
            }
        } else
            wasToggleDelayDown = false;
        if (client.screen != null) {
            if (client.screen instanceof HandledScreenHooks)
                tickOnHandledScreen(client, (HandledScreenHooks) client.screen);
            else
                reset();
        } else
            tickNormally(client);
    }

    private static void tickNormally(Minecraft client) {
        if (client.player == null)
            return;
        ItemStack stack = client.player.getInventory().getSelectedItem();
        if (ModConfig.get().dropDelay.isEnabled(stack)) {
            if (client.player.isSpectator()) {
                reset();
                return;
            }
            doDropProgress(client, stack, entireStack -> {
                if (client.player.drop(entireStack))
                    client.player.swing(InteractionHand.MAIN_HAND);
            });
        } else {
            reset();
            while (client.options.keyDrop.consumeClick()) {
                if (FavoredChecker.isStackFavored(stack))
                    continue;
                if (!client.player.isSpectator() && client.player.drop(ModKeyMappings.isDown(keyDropStack)))
                    client.player.swing(InteractionHand.MAIN_HAND);
            }
        }
    }

    private static void tickOnHandledScreen(Minecraft client, HandledScreenHooks screenHooks) {
        if (client.player == null)
            return;
        ItemStack stack = screenHooks.dontdropit_getSelectedStack();
        if (ModConfig.get().dropDelay.mode.isEnabled(stack)) {
            if (client.player.isSpectator() || !screenHooks.dontdropit_canDrop()) {
                reset();
                return;
            }
            doDropProgress(client, stack, screenHooks::dontdropit_drop);
        }
        else
            reset();
    }

    @FunctionalInterface
    private interface DropAction {
        void drop(boolean entireStack);
    }

    private static void doDropProgress(Minecraft client, ItemStack stack, DropAction dropAction) {
        if (ModKeyMappings.isDown(client.options.keyDrop)) {
            ((KeyMappingAccessor) client.options.keyDrop).setTimesPressed(0); // eat all presses!
            if (dropDelayCounter < getCounterMax()) {
                boolean isDropStackDown = ModKeyMappings.isDown(keyDropStack) && stack.getCount() > 1;
                if (dropDelayCounter == 0)
                    wasDropStackDown = isDropStackDown;
                else if (wasDropStackDown != isDropStackDown) {
                    dropDelayCounter = 0;
                    return;
                }
                if (ModConfig.get().dropDelay.mode == DelayActivationMode.ENABLED) {
                    if (!FavoredChecker.canDropStack(stack)) {
                        dropDelayCounter = 0;
                        return;
                    }
                }
                if (dropDelayCounter > 0 && stack != currentStack)
                    dropDelayCounter = -1;
                currentStack = stack;
                dropDelayCounter++;
            } else {
                dropDelayCounter = ModConfig.get().dropDelay.doDelayOnce ? getCounterMax() : 0;
                dropAction.drop(wasDropStackDown);
            }
        } else
            reset();
    }

    public static long getCounter() {
        return dropDelayCounter;
    }

    public static long getCounterMax() {
        return ModConfig.get().dropDelay.ticks;
    }

    public static ItemStack getCurrentStack() {
        return currentStack;
    }

    public static boolean isDroppingEntireStack() {
        return wasDropStackDown;
    }
}
