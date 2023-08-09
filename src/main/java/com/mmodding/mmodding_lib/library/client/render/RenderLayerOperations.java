package com.mmodding.mmodding_lib.library.client.render;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayer;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.block.extensions.api.client.BlockRenderLayerMap;

@ClientOnly
public class RenderLayerOperations {

    public static void setCutout(Block block) {
		BlockRenderLayerMap.put(RenderLayer.getCutout(), block);
    }

    public static void setTranslucent(Block block) {
		BlockRenderLayerMap.put(RenderLayer.getTranslucent(), block);
    }

	public static void setTransparent(Block block) {
		FluidRenderHandlerRegistry.INSTANCE.setBlockTransparency(block, true);
	}
}
