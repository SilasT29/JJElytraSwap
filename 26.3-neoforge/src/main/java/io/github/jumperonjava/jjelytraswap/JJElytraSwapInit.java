package io.github.jumperonjava.jjelytraswap;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.inventory.ContainerInput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

public class JJElytraSwapInit
{
	public static final String MODID = "jjelytraswap";
	public static final Logger LOGGER = LoggerFactory.getLogger("JJElytraSwap");
	public static ModPlatform PLATFORM = null;

	public static void entrypoint(ModPlatform platform) {
		JJElytraSwapInit.PLATFORM = platform;
		onInitializeClient();
	}

	public static boolean enabled = true;

	/** Component ids shared across mapping generations. */
	public enum CompType { GLIDER, EQUIPPABLE, ATTRIBUTE_MODIFIERS, CUSTOM_NAME }

	public static boolean stackHasComponent(ItemStack stack, CompType type) {
		return stack.has(switch (type) {
			case GLIDER -> DataComponents.GLIDER;
			case EQUIPPABLE -> DataComponents.EQUIPPABLE;
			case ATTRIBUTE_MODIFIERS -> DataComponents.ATTRIBUTE_MODIFIERS;
			case CUSTOM_NAME -> DataComponents.CUSTOM_NAME;
		});
	}

	private static Minecraft getClient() {
		return Minecraft.getInstance();
	}

	private static boolean playerReady() {
		return getClient().level != null && getClient().player != null;
	}

	private static ItemStack invStack(int slot) {
		return getClient().player.getInventory().getItem(slot);
	}

	private static ItemStack chestStack() {
		return getClient().player.getItemBySlot(EquipmentSlot.CHEST);
	}

	private static boolean playerInAir() {
		var p = getClient().player;
		return !p.onGround() || p.isInLiquid();
	}

	public static void tryWearChestplate() {
		if (!playerReady()) {
			return;
		}

		if (chestStack().isEmpty()) {
			return;
		}

		var chestplateSlots = getChestplateSlots();

		chestplateSlots = chestplateSlots
				.stream()
				.filter(slot->(getChestplateStat(invStack(slot))>0f))
				.sorted(
						Comparator.comparingInt(
								slot -> getChestplateStat(
										invStack(slot)
								)
						)
				).collect(Collectors.toCollection(ArrayList::new));
		Collections.reverse(chestplateSlots);


//		if(stackHasComponent(chestStack(),CompType.GLIDER))
//			return;

		if (!chestplateSlots.isEmpty()) {
			int bestSlot = chestplateSlots.get(0);
			swap(bestSlot);
		}
	}


	public static void tryWearElytra() {
		if (!playerReady()) {
			return;
		}

		if (stackHasComponent(invStack(38),CompType.GLIDER)) {
			return;
		}

		var elytraSlots = getElytraSlots();

		elytraSlots.sort(Comparator.comparingInt(slot -> getElytraStat(invStack(slot))));

		if (!elytraSlots.isEmpty()) {
			int bestSlot = elytraSlots.get(elytraSlots.size() - 1);
			wearElytra(bestSlot);
		}
	}

	public static List<Integer> getElytraSlots() {
		List<Integer> elytraSlots = new ArrayList<>();

		for (int slot : slotArray()) {
			if (stackHasComponent(invStack(slot),CompType.GLIDER)) {
				elytraSlots.add(slot);
			}
		}
		return elytraSlots;
	}


	public static List<Integer> getChestplateSlots() {
		List<Integer> chestplateSlots = new ArrayList<>();

		for (int slot : slotArray()) {
			if (isSlotChestplate(slot)) {
				chestplateSlots.add(slot);
			}
		}

		return chestplateSlots;
	}

	private static Registry<Enchantment> getEnchantmentRegistry() {
		return getClient().level.registryAccess()
		.lookupOrThrow(Registries.ENCHANTMENT);
	}

	private static int getLevel(ResourceKey<Enchantment> key, ItemStack stack) {
		Holder<Enchantment> enchantEntry = getEnchantmentRegistry()
				.getOrThrow(key);
		return EnchantmentHelper.getItemEnchantmentLevel(enchantEntry, stack);
	}

	private static int getElytraStat(ItemStack elytraItem) {
		var stat = (getLevel(Enchantments.MENDING,elytraItem)*3+1)+getLevel(Enchantments.UNBREAKING,elytraItem);

		return stat;
	}

	private static int getChestplateStat(ItemStack chestplateItem) {
		float score = 1;

		if(stackHasComponent(chestplateItem,CompType.EQUIPPABLE)){
			if(chestplateItem.get(DataComponentsOrTypes.EQUIPPABLE).slot()==EquipmentSlot.CHEST){
				var component = chestplateItem.get(DataComponentsOrTypes.ATTRIBUTE_MODIFIERS);
				for (ItemAttributeModifiers.Entry entry : component.modifiers()) {
					Holder<Attribute> attribute = entry.attribute();
					if(attribute == Attributes.ARMOR) {
						score += entry.modifier().amount();
					}
					if(attribute == Attributes.ARMOR_TOUGHNESS) {
						score += entry.modifier().amount();
					}
				}
				score += getLevel(Enchantments.PROTECTION,chestplateItem)*2;
				score += getLevel(Enchantments.MENDING,chestplateItem)*0.5;
				score += stackHasComponent(chestplateItem,CompType.CUSTOM_NAME)?0.25:0;
				score += getLevel(Enchantments.UNBREAKING,chestplateItem)*0.24/3;
			}
		}

		return (int) (score*1000);
	}

	private static void wearElytra(int slotId) {
		swap(slotId);
		try {
			getClient().getConnection().send(new ServerboundPlayerCommandPacket(getClient().player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));

			getClient().player.tryToStartFallFlying();
		} catch (NullPointerException ex) {
			ex.printStackTrace();
		}
	}


	private static void swap(int slot) {
		int slot2 = slot;
		if (slot2 == 40) slot2 = 45;
		if (slot2 < 9) slot2 += 36;

		try {
			var player = getClient().player;
			getClient().gameMode.handleContainerInput(0, slot2, 0, ContainerInput.PICKUP, player);
			getClient().gameMode.handleContainerInput(0, 6, 0, ContainerInput.PICKUP, player);
			getClient().gameMode.handleContainerInput(0, slot2, 0, ContainerInput.PICKUP, player);
		} catch (NullPointerException ex) {
			ex.printStackTrace();
		}
	}
	public static boolean isSlotChestplate(int slotId) {

		if (!playerReady()) {
			return false;
		}
		ItemStack chestSlot = invStack(slotId);

		return !chestSlot.isEmpty() &&
				stackHasComponent(chestSlot,CompType.EQUIPPABLE) &&
				chestSlot.get(DataComponentsOrTypes.EQUIPPABLE).slot() == EquipmentSlot.CHEST &&
				getLevel(Enchantments.BINDING_CURSE,chestSlot) == 0;
	}

	private static int[] slotArray() {
		int[] range = new int[37];
		for (int i = 0; i < 9; i++) range[i] = 8 - i;
		for (int i = 9; i < 36; i++) range[i] = 35 - (i - 9);
		range[36] = 40;
		return range;
	}

	public static boolean shouldWearChestplatePrevTick =true;
	public static void onInitializeClient() {
		PLATFORM.registerToggleKeybind("jjelytraswap.keybind",-1);
		PLATFORM.registerClientTickEvent(()->{
			if (!playerReady()) {
				return;
			}

			if(PLATFORM.consumeToggleKeybind())
			{
				enabled=!enabled;
				sendToggleMessage(enabled);
			}
			if(!enabled)
				return;
			boolean isInAir = playerInAir();
			boolean shouldWearChestplate = !isInAir;
			if(shouldWearChestplate && !shouldWearChestplatePrevTick){
				if(stackHasComponent(chestStack(),CompType.GLIDER)){
					tryWearChestplate();
				}
			}
			shouldWearChestplatePrevTick = shouldWearChestplate;
		});
	}

	private static void sendToggleMessage(boolean on) {
		var player = getClient().player;
		if (player != null) {
			player.sendSystemMessage(Component.translatable("jjelytraswap." + (on ? "enabled" : "disabled")));
		}
	}

	/** Static access to the component types, resolved per mapping generation. */
	static final class DataComponentsOrTypes {
		static final net.minecraft.core.component.DataComponentType<net.minecraft.util.Unit> GLIDER = DataComponents.GLIDER;
		static final net.minecraft.core.component.DataComponentType<net.minecraft.world.item.equipment.Equippable> EQUIPPABLE = DataComponents.EQUIPPABLE;
		static final net.minecraft.core.component.DataComponentType<net.minecraft.world.item.component.ItemAttributeModifiers> ATTRIBUTE_MODIFIERS = DataComponents.ATTRIBUTE_MODIFIERS;
		static final net.minecraft.core.component.DataComponentType<net.minecraft.network.chat.Component> CUSTOM_NAME = DataComponents.CUSTOM_NAME;
	}
}