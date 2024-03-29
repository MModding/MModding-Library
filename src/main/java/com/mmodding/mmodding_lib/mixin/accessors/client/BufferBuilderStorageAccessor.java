package com.mmodding.mmodding_lib.mixin.accessors.client;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.SortedMap;

@Mixin(BufferBuilderStorage.class)
public interface BufferBuilderStorageAccessor {

	@Accessor
	SortedMap<RenderLayer, BufferBuilder> getEntityBuilders();
}
