package adudecalledleo.dontdropit.mixin;

import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

import adudecalledleo.dontdropit.ModKeyBindings;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseMixin {
    @Shadow @Final private Minecraft minecraft;
    @Shadow private double xpos;
    @Shadow private double ypos;

    @Inject(method = "onButton", at = @At("HEAD"))
    public void updateModKeys(long window, MouseButtonInfo input, int action, CallbackInfo ci) {
        // this forces our key bindings (and the drop key binding) to be updated in handled screens
        // not required for mouse bindings, but is much more convenient than handling them manually
        if (minecraft.getWindow().handle() == window && minecraft.screen instanceof AbstractContainerScreen<?> screen) {
            GuiEventListener hoveredElement = screen.getChildAt(xpos, ypos).orElse(null);
            if (hoveredElement instanceof AbstractWidget) {
                // mouse is over something clickable, don't update keys!
                return;
            }

            KeyMapping targetBinding = null;
            if (minecraft.options.keyDrop.matchesMouse(new MouseButtonEvent(xpos, ypos, input)))
                targetBinding = minecraft.options.keyDrop;
            else {
                for (KeyMapping keyBinding : ModKeyBindings.all) {
                    if (keyBinding.matchesMouse(new MouseButtonEvent(xpos, ypos, input))) {
                        targetBinding = keyBinding;
                        break;
                    }
                }
            }

            if (targetBinding == null)
                return;
            if (action == GLFW_RELEASE)
                targetBinding.setDown(false);
            else {
                targetBinding.setDown(true);
                ((KeyBindingAccessor) targetBinding).setTimesPressed(((KeyBindingAccessor) targetBinding).getTimesPressed() + 1);
            }
        }
    }
}
