package adudecalledleo.dontdropit;

import java.util.Collections;
import java.util.Set;

import adudecalledleo.dontdropit.api.DontDropItApi;
import adudecalledleo.dontdropit.config.DelayActivationMode;
import adudecalledleo.dontdropit.config.DropBehaviorOverride;
import adudecalledleo.dontdropit.config.FavoredChecker;
import adudecalledleo.dontdropit.config.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DontDropIt implements ClientModInitializer, DontDropItApi {
    public static final String MOD_ID = "dontdropit";
    public static final String MOD_NAME = "Don't Drop It!";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitializeClient() {
        DropBehaviorOverride.registerConfigGuiProvider(ModConfig.class);
        DelayActivationMode.registerConfigGuiProvider(ModConfig.class);
        AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new)
                .registerSaveListener((manager, data) -> {
                    data.postUpdate();
                    return InteractionResult.PASS;
                });
        ModKeyMappings.register();
        IgnoredSlots.collectFromEntrypoints();
        ClientTickEvents.END_CLIENT_TICK.register(DropDelayHandler::tick);
        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((client, world) -> {
            if (world != null) {
                FavoredChecker.updateFavoredSets(ModConfig.get());
            }
        });
        LOGGER.info("Don't drop that Diamond Pickaxe! (Don't Drop It! successfully initialized)");
    }

    @Override
    public Set<Class<? extends Slot>> getIgnoredDropDelaySlots() {
        return Collections.singleton(CreativeModeInventoryScreen.CustomCreativeSlot.class);
    }
}
