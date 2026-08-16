package adudecalledleo.dontdropit;

import adudecalledleo.dontdropit.config.FavoredChecker;
import adudecalledleo.dontdropit.config.ModConfig;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class DropDelayRenderer {
    private static final Identifier TEX_FAVORITE = DontDropIt.id("textures/gui/favorite.png");

    public static void renderFavoriteIcon(DrawContext context, ItemStack stack, int x, int y) {
        if (!ModConfig.get().favorites.drawOverlay || !FavoredChecker.isStackFavored(stack))
            return;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEX_FAVORITE, x, y, 18, 18, 18, 18, 18, 18);
    }

    private static int pack(int r, int g, int b, int a) {
        return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF;
    }

    private static final int COLOR_FORCE = pack(0xFF, 0x00, 0x00, 0x30);
    private static final int COLOR_PROGRESS = pack(0x00, 0xFF, 0x00, 0x30);

    public static void renderProgressOverlay(DrawContext context, ItemStack stack, int x, int y, int w, int h) {
        ItemStack currentStack = DropDelayHandler.getCurrentStack();
        if (currentStack.isEmpty() || currentStack != stack)
            return;
        if (!ModConfig.get().dropDelay.mode.isEnabled(stack))
            return;
        if ((stack.getCount() > 1 && DropDelayHandler.isDroppingEntireStack())
                || ModKeyBindings.isDown(ModKeyBindings.keyForceDrop))
            context.fill(x, y, x + w, y + h, COLOR_FORCE);
        long counter = DropDelayHandler.getCounter();
        int progHeight = MathHelper.floor((counter / (double) DropDelayHandler.getCounterMax()) * h);
        context.fill(x, y + h - progHeight, x + w, y + h, COLOR_PROGRESS);
    }

    public static void renderOverlay(DrawContext context, ItemStack stack, int x, int y) {
        if (stack.isEmpty())
            return;
        context.getMatrices().pushMatrix();
        renderFavoriteIcon(context, stack, x - 1, y - 1);
        renderProgressOverlay(context, stack, x, y, 16, 16);
        context.getMatrices().popMatrix();
    }
}
