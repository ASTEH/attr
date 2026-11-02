package com.example.soundattract.client;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = "soundattract", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class AwarenessHudEditorEvents {
    private AwarenessHudEditorEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.screen != null) {
            return;
        }

        if (AwarenessKeybinds.CONFIGURE_HUD.consumeClick()) {
            minecraft.setScreen(new AwarenessHudConfigScreen(null));
        }
    }
}
