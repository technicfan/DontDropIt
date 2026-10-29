package adudecalledleo.dontdropit;

import adudecalledleo.dontdropit.config.FavouredChecker;
import adudecalledleo.dontdropit.config.ModConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class DropDelayRenderer {
    private static final Identifier TEX_FAVOURITE = DontDropIt.id("textures/gui/favourite.png");

    public static void renderFavouriteIcon(GuiGraphics graphics, ItemStack stack, int x, int y) {
        if (!ModConfig.get().favourites.drawOverlay || !FavouredChecker.isStackFavoured(stack))
            return;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEX_FAVOURITE, x, y, 18, 18, 18, 18, 18, 18);
    }

    private static int pack(int r, int g, int b, int a) {
        return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF;
    }

    private static final int COLOR_FORCE = pack(0xFF, 0x00, 0x00, 0x30);
    private static final int COLOR_PROGRESS = pack(0x00, 0xFF, 0x00, 0x30);

    public static void renderProgressOverlay(GuiGraphics graphics, ItemStack stack, float partialTick, int x, int y, int w, int h) {
        ItemStack currentStack = DropDelayHandler.getCurrentStack();
        if (currentStack.isEmpty() || currentStack != stack)
            return;
        if (!ModConfig.get().dropDelay.mode.isEnabled(stack))
            return;
        if ((stack.getCount() > 1 && DropDelayHandler.isDroppingEntireStack())
                || (ModKeyMappings.isDown(ModKeyMappings.keyForceDrop) && FavouredChecker.isStackFavoured(stack)))
            graphics.fill(x, y, x + w, y + h, COLOR_FORCE);
        long counter = DropDelayHandler.getCounter();
        float progHeight = ((counter + partialTick) / DropDelayHandler.getCounterMax()) * h;
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y + h - progHeight);
        graphics.pose().scale(w, progHeight);
        graphics.fill(0, 0, 1, 1, COLOR_PROGRESS);
        graphics.pose().popMatrix();
    }

    public static void renderOverlay(GuiGraphics graphics, ItemStack stack, DeltaTracker tickCounter, int x, int y) {
        if (stack.isEmpty())
            return;
        renderFavouriteIcon(graphics, stack, x - 1, y - 1);
        renderProgressOverlay(graphics, stack, tickCounter.getGameTimeDeltaPartialTick(false), x, y, 16, 16);
    }
}
