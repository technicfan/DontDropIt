package adudecalledleo.dontdropit;

import adudecalledleo.dontdropit.config.FavoredChecker;
import adudecalledleo.dontdropit.config.ModConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class DropDelayRenderer {
    private static final Identifier TEX_FAVORITE = DontDropIt.id("textures/gui/favorite.png");

    public static void renderFavoriteIcon(GuiGraphics graphics, ItemStack stack, int x, int y) {
        if (!ModConfig.get().favorites.drawOverlay || !FavoredChecker.isStackFavored(stack))
            return;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEX_FAVORITE, x, y, 18, 18, 18, 18, 18, 18);
    }

    private static int pack(int r, int g, int b, int a) {
        return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF;
    }

    private static final int COLOR_FORCE = pack(0xFF, 0x00, 0x00, 0x30);
    private static final int COLOR_PROGRESS = pack(0x00, 0xFF, 0x00, 0x30);

    public static void renderProgressOverlay(GuiGraphics graphics, ItemStack stack, int x, int y, int w, int h) {
        ItemStack currentStack = DropDelayHandler.getCurrentStack();
        if (currentStack.isEmpty() || currentStack != stack)
            return;
        if (!ModConfig.get().dropDelay.mode.isEnabled(stack))
            return;
        if ((stack.getCount() > 1 && DropDelayHandler.isDroppingEntireStack())
                || ModKeyMappings.isDown(ModKeyMappings.keyForceDrop))
            graphics.fill(x, y, x + w, y + h, COLOR_FORCE);
        long counter = DropDelayHandler.getCounter();
        int progHeight = Mth.floor((counter / (double) DropDelayHandler.getCounterMax()) * h);
        graphics.fill(x, y + h - progHeight, x + w, y + h, COLOR_PROGRESS);
    }

    public static void renderOverlay(GuiGraphics graphics, ItemStack stack, int x, int y) {
        if (stack.isEmpty())
            return;
        graphics.pose().pushMatrix();
        renderFavoriteIcon(graphics, stack, x - 1, y - 1);
        renderProgressOverlay(graphics, stack, x, y, 16, 16);
        graphics.pose().popMatrix();
    }
}
