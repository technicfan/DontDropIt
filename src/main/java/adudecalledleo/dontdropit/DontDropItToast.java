package adudecalledleo.dontdropit;

import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.Text;

public class DontDropItToast extends SystemToast {
    private static final Text TITLE = Text.translatable("key.dontdropit.toggleDropDelay");

    DontDropItToast(boolean disabled) {
        super(Type.DROP_DELAY_DISABLED_TOGGLED, TITLE, description(disabled));
    }

    private static Text description(boolean disabled) {
        return Text.translatable("dontdropit.toast.toggleDropDelay." + (disabled ? "disabled" : "enabled"));
    }

    static void show(ToastManager toastManager, boolean disabled) {
        DontDropItToast toast = toastManager.getToast(DontDropItToast.class, Type.DROP_DELAY_DISABLED_TOGGLED);
        if (toast != null) {
            toast.setContent(TITLE, description(disabled));
        } else {
            toastManager.add(new DontDropItToast(disabled));
        }
    }

    public static class Type extends SystemToast.Type {
        public static final Type DROP_DELAY_DISABLED_TOGGLED = new Type();
    }
}
