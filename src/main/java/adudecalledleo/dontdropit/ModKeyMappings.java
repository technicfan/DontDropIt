package adudecalledleo.dontdropit;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class ModKeyMappings {
    private ModKeyMappings() { }

    private static final KeyMapping.Category MOD_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(DontDropIt.MOD_ID, DontDropIt.MOD_ID));

    public static final KeyMapping keyDropStack = new KeyMapping("key.dontdropit.dropStack",
            InputConstants.KEY_LCONTROL, MOD_CATEGORY);
    public static final KeyMapping keyForceDrop = new KeyMapping("key.dontdropit.forceDrop",
            InputConstants.KEY_LALT, MOD_CATEGORY);
    public static final KeyMapping keyToggleDropDelay = new KeyMapping("key.dontdropit.toggleDropDelay",
            InputConstants.UNKNOWN.getValue(), MOD_CATEGORY);

    public static final KeyMapping[] all = new KeyMapping[] { keyDropStack, keyForceDrop, keyToggleDropDelay };

    public static void register() {
        for (KeyMapping keyMapping : all)
            KeyBindingHelper.registerKeyBinding(keyMapping);
    }

    public static boolean isDown(KeyMapping keyMapping) {
        if (keyMapping.isUnbound())
            return false;
        return keyMapping.isDown();
    }
}
