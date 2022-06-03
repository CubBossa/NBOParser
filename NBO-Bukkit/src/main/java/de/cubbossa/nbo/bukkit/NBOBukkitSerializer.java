package de.cubbossa.nbo.bukkit;

import nbo.NBOSerializer;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.banner.Pattern;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
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
}
