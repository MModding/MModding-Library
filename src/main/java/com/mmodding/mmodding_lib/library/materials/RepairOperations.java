package com.mmodding.mmodding_lib.library.materials;

import net.minecraft.item.ItemStack;

public interface RepairOperations {

	boolean preventsRepair(ItemStack stack);

	void afterRepaired(ItemStack stack);
}
