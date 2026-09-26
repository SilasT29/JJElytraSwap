//? if neoforge {
package io.github.jumperonjava.jjelytraswap.platforms.neoforge;

import io.github.jumperonjava.jjelytraswap.JJElytraSwapInit;
import io.github.jumperonjava.jjelytraswap.ModPlatform;
//? if >= 26.3 {
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
//? } else {
/*import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Identifier;
*///?}
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
//? if <1.21 {
/*import net.neoforged.neoforge.client.ConfigScreenHandler;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TickEvent;

import java.util.function.Consumer;

*///?} else {
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

//?}
@Mod("jjelytraswap")
public class JJElytraSwapNeoForge {

    private static IEventBus MOD_EVENT_BUS;
    private static IEventBus GAME_EVENT_BUS;

    public JJElytraSwapNeoForge(IEventBus eventBus) {
        MOD_EVENT_BUS = eventBus;
        GAME_EVENT_BUS = NeoForge.EVENT_BUS;
        JJElytraSwapInit.entrypoint(new NeoForgePlatform());
    }

    public static class NeoForgePlatform implements ModPlatform {

        @Override
        public String getModloader() {
            return "NeoForge";
        }

        @Override
        public boolean isModLoaded(String modId) {
            return ModList.get().isLoaded(modId);
        }


        List<Runnable> clientEvents = new ArrayList<>();

        //? if >= 26.3 {
        private KeyMapping toggleBind;
        //? } else {
        /*private KeyBinding toggleBind;
         *///?}

        @Override
        public void registerClientTickEvent(Runnable o) {
            GAME_EVENT_BUS.addListener((Consumer<ClientTickEvent.Post>)event -> o.run());
        }

        @Override
        public void registerToggleKeybind(String translationKeyName, int defaultKeyId) {
            //? if >= 26.3 {
            // 26.3 uses SDL scancodes: negative "unbound" sentinel (-1) would crash
            // KeyMapping.setAll() in InputConstants.isKeyDown — use scancode 0 (= UNKNOWN).
            KeyMapping.Category kbCategory = new KeyMapping.Category(Identifier.fromNamespaceAndPath("jjelytraswap","generic"));
            toggleBind = new KeyMapping(translationKeyName, Math.max(defaultKeyId, 0), kbCategory);
            //? } else if >= 1.21.9 {
            /*KeyBinding.Category kbCategory = new KeyBinding.Category(Identifier.of("jjelytraswap","generic"));
            toggleBind = new KeyBinding(translationKeyName,defaultKeyId,kbCategory);
            *///? } else {
            /*toggleBind = new KeyBinding(translationKeyName, defaultKeyId, "JJElytraSwap");
             *///?}
            MOD_EVENT_BUS.addListener((Consumer<RegisterKeyMappingsEvent>) event -> event.register(toggleBind));
        }

        @Override
        public boolean consumeToggleKeybind() {
            //? if >= 26.3 {
            return toggleBind != null && toggleBind.consumeClick();
            //? } else {
            /*return toggleBind != null && toggleBind.wasPressed();
             *///?}
        }
    }
}
//?}