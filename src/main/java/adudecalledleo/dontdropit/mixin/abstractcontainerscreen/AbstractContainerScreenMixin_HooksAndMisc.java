package adudecalledleo.dontdropit.mixin.abstractcontainerscreen;

import adudecalledleo.dontdropit.DropDelayRenderer;
import adudecalledleo.dontdropit.IgnoredSlots;
import adudecalledleo.dontdropit.ModKeyMappings;
import adudecalledleo.dontdropit.config.FavoredChecker;
import adudecalledleo.dontdropit.config.ModConfig;
import adudecalledleo.dontdropit.duck.HandledScreenHooks;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static adudecalledleo.dontdropit.ModKeyMappings.keyDropStack;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin_HooksAndMisc extends Screen implements HandledScreenHooks {
    @Shadow protected Slot hoveredSlot;

    @Shadow protected abstract void slotClicked(Slot slot, int invSlot, int clickData, ClickType actionType);

    private AbstractContainerScreenMixin_HooksAndMisc() {
        super(Component.empty());
        throw new RuntimeException("Mixin constructor called");
    }

    @Override
    public boolean dontdropit_canDrop() {
        if (minecraft == null || minecraft.player == null || hoveredSlot == null)
            return false;
        if (IgnoredSlots.isSlotIgnored(hoveredSlot))
            return false;
        return hoveredSlot.mayPickup(minecraft.player);
    }

    @Override
    public void dontdropit_drop(boolean entireStack) {
        if (hoveredSlot == null || dontdropit_getSelectedStack().isEmpty())
            return;
        slotClicked(hoveredSlot, hoveredSlot.index, entireStack ? 1 : 0, ClickType.THROW);
    }

    @Override
    public ItemStack dontdropit_getSelectedStack() {
        return hoveredSlot == null ? ItemStack.EMPTY : hoveredSlot.getItem();
    }

    @Redirect(method = "keyPressed",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;matches(Lnet/minecraft/client/input/KeyEvent;)Z",
                       ordinal = 2))
    public boolean disableDropKey(KeyMapping keyBinding, KeyEvent input) {
        if (hoveredSlot == null || IgnoredSlots.isSlotIgnored(hoveredSlot))
            return keyBinding.matches(input);
        ItemStack stack = dontdropit_getSelectedStack();
        if (ModConfig.get().dropDelay.mode.isEnabled(stack))
            return false;
        return dontdropit_canDrop()
                && FavoredChecker.canDropStack(dontdropit_getSelectedStack())
                && keyBinding.matches(input);
    }

    @Redirect(method = "keyPressed",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/client/input/KeyEvent;hasControlDown()Z"))
    public boolean useDropStackKey(KeyEvent input) {
        return ModKeyMappings.isDown(keyDropStack);
    }

    @Inject(method = "renderSlot",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/gui/GuiGraphics;renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
                     shift = At.Shift.AFTER))
    public void drawSlotProgressOverlay(GuiGraphics graphics, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        if (IgnoredSlots.isSlotIgnored(slot))
            return;
        DropDelayRenderer.renderOverlay(graphics, slot.getItem(), slot.x, slot.y);
    }
}
