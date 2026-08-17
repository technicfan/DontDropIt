package adudecalledleo.dontdropit.mixin;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Redirect(method = "handleKeybinds",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z",
                       ordinal = 8))
    public boolean disableDropKey(KeyMapping keyMapping) {
        return false;
    }
}
