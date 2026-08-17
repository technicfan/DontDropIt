package adudecalledleo.dontdropit.config;

import java.util.Collections;
import java.util.Locale;

import me.shedaniel.autoconfig.AutoConfigClient;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import static me.shedaniel.autoconfig.util.Utils.getUnsafely;
import static me.shedaniel.autoconfig.util.Utils.setUnsafely;

public enum DelayActivationMode {
    ENABLED, FAVORITES_ONLY, DISABLED;

    public boolean isEnabled(ItemStack stack) {
        return switch (this) {
            case DISABLED -> false;
            case FAVORITES_ONLY -> FavoredChecker.isStackFavored(stack);
            case ENABLED -> true;
        };
    }

    public static Component toText(Object obj) {
        if (obj instanceof DelayActivationMode thiz) {
            var text =
                    Component.translatable("text.autoconfig.dontdropit.general.delayed_drop."
                            + thiz.name().toLowerCase(Locale.ROOT));
            if (thiz == DISABLED) {
                text = text.withStyle(style -> style.withColor(ChatFormatting.RED));
            }
            return text;
        } else {
            return Component.empty();
        }
    }

    private static final DelayActivationMode[] VALUES = values();

    public static <T extends ConfigData> void registerConfigGuiProvider(Class<T> configClass) {
        final ConfigEntryBuilder entryBuilder = ConfigEntryBuilder.create();
        AutoConfigClient.getGuiRegistry(configClass).registerTypeProvider((i13n, field, config, defaults, registry) ->
                Collections.singletonList(
                        entryBuilder.startSelector(
                                Component.translatable(i13n),
                                        VALUES,
                                        getUnsafely(field, config, getUnsafely(field, defaults)))
                                .setDefaultValue(() -> getUnsafely(field, defaults))
                                .setSaveConsumer(newValue -> setUnsafely(field, config, newValue))
                                .setNameProvider(DelayActivationMode::toText)
                                .build()
                ), DelayActivationMode.class);
    }
}
