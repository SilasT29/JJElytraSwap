//? if fabric {
package io.github.jumperonjava.jjelytraswap.platforms.fabric;

import io.github.jumperonjava.jjelytraswap.ModPlatform;
import io.github.jumperonjava.jjelytraswap.JJElytraSwapInit;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

//? if >= 26.3 {
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
//? } else {
/*import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Identifier;
*///?}

public class JJElytraSwapFabric implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		JJElytraSwapInit.entrypoint(new FabricPlatform());
	}
	public static class FabricPlatform implements ModPlatform{

		@Override
		public String getModloader() {
			return "Fabric";
		}

		@Override
		public boolean isModLoaded(String modloader) {
			return FabricLoader.getInstance().isModLoaded(modloader);
		}

		@Override
		public void registerClientTickEvent(Runnable o) {
			ClientTickEvents.END_CLIENT_TICK.register(client -> o.run());
		}

		//? if >= 26.3 {
		private KeyMapping toggleBind;
		//? } else {
		/*private KeyBinding toggleBind;
		 *///?}

		@Override
		public void registerToggleKeybind(String translationKeyName, int defaultKeyId) {
			//? if >= 26.3 {
			KeyMapping.Category kbCategory = new KeyMapping.Category(Identifier.fromNamespaceAndPath("jjelytraswap","generic"));
			toggleBind = new KeyMapping(translationKeyName,defaultKeyId,kbCategory);
			KeyMappingHelper.registerKeyMapping(toggleBind);
			//? } else if >= 1.21.9 {
			/*KeyBinding.Category kbCategory = new KeyBinding.Category(Identifier.of("jjelytraswap","generic"));
			toggleBind = new KeyBinding(translationKeyName,defaultKeyId,kbCategory);
			KeyBindingHelper.registerKeyBinding(toggleBind);
			*///? } else {
			/*toggleBind = new KeyBinding(translationKeyName,defaultKeyId,"JJElytraSwap");
			KeyBindingHelper.registerKeyBinding(toggleBind);
			 *///?}
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