package techreborn.world;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.world.biome.Biome;

import java.util.function.Predicate;

public enum WorldTargetType implements StringIdentifiable {
	DEFAULT("default", category -> category != Biome.Category.NETHER && category != Biome.Category.THEEND),
	NETHER("nether", category -> category == Biome.Category.NETHER),
	END("end", category -> category == Biome.Category.THEEND);

	private final String name;
	private final Predicate<Biome.Category> biomeCategoryPredicate;
	public static final Codec<WorldTargetType> CODEC = StringIdentifiable.createCodec(WorldTargetType::values, WorldTargetType::getByName);

	WorldTargetType(String name, Predicate<Biome.Category> biomeCategoryPredicate) {
		this.name = name;
		this.biomeCategoryPredicate = biomeCategoryPredicate;
	}

	public String getName() {
		return name;
	}

	public boolean isApplicable(Biome.Category biomeCategory) {
		return biomeCategoryPredicate.test(biomeCategory);
	}

	public static WorldTargetType getByName(String name) {
		return null;
	}

	@Override
	public String asString() {
		return name;
	}
}
