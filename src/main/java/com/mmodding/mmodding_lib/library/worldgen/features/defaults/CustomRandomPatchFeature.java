package com.mmodding.mmodding_lib.library.worldgen.features.defaults;

import net.minecraft.util.Holder;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.decorator.*;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.util.PlacedFeatureUtil;
import org.quiltmc.qsl.worldgen.biome.api.BiomeModifications;
import org.quiltmc.qsl.worldgen.biome.api.BiomeSelectionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

public class CustomRandomPatchFeature implements CustomFeature, FeatureRegistrable {

	private final AtomicBoolean registered = new AtomicBoolean();
	private final AtomicReference<Identifier> identifier = new AtomicReference<>();
	private final List<Pair<PlacedFeature, String>> additionalPlacedFeatures = new ArrayList<>();

	private final AtomicInteger count = new AtomicInteger();
	private final AtomicInteger rarity = new AtomicInteger();
	private final int tries;
	private final int spreadXZ;
	private final int spreadY;
	private final Holder<PlacedFeature> feature;

	public CustomRandomPatchFeature(int tries, int spreadHorizontally, int spreadVertically, Holder<PlacedFeature> feature) {
		this.tries = tries;
		this.spreadXZ = spreadHorizontally;
		this.spreadY = spreadVertically;
		this.feature = feature;
	}

	@Override
	public Feature<?> getFeature() {
		return Feature.RANDOM_PATCH;
	}

	@Override
	public ConfiguredFeature<?, ?> getConfiguredFeature() {
		return new ConfiguredFeature<>(Feature.RANDOM_PATCH, new RandomPatchFeatureConfig(this.tries, this.spreadXZ, this.spreadY, this.feature));
	}

	public CustomRandomPatchFeature setCount(int count) {
		this.count.set(count);
		return this;
	}

	public CustomRandomPatchFeature setRarity(int rarity) {
		this.rarity.set(rarity);
		return this;
	}

	public PlacedFeature createPlacedFeature(int count, int rarity) {

		List<PlacementModifier> placementModifiers = new ArrayList<>();
		if (count != 0) placementModifiers.add(CountPlacementModifier.create(count));
		if (rarity != 0) placementModifiers.add(RarityFilterPlacementModifier.create(rarity));
		placementModifiers.add(InSquarePlacementModifier.getInstance());
		placementModifiers.add(PlacedFeatureUtil.WORLD_SURFACE_WG_HEIGHTMAP);
		placementModifiers.add(BiomePlacementModifier.getInstance());

		return new PlacedFeature(Holder.createDirect(this.getConfiguredFeature()), placementModifiers);
	}

	@Override
	public PlacedFeature getDefaultPlacedFeature() {
		return this.createPlacedFeature(this.count.get(), this.rarity.get());
	}

	public CustomRandomPatchFeature addPlacedFeature(int count, int rarity, String idExt) {
		this.additionalPlacedFeatures.add(new Pair<>(this.createPlacedFeature(count, rarity), idExt));
		return this;
	}

	@Override
	public List<Pair<PlacedFeature, String>> getAdditionalPlacedFeatures() {
		return this.additionalPlacedFeatures;
	}

	@Override
	public void addDefaultToBiomes(Predicate<BiomeSelectionContext> ctx) {
		this.addAdditionalToBiomes(ctx, "");
	}

	@Override
	public void addAdditionalToBiomes(Predicate<BiomeSelectionContext> ctx, String idExt) {
		if (this.registered.get()) {
			BiomeModifications.addFeature(
				ctx, GenerationStep.Feature.VEGETAL_DECORATION,
				RegistryKey.of(Registry.PLACED_FEATURE_KEY, this.addIdExt(this.identifier.get(), idExt))
			);
		}
	}

	@Override
	public void setIdentifier(Identifier identifier) {
		this.identifier.set(identifier);
	}

	@Override
	public boolean isNotRegistered() {
		return !this.registered.get();
	}

	@Override
	public void setRegistered() {
		this.registered.set(true);
	}
}
