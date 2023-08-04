package com.mmodding.mmodding_lib.ducks;

import com.mmodding.mmodding_lib.persistentstates.DifferedSeedsState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public interface GeneratorOptionsDuckInterface {

	void mmodding_lib$fillDimensionSeedAddendsNbt(NbtList list);

	boolean mmodding_lib$containsDimensionSeedAddend(RegistryKey<World> worldKey);

	void mmodding_lib$addDimensionSeedAddend(RegistryKey<World> worldKey, long differed);

	long mmodding_lib$getDimensionSeedAddend(RegistryKey<World> worldKey);

	DifferedSeedsState mmodding_lib$createDifferedSeedsState();

	DifferedSeedsState mmodding_lib$stateFromNbt(NbtCompound nbt);
}
