package com.mmodding.mmodding_lib.client;

import com.google.gson.JsonParser;
import com.mmodding.mmodding_lib.library.client.events.ClientConfigNetworkingEvents;
import com.mmodding.mmodding_lib.library.config.Config;
import com.mmodding.mmodding_lib.library.config.ConfigObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

@Environment(EnvType.CLIENT)
public class ClientPacketReceivers {

	public static void register() {

		ClientPlayNetworking.registerGlobalReceiver(new Identifier("configs-channel"), ((client, handler, buf, responseSender) -> {
			String configName = buf.readString();
			String configContent = buf.readString();

			ClientConfigNetworkingEvents.BEFORE.invoker().beforeConfigReceived(MModdingLibClient.clientConfigs.get(configName));

			MModdingLibClient.clientConfigs.get(configName).saveConfig(new ConfigObject(JsonParser.parseString(configContent).getAsJsonObject()));
			Config config = MModdingLibClient.clientConfigs.get(configName);

			ClientConfigNetworkingEvents.AFTER.invoker().afterConfigReceived(config);
		}));
	}
}