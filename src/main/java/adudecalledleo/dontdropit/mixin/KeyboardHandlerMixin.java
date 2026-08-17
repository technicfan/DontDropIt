package adudecalledleo.dontdropit.mixin;

import adudecalledleo.dontdropit.ModKeyMappings;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.platform.InputConstants;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "keyPress", at = @At(value = "HEAD"))
    public void updateModKeys(long window, int action, KeyEvent input, CallbackInfo ci) {
        // this forces our key bindings (and the drop key binding) to be updated in handled screens
        // this allows scancodes to work properly, since you can't poll them via GLFW
        if (minecraft.getWindow().handle() == window && minecraft.screen instanceof AbstractContainerScreen<?> screen) {
            if (screen.getFocused() instanceof EditBox textFieldWidget) {
                if (textFieldWidget.canConsumeInput()) {
                    // a text field widget is active, don't update keys!
                    return;
                }
            }

            KeyMapping targetBinding = null;
            if (minecraft.options.keyDrop.matches(input))
                targetBinding = minecraft.options.keyDrop;
            else {
                for (KeyMapping keyBinding : ModKeyMappings.all) {
                    if (keyBinding.matches(input)) {
                        targetBinding = keyBinding;
                        break;
                    }
                }
            }
            if (targetBinding == null)
                return;
            if (action == InputConstants.RELEASE)
                targetBinding.setDown(false);
            else {
                targetBinding.setDown(true);
                ((KeyMappingAccessor) targetBinding).setTimesPressed(((KeyMappingAccessor) targetBinding).getTimesPressed() + 1);
            }
        }
    }
}
