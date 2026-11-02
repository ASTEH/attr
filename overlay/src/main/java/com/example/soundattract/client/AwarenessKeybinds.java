package com.example.soundattract.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.api.distmarker.Dist;

@EventBusSubscriber(modid = "soundattract", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class AwarenessKeybinds {
    public static final KeyMapping TOGGLE_HUD = new KeyMapping(
            "key.soundattract.toggle_awareness_hud",
            InputConstants.UNKNOWN.getValue(),
            "key.categories.soundattract");

    public static final KeyMapping CONFIGURE_HUD = new KeyMapping(
            "key.soundattract.configure_awareness_hud",
            InputConstants.UNKNOWN.getValue(),
            "key.categories.soundattract");

    private AwarenessKeybinds() {
    }

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_HUD);
        event.register(CONFIGURE_HUD);
    }
}
