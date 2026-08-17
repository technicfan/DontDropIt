package adudecalledleo.dontdropit.mixin.handledscreen;

import adudecalledleo.dontdropit.config.FavoredChecker;
import adudecalledleo.dontdropit.config.ModConfig;
import adudecalledleo.dontdropit.mixin.KeyBindingAccessor;
import adudecalledleo.dontdropit.mixin.SlotAccessor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class HandledScreenMixin_InterceptClose<T extends AbstractContainerMenu> extends Screen {
    @Shadow @Final protected T menu;

    @Shadow protected abstract void slotClicked(Slot slot, int slotId, int button, ClickType actionType);

    private HandledScreenMixin_InterceptClose() {
        super(Component.empty());
        throw new RuntimeException("Mixin constructor called");
    }

    @Unique private Inventory playerInventory;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void capturePlayerInventory(T handler, Inventory inventory, Component title, CallbackInfo ci) {
        this.playerInventory = inventory;
    }

    @Inject(method = "onClose", at = @At("HEAD"))
    public void cursorCloseDropOverride(CallbackInfo ci) {
        if (minecraft == null || minecraft.player == null)
            return;
        // eat all drop key presses, so we don't drop hotbar items if drop delay is disabled
        minecraft.options.keyDrop.setDown(false);
        ((KeyBindingAccessor) minecraft.options.keyDrop).setTimesPressed(0);

        ItemStack cursorStack = menu.getCarried();
        boolean canDrop = true;
        switch (ModConfig.get().general.cursorCloseDropOverride) {
        case FAVORITE_ITEMS:
            if (!FavoredChecker.isStackFavored(cursorStack))
                break;
        case ALL_ITEMS:
            canDrop = false;
            break;
        default:
        }
        if (cursorStack.isEmpty() || canDrop)
            return;
        int targetInvId;
        targetInvId = playerInventory.getFreeSlot();
        if (targetInvId < 0)
            targetInvId = playerInventory.getSlotWithRemainingSpace(cursorStack);
        if (targetInvId >= 0) {
            // locate handler slot ID that matches the target inventory slot ID
            Slot targetSlot = null;
            for (Slot slot : menu.slots) {
                if (slot.container == playerInventory && ((SlotAccessor) slot).getInventoryIndex() == targetInvId) {
                    targetSlot = slot;
                    break;
                }
            }
            if (targetSlot == null)
                return;
            slotClicked(targetSlot, -1, 0, ClickType.PICKUP);
        }
    }
}
