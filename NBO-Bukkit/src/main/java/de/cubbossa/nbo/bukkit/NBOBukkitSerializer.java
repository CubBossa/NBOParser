package de.cubbossa.nbo.bukkit;

import nbo.NBOSerializer;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Biome;
import org.bukkit.block.banner.Pattern;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTables;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.BlockVector;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.LinkedHashMap;
import java.util.Map;

public class NBOBukkitSerializer {

	public static void addBukkitSerialization(NBOSerializer serializer) {
		serializer
				.register(Vector.class, Vector::deserialize, Vector::serialize)
				.register(BlockVector.class, BlockVector::deserialize, BlockVector::serialize)
				.register(ItemStack.class, ItemStack::deserialize, ItemStack::serialize)
				.register(Color.class, Color::deserialize, Color::serialize)
				.register(PotionEffect.class, map -> (PotionEffect) ConfigurationSerialization.deserializeObject(map), PotionEffect::serialize)
				.register(FireworkEffect.class, map -> (FireworkEffect) FireworkEffect.deserialize(map), FireworkEffect::serialize)
				.register(Pattern.class, map -> (Pattern) ConfigurationSerialization.deserializeObject(map), Pattern::serialize)
				.register(Location.class, Location::deserialize, Location::serialize)
				.register(AttributeModifier.class, AttributeModifier::deserialize, AttributeModifier::serialize)
				.register(BoundingBox.class, BoundingBox::deserialize, BoundingBox::serialize)

				.register(Advancement.class, map -> loadKeyed(Registry.ADVANCEMENT, map), x -> Map.of("id", x.getKey().toString()))
				.register(Art.class, map -> loadKeyed(Registry.ART, map), x -> Map.of("id", x.getKey().toString()))
				.register(Attribute.class, map -> loadKeyed(Registry.ATTRIBUTE, map), x -> Map.of("id", x.getKey().toString()))
				.register(KeyedBossBar.class, map -> loadKeyed(Registry.BOSS_BARS, map), x -> Map.of("id", x.getKey().toString()))
				.register(Biome.class, map -> loadKeyed(Registry.BIOME, map), x -> Map.of("id", x.getKey().toString()))
				.register(Enchantment.class, map -> loadKeyed(Registry.ENCHANTMENT, map), x -> Map.of("id", x.getKey().toString()))
				.register(EntityType.class, map -> loadKeyed(Registry.ENTITY_TYPE, map), x -> Map.of("id", x.getKey().toString()))
				.register(LootTables.class, map -> loadKeyed(Registry.LOOT_TABLES, map), x -> Map.of("id", x.getKey().toString()))
				.register(Material.class, map -> loadKeyed(Registry.MATERIAL, map), x -> Map.of("id", x.getKey().toString()))
				.register(Statistic.class, map -> loadKeyed(Registry.STATISTIC, map), x -> Map.of("id", x.getKey().toString()))
				.register(Villager.Profession.class, map -> loadKeyed(Registry.VILLAGER_PROFESSION, map), x -> Map.of("id", x.getKey().toString()))
				.register(Villager.Type.class, map -> loadKeyed(Registry.VILLAGER_TYPE, map), x -> Map.of("id", x.getKey().toString()))
				.register(MemoryKey.class, map -> loadKeyed(Registry.MEMORY_MODULE_TYPE, map), x -> Map.of("id", x.getKey().toString()))
				.register(Fluid.class, map -> loadKeyed(Registry.FLUID, map), x -> Map.of("id", x.getKey().toString()))
				.register(GameEvent.class, map -> loadKeyed(Registry.GAME_EVENT, map), x -> Map.of("id", x.getKey().toString()))

				.register(Particle.DustTransition.class, map -> {
					return new Particle.DustTransition(
							(Color) map.getOrDefault("color", Color.BLACK),
							(Color) map.getOrDefault("toColor", Color.BLACK),
							(float) map.getOrDefault("size", 1f)
					);
				}, dustTransition -> {
					Map<String, Object> map = new LinkedHashMap<>();
					map.put("color", dustTransition.getColor());
					map.put("size", dustTransition.getSize());
					map.put("toColor", dustTransition.getToColor());
					return map;
				})


				.register(Particle.DustOptions.class, map -> {
					return new Particle.DustOptions(
							(Color) map.getOrDefault("color", Color.BLACK),
							(float) map.getOrDefault("size", 1f)
					);
				}, dustTransition -> {
					Map<String, Object> map = new LinkedHashMap<>();
					map.put("color", dustTransition.getColor());
					map.put("size", dustTransition.getSize());
					return map;
				});
	}

	private static <T extends Keyed> T loadKeyed(Registry<T> registry, Map<String, Object> map) {
		if (!map.containsKey("id") || !(map.get("id") instanceof String)) {
			throw new RuntimeException("Sound objects require key 'id'.");
		}
		NamespacedKey key = NamespacedKey.fromString((String) map.get("id"));
		if (key == null) {
			throw new RuntimeException("Could not find sound with id '" + map.get("id") + "'.");
		}
		return registry.get(key);
	}
}
