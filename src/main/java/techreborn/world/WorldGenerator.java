/*
 * This file is part of TechReborn, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2020 TechReborn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package techreborn.world;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.block.Blocks;
import net.minecraft.structure.rule.BlockStateMatchRuleTest;
import net.minecraft.structure.rule.RuleTest;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.Category;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.UniformIntDistribution;
import net.minecraft.world.gen.decorator.ChanceDecoratorConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.feature.size.TwoLayersFeatureSize;
import net.minecraft.world.gen.stateprovider.SimpleBlockStateProvider;
import net.minecraft.world.gen.stateprovider.WeightedBlockStateProvider;
import net.minecraft.world.gen.trunk.StraightTrunkPlacer;
import techreborn.blocks.misc.BlockRubberLog;
import techreborn.config.TechRebornConfig;
import techreborn.init.TRContent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author drcrazy
 */
public class WorldGenerator {

	public static Feature<TreeFeatureConfig> RUBBER_TREE_FEATURE;
	public static RubberTreeDecorator RUBBER_TREE_DECORATOR;
	public static TreeFeatureConfig RUBBER_TREE_CONFIG;
	private static final RuleTest END_STONE = new BlockStateMatchRuleTest(Blocks.END_STONE.getDefaultState());

	private static final List<Biome> checkedBiomes = new ArrayList<>();

	public static void initBiomeFeatures() {
		setupTrees();

		for (Biome biome : BuiltinRegistries.BIOME) {
			addToBiome(biome);
		}

		//Handles modded biomes
		RegistryEntryAddedCallback.event(BuiltinRegistries.BIOME).register((i, identifier, biome) -> addToBiome(biome));
	}

	private static void setupTrees() {
		RUBBER_TREE_FEATURE = Registry.register(Registry.FEATURE, new Identifier("techreborn:rubber_tree"), new RubberTreeFeature(TreeFeatureConfig.CODEC));
		RUBBER_TREE_DECORATOR = Registry.register(Registry.DECORATOR, new Identifier("techreborn:rubber_tree"), new RubberTreeDecorator(ChanceDecoratorConfig.CODEC));

		WeightedBlockStateProvider logProvider = new WeightedBlockStateProvider();
		logProvider.addState(TRContent.RUBBER_LOG.getDefaultState(), 10);

		Arrays.stream(Direction.values())
				.filter(direction -> direction.getAxis().isHorizontal())
				.map(direction -> TRContent.RUBBER_LOG.getDefaultState()
						.with(BlockRubberLog.HAS_SAP, true)
						.with(BlockRubberLog.SAP_SIDE, direction)
				)
				.forEach(state -> logProvider.addState(state, 1));

		RUBBER_TREE_CONFIG = new TreeFeatureConfig.Builder(
				logProvider,
				new SimpleBlockStateProvider(TRContent.RUBBER_LEAVES.getDefaultState()),
				new RubberTreeFeature.FoliagePlacer(UniformIntDistribution.of(2, 0), UniformIntDistribution.of(0, 0), 3),
				new StraightTrunkPlacer(TechRebornConfig.rubberTreeBaseHeight, 3, 0),
				new TwoLayersFeatureSize(1, 0, 1)
		).build();
	}

	private static void addToBiome(Biome biome) {
		if (checkedBiomes.contains(biome)) {
			//Just to be sure we dont add the stuff twice to the same biome
			return;
		}
		checkedBiomes.add(biome);

		if (biome.getCategory() == Category.NETHER) {
			if (TechRebornConfig.enableCinnabarOre) {
				addOre(biome, OreFeatureConfig.Rules.BASE_STONE_NETHER, TRContent.Ores.CINNABAR);
			}
			if (TechRebornConfig.enablePyriteOre) {
				addOre(biome, OreFeatureConfig.Rules.BASE_STONE_NETHER, TRContent.Ores.PYRITE);
			}
			if (TechRebornConfig.enableSphaleriteOre) {
				addOre(biome, OreFeatureConfig.Rules.BASE_STONE_NETHER, TRContent.Ores.SPHALERITE);
			}
		} else if (biome.getCategory() == Category.THEEND) {
			if (TechRebornConfig.enablePeridotOre) {
				addOre(biome, END_STONE, TRContent.Ores.PERIDOT);
			}
			if (TechRebornConfig.enableSheldoniteOre) {
				addOre(biome, END_STONE, TRContent.Ores.SHELDONITE);
			}
			if (TechRebornConfig.enableSodaliteOre) {
				addOre(biome, END_STONE, TRContent.Ores.SODALITE);
			}
			if (TechRebornConfig.enableTungstenOre) {
				addOre(biome, END_STONE, TRContent.Ores.TUNGSTEN);
			}
		} else {
			if (TechRebornConfig.enableBauxiteOre) {
				addOre(biome, OreFeatureConfig.Rules.BASE_STONE_OVERWORLD, TRContent.Ores.BAUXITE);
			}
			if (TechRebornConfig.enableCopperOre) {
				addOre(biome, OreFeatureConfig.Rules.BASE_STONE_OVERWORLD, TRContent.Ores.COPPER);
			}
			if (TechRebornConfig.enableGalenaOre) {
				addOre(biome, OreFeatureConfig.Rules.BASE_STONE_OVERWORLD, TRContent.Ores.GALENA);
			}
			if (TechRebornConfig.enableIridiumOre) {
				addOre(biome, OreFeatureConfig.Rules.BASE_STONE_OVERWORLD, TRContent.Ores.IRIDIUM);
			}
			if (TechRebornConfig.enableLeadOre) {
				addOre(biome, OreFeatureConfig.Rules.BASE_STONE_OVERWORLD, TRContent.Ores.LEAD);
			}
			if (TechRebornConfig.enableRubyOre) {
				addOre(biome, OreFeatureConfig.Rules.BASE_STONE_OVERWORLD, TRContent.Ores.RUBY);
			}
			if (TechRebornConfig.enableSapphireOre) {
				addOre(biome, OreFeatureConfig.Rules.BASE_STONE_OVERWORLD, TRContent.Ores.SAPPHIRE);
			}
			if (TechRebornConfig.enableSilverOre) {
				addOre(biome, OreFeatureConfig.Rules.BASE_STONE_OVERWORLD, TRContent.Ores.SILVER);
			}
			if (TechRebornConfig.enableTinOre) {
				addOre(biome, OreFeatureConfig.Rules.BASE_STONE_OVERWORLD, TRContent.Ores.TIN);
			}

			if (biome.getCategory() == Category.FOREST || biome.getCategory() == Category.TAIGA || biome.getCategory() == Category.SWAMP) {
				addFeature(biome, GenerationStep.Feature.VEGETAL_DECORATION,
						RUBBER_TREE_FEATURE.configure(RUBBER_TREE_CONFIG)
								.decorate(RUBBER_TREE_DECORATOR
										.configure(new ChanceDecoratorConfig(biome.getCategory() == Category.SWAMP ? TechRebornConfig.rubberTreeChance / 3 : TechRebornConfig.rubberTreeChance))
								)
				);
			}
		}
	}

	private static void addOre(Biome biome, RuleTest ruleTest, TRContent.Ores ore) {
		addFeature(biome,
				GenerationStep.Feature.UNDERGROUND_ORES,
				Feature.ORE.configure(
					new OreFeatureConfig(ruleTest, ore.block.getDefaultState(), ore.veinSize)
				).method_30377(ore.maxY).spreadHorizontally().repeat(ore.veinsPerChunk)
		);
	}

	private static void addFeature(Biome biome, GenerationStep.Feature feature, ConfiguredFeature<?, ?> configuredFeature) {
		List<List<Supplier<ConfiguredFeature<?, ?>>>> features = biome.getGenerationSettings().getFeatures();

		int stepIndex = feature.ordinal();

		while(features.size() <= stepIndex) {
			features.add(Lists.newArrayList());
		}

		List<Supplier<ConfiguredFeature<?, ?>>> stepList = features.get(feature.ordinal());
		if (stepList instanceof ImmutableList) {
			features.set(feature.ordinal(), stepList = new ArrayList<>(stepList));
		}

		stepList.add(() -> configuredFeature);
	}
}