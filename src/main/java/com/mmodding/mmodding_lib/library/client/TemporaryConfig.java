package com.mmodding.mmodding_lib.library.client;

import com.mmodding.mmodding_lib.library.config.Config;
import com.mmodding.mmodding_lib.library.config.ConfigObject;
import com.mmodding.mmodding_lib.library.config.ConfigScreenOptions;

public abstract class TemporaryConfig implements Config {

	private ConfigObject configObject = Config.super.getContent();
	@Override
	public void saveConfig(ConfigObject configObject) {
		this.configObject = configObject;
	}

	@Override
	public ConfigObject getContent() {
		return this.configObject;
	}

	public static TemporaryConfig fromConfig(Config config) {

		return new TemporaryConfig() {

			@Override
			public String getConfigName() {
				return config.getConfigName();
			}

			@Override
			public String getFileName() {
				return config.getFileName();
			}

			@Override
			public ConfigObject defaultConfig() {
				return config.defaultConfig();
			}

			@Override
			public ConfigScreenOptions getConfigOptions() {
				return config.getConfigOptions();
			}
		};
	}
}