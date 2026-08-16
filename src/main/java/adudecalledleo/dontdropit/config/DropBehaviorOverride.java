package adudecalledleo.dontdropit.config;

import java.util.Collections;
import java.util.Locale;

import me.shedaniel.autoconfig.AutoConfigClient;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static me.shedaniel.autoconfig.util.Utils.getUnsafely;
import static me.shedaniel.autoconfig.util.Utils.setUnsafely;

public enum DropBehaviorOverride {
    FAVORITE_ITEMS, ALL_ITEMS, DISABLED;

    public static Text toText(Object obj) {
        if (obj instanceof DropBehaviorOverride thiz) {
            var text =
                    Text.translatable("text.autoconfig.dontdropit.general.drop_behavior."
                            + thiz.name().toLowerCase(Locale.ROOT));
            if (thiz == DISABLED) {
                text = text.styled(style -> style.withColor(Formatting.RED));
            }
            return text;
        } else {
            return Text.empty();
        }
    }

    private static final DropBehaviorOverride[] VALUES = values();

    public static <T extends ConfigData> void registerConfigGuiProvider(Class<T> configClass) {
        final ConfigEntryBuilder entryBuilder = ConfigEntryBuilder.create();
        AutoConfigClient.getGuiRegistry(configClass).registerTypeProvider((i13n, field, config, defaults, registry) ->
                Collections.singletonList(
                    entryBuilder.startSelector(
                            Text.translatable(i13n),
                                    VALUES,
                                    getUnsafely(field, config, getUnsafely(field, defaults)))
                            .setDefaultValue(() -> getUnsafely(field, defaults))
                            .setSaveConsumer(newValue -> setUnsafely(field, config, newValue))
                            .setNameProvider(DropBehaviorOverride::toText)
                            .build()
                ), DropBehaviorOverride.class);
    }
}
