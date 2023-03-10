package com.mmodding.mmodding_lib.library.config.screen.editing;

import com.mmodding.mmodding_lib.library.config.ConfigObject;
import com.mmodding.mmodding_lib.library.config.screen.ConfigElementsListEntry;
import com.mmodding.mmodding_lib.library.config.screen.ConfigElementsListWidget;
import com.mmodding.mmodding_lib.library.config.screen.ConfigScreen;
import com.mmodding.mmodding_lib.library.screens.widgets.NumberFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class NumberEditScreen extends EditScreen<Number> {

	private NumberFieldWidget numberBox;

	public NumberEditScreen(ConfigScreen lastScreen, ConfigElementsListWidget list, ConfigElementsListEntry entry, ConfigObject mutableConfig, String fieldName, ConfigObject.Value<Number> baseValue) {
		super(lastScreen, list, entry, mutableConfig, fieldName, baseValue);
	}

	@Override
	protected void init() {
		super.init();
		assert this.client != null;
		this.addSelectableChild(this.numberBox = new NumberFieldWidget(this.textRenderer, this.width / 2 - 100, this.height / 2, 200, 20, Text.of(this.baseValue.getValue())));
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);
		this.numberBox.render(matrices, mouseX, mouseY, delta);
	}

	@Override
	public void keepAndClose() {
		this.keep(new ConfigObject.Value<>(this.numberBox.getNumber()));
		this.close();
	}
}
