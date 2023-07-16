package com.mmodding.mmodding_lib.client;

import com.mmodding.mmodding_lib.MModdingLib;
import com.mmodding.mmodding_lib.library.config.screen.ConfigScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.ScreenTexts;
import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.loader.api.minecraft.ClientOnly;

@ClientOnly
@ApiStatus.Internal
public class MModdingScreen extends ConfigScreen {

	private final Screen lastScreen;

	private ButtonWidget returnButton;

	public MModdingScreen(Screen lastScreen) {
		super("mmodding", MModdingLib.config, lastScreen);
		this.lastScreen = lastScreen;
	}

	@Override
	protected void init() {
		assert this.client != null;
		this.client.keyboard.setRepeatEvents(true);
		this.returnButton = this.addDrawableChild(new ButtonWidget(
				this.width / 2 + 100,
				this.height - this.height / 4,
				98,
				20,
				ScreenTexts.DONE,
				button -> this.client.setScreen(lastScreen)
		));
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBlockTextureAsBackgroundTexture(new ConfigScreen.BlockTextureLocation("blue_terracotta.png"), 255, 255, 255, 255);
		drawCenteredText(matrices, this.textRenderer, "MModding Screen", this.width / 2, 10, 775184);
		this.returnButton.render(matrices, mouseX, mouseY, delta);
	}
}
