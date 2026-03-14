package ichttt.mods.firstaid.fabric;

import ichttt.mods.firstaid.FirstAidClient;
import net.fabricmc.api.ClientModInitializer;

public class FirstAidFabricClientEntrypoint implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FirstAidClient.initClient();
    }
}
