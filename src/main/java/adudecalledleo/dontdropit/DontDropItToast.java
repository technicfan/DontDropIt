package adudecalledleo.dontdropit;

import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.network.chat.Component;

public class DontDropItToast extends SystemToast {
    private static final Component TITLE = Component.translatable("key.dontdropit.toggleDropDelay");

    DontDropItToast(boolean disabled) {
        super(Type.DROP_DELAY_DISABLED_TOGGLED, TITLE, description(disabled));
    }

    private static Component description(boolean disabled) {
        return Component.translatable("dontdropit.toast.toggleDropDelay." + (disabled ? "disabled" : "enabled"));
    }

    static void show(ToastManager toastManager, boolean disabled) {
        DontDropItToast toast = toastManager.getToast(DontDropItToast.class, Type.DROP_DELAY_DISABLED_TOGGLED);
        if (toast != null) {
            toast.reset(TITLE, description(disabled));
        } else {
            toastManager.addToast(new DontDropItToast(disabled));
        }
    }

    public static class Type extends SystemToast.SystemToastId {
        public static final Type DROP_DELAY_DISABLED_TOGGLED = new Type();
    }
}
