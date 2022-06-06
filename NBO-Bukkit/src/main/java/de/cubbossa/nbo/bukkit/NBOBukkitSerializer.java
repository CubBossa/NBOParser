package de.cubbossa.nbo.bukkit;

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
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockVector;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class NBOBukkitSerializer {

	public static void addBukkitSerialization(NBOSerializer serializer) {
		serializer
				.registerMapSerializer(Vector.class, Vector::deserialize, Vector::serialize)
				.registerMapSerializer(BlockVector.class, BlockVector::deserialize, BlockVector::serialize)
				.registerMapSerializer(ItemStack.class, ItemStack::deserialize, ItemStack::serialize)
				.registerMapSerializer(Color.class, Color::deserialize, Color::serialize)
				.registerMapSerializer(FireworkEffect.class, map -> (FireworkEffect) FireworkEffect.deserialize(map), FireworkEffect::serialize)
				.registerMapSerializer(Pattern.class, map -> (Pattern) ConfigurationSerialization.deserializeObject(map), Pattern::serialize)
				.registerMapSerializer(Location.class, Location::deserialize, Location::serialize)
				.registerMapSerializer(AttributeModifier.class, AttributeModifier::deserialize, AttributeModifier::serialize)
				.registerMapSerializer(BoundingBox.class, BoundingBox::deserialize, BoundingBox::serialize)

				.registerMapSerializer(Advancement.class, map -> loadKeyed(Registry.ADVANCEMENT, map), x -> Map.of("id", x.getKey().toString()))
				.registerMapSerializer(Art.class, map -> loadKeyed(Registry.ART, map), x -> Map.of("id", x.getKey().toString()))
				.registerMapSerializer(Attribute.class, map -> loadKeyed(Registry.ATTRIBUTE, map), x -> Map.of("id", x.getKey().toString()))
				.registerMapSerializer(KeyedBossBar.class, map -> loadKeyed(Registry.BOSS_BARS, map), x -> Map.of("id", x.getKey().toString()))
				.registerMapSerializer(Biome.class, map -> loadKeyed(Registry.BIOME, map), x -> Map.of("id", x.getKey().toString()))
				.registerMapSerializer(Enchantment.class, map -> loadKeyed(Registry.ENCHANTMENT, map), x -> Map.of("id", x.getKey().toString()))
				.registerMapSerializer(EntityType.class, map -> loadKeyed(Registry.ENTITY_TYPE, map), x -> Map.of("id", x.getKey().toString()))
				.registerMapSerializer(LootTables.class, map -> loadKeyed(Registry.LOOT_TABLES, map), x -> Map.of("id", x.getKey().toString()))
				.registerMapSerializer(Material.class, map -> loadKeyed(Registry.MATERIAL, map), x -> Map.of("id", x.getKey().toString()))
				.registerMapSerializer(Statistic.class, map -> loadKeyed(Registry.STATISTIC, map), x -> Map.of("id", x.getKey().toString()))
				.registerMapSerializer(Villager.Profession.class, map -> loadKeyed(Registry.VILLAGER_PROFESSION, map), x -> Map.of("id", x.getKey().toString()))
				.registerMapSerializer(Villager.Type.class, map -> loadKeyed(Registry.VILLAGER_TYPE, map), x -> Map.of("id", x.getKey().toString()))
				.registerMapSerializer(MemoryKey.class, map -> loadKeyed(Registry.MEMORY_MODULE_TYPE, map), x -> Map.of("id", x.getKey().toString()))
				.registerMapSerializer(Fluid.class, map -> loadKeyed(Registry.FLUID, map), x -> Map.of("id", x.getKey().toString()))
				.registerMapSerializer(GameEvent.class, map -> loadKeyed(Registry.GAME_EVENT, map), x -> Map.of("id", x.getKey().toString()))


				.registerMapSerializer(PotionEffect.class, map -> {
					return new PotionEffect(PotionEffectType.getByKey(NamespacedKey.fromString((String) map.get("effect"))),
							getInt(map, "duration"),
							getInt(map, "amplifier"),
							getBool(map, "ambient", false),
							getBool(map, "has-particles", true),
							getBool(map, "has-icon", true));
				}, effect -> {
					LinkedHashMap<String, Object> map = new LinkedHashMap<>();
					map.put("effect", effect.getType().getKey().toString());
					map.put("duration", effect.getDuration());
					map.put("amplifier", effect.getAmplifier());
					map.put("ambient", effect.isAmbient());
					map.put("has-particles", effect.hasParticles());
					map.put("has-icon", effect.hasIcon());
					return map;
				})


				.registerMapSerializer(Particle.DustTransition.class, map -> {
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


				.registerMapSerializer(Particle.DustOptions.class, map -> {
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

	private static int getInt(Map<?, ?> map, Object key) {
		Object num = map.get(key);
		if (num instanceof Integer) {
			return (Integer) num;
		} else {
			throw new NoSuchElementException(map + " does not contain " + key);
		}
	}

	private static boolean getBool(Map<?, ?> map, Object key, boolean def) {
		Object bool = map.get(key);
		return bool instanceof Boolean ? (Boolean) bool : def;
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
