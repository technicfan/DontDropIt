package adudecalledleo.dontdropit.mixin.handledscreen;

import java.util.ArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import adudecalledleo.dontdropit.ModKeyBindings;
import adudecalledleo.dontdropit.config.DropBehaviorOverride;
import adudecalledleo.dontdropit.config.FavoredChecker;
import adudecalledleo.dontdropit.config.ModConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static adudecalledleo.dontdropit.ModKeyBindings.keyForceDrop;

@Mixin(AbstractContainerScreen.class)
public abstract class HandledScreenMixin_InterceptMouse<T extends AbstractContainerMenu> extends Screen {
    @Shadow @Final protected T menu;
    @Shadow protected int leftPos;
    @Shadow protected int topPos;
    @Shadow private int imageWidth, imageHeight;

    @Shadow protected abstract void slotClicked(Slot slot, int invSlot, int clickData, ClickType actionType);
    @Shadow protected abstract boolean hasClickedOutside(double mouseX, double mouseY, int left, int top);

    private HandledScreenMixin_InterceptMouse() {
        super(Component.empty());
        throw new RuntimeException("Mixin constructor called");
    }

    @Unique
    private boolean shouldAllowShiftClick(Slot slot, int invSlot, ClickType actionType) {
        if (!ModConfig.get().favorites.disableShiftClick || actionType != ClickType.QUICK_MOVE) {
            return true;
        }
        if (slot == null && invSlot >= 0) {
            slot = menu.slots.get(invSlot);
        }
        if (slot == null) {
            return true;
        }
        return !FavoredChecker.isStackFavored(slot.getItem());
    }

    @Unique
    private void disableFavoredShiftClick(Slot slot, int invSlot, int clickData, ClickType actionType) {
        if (shouldAllowShiftClick(slot, invSlot, actionType)) {
            slotClicked(slot, invSlot, clickData, actionType);
        }
    }

    @Unique
    private boolean dontdropit$cancelOnMouseClick;

    @ModifyArg(method = "mouseClicked", index = 0,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ClickType;)V",
                    ordinal = 1))
    public Slot disableFavoredShiftClick_mouseClicked(Slot slot, int invSlot, int clickData, ClickType actionType) {
        dontdropit$cancelOnMouseClick = !shouldAllowShiftClick(slot, invSlot, actionType);
        return slot;
    }

    @Inject(method = "slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ClickType;)V",
            at = @At("HEAD"),
            cancellable = true)
    public void cancelOnMouseClick(Slot slot, int slotId, int button, ClickType actionType, CallbackInfo ci) {
        if (dontdropit$cancelOnMouseClick) {
            dontdropit$cancelOnMouseClick = false;
            ci.cancel();
        }
    }

    @Redirect(method = "mouseReleased",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ClickType;)V",
                       ordinal = 0))
    public void disableFavoredShiftClick_mouseReleasedDouble(@SuppressWarnings("rawtypes") AbstractContainerScreen handledScreen,
            Slot slot, int invSlot, int clickData, ClickType actionType) {
        disableFavoredShiftClick(slot, invSlot, clickData, actionType);
    }

    @Redirect(method = "mouseReleased",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ClickType;)V",
                       ordinal = 9))
    public void oobClickDropOverride(@SuppressWarnings("rawtypes") AbstractContainerScreen handledScreen,
            Slot slot, int invSlot, int clickData, ClickType actionType) {
        if (actionType == ClickType.QUICK_MOVE) {
            disableFavoredShiftClick(slot, invSlot, clickData, actionType);
            return;
        }
        DropBehaviorOverride oobDropClickOverride = ModConfig.get().general.oobDropClickOverride;
        if (oobDropClickOverride == DropBehaviorOverride.DISABLED || invSlot != -999 || actionType != ClickType.PICKUP) {
            slotClicked(slot, invSlot, clickData, actionType);
            return;
        }
        ItemStack cursorStack = menu.getCarried();
        boolean forceDrop = ModKeyBindings.isDown(ModKeyBindings.keyForceDrop);
        boolean canDrop = true;
        switch (oobDropClickOverride) {
        case FAVORITE_ITEMS:
            if (!FavoredChecker.isStackFavored(cursorStack))
                break;
        case ALL_ITEMS:
            canDrop = false;
            break;
        default:
        }
        if (forceDrop || canDrop)
            slotClicked(null, -999, clickData, ClickType.PICKUP);
    }

    @Inject(method = "renderTooltip", at = @At("HEAD"))
    public void drawDropBlockTooltip(GuiGraphics context, int mouseX, int mouseY, CallbackInfo ci) {
        ItemStack cursorStack = menu.getCarried();
        if (cursorStack.isEmpty() || !hasClickedOutside(mouseX, mouseY, leftPos, topPos))
            return;
        ArrayList<Component> tooltipTexts = new ArrayList<>();
        boolean forceDrop = ModKeyBindings.isDown(keyForceDrop);
        boolean canDrop = false;
        switch (ModConfig.get().general.oobDropClickOverride) {
        case FAVORITE_ITEMS:
            if (FavoredChecker.isStackFavored(cursorStack))
                break;
        case DISABLED:
            canDrop = true;
            break;
        default:
        }
        if (forceDrop || canDrop) {
            tooltipTexts.add(Component.translatable("dontdropit.tooltip.drop.allowed")
                    .withStyle(style -> style.withBold(true).withBold(true).withColor(ChatFormatting.GREEN)));
            if (!canDrop) {
                tooltipTexts.add(Component.translatable("dontdropit.tooltip.drop.allowed.unblock_hint")
                        .withStyle(style -> style.withItalic(true).withColor(ChatFormatting.GRAY)));
            }
        } else {
            tooltipTexts.add(Component.translatable("dontdropit.tooltip.drop.blocked")
                    .withStyle(style -> style.withBold(true).withColor(ChatFormatting.RED)));
            if (keyForceDrop.isUnbound()) {
                tooltipTexts.add(Component.translatable("dontdropit.tooltip.drop.unblock_hint.unbound[0]",
                        ComponentUtils.wrapInSquareBrackets(Component.translatable(keyForceDrop.getName())
                                .withStyle(style -> style.withBold(true).withColor(ChatFormatting.WHITE))))
                        .withStyle(style -> style.withColor(ChatFormatting.GRAY)));
                tooltipTexts.add(Component.translatable("dontdropit.tooltip.drop.unblock_hint.unbound[1]")
                        .withStyle(style -> style.withColor(ChatFormatting.GRAY)));
            } else
                tooltipTexts.add(Component.translatable("dontdropit.tooltip.drop.unblock_hint",
                        ComponentUtils.wrapInSquareBrackets(Component.translatable(keyForceDrop.saveString())
                                .withStyle(style -> style.withBold(true).withColor(ChatFormatting.WHITE))))
                        .withStyle(style -> style.withColor(ChatFormatting.GRAY)));
        }
        context.setComponentTooltipForNextFrame(font, tooltipTexts, mouseX, mouseY);
    }
}
