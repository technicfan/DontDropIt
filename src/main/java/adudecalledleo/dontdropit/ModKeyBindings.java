package adudecalledleo.dontdropit;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

public class ModKeyBindings {
    private ModKeyBindings() { }

    private static final KeyBinding.Category MOD_CATEGORY = KeyBinding.Category.create(Identifier.of(DontDropIt.MOD_ID, DontDropIt.MOD_ID));

    public static final KeyBinding keyDropStack = new KeyBinding("key.dontdropit.dropStack",
            InputUtil.GLFW_KEY_LEFT_CONTROL, MOD_CATEGORY);
    public static final KeyBinding keyForceDrop = new KeyBinding("key.dontdropit.forceDrop",
            InputUtil.GLFW_KEY_LEFT_ALT, MOD_CATEGORY);
    public static final KeyBinding keyToggleDropDelay = new KeyBinding("key.dontdropit.toggleDropDelay",
            InputUtil.UNKNOWN_KEY.getCode(), MOD_CATEGORY);

    public static final KeyBinding[] all = new KeyBinding[] { keyDropStack, keyForceDrop, keyToggleDropDelay };

    public static void register() {
        for (KeyBinding keyBinding : all)
            KeyBindingHelper.registerKeyBinding(keyBinding);
    }

    public static boolean isDown(KeyBinding keyBinding) {
        if (keyBinding.isUnbound())
            return false;
        return keyBinding.isPressed();
    }
}
