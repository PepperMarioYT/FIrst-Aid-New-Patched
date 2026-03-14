package ichttt.mods.firstaid.client;

import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public final class RenderStateExtensions {
    public static final RenderStateDataKey<Boolean> PASSENGER = RenderStateDataKey.create(() -> "firstaid:passenger");
    public static final RenderStateDataKey<Boolean> UNCONSCIOUS = RenderStateDataKey.create(() -> "firstaid:unconscious");

    private RenderStateExtensions() {
    }

    public static boolean shouldApplyUnconsciousAttributes(LivingEntityRenderState renderState) {
        if (!(renderState instanceof FabricRenderState fabricState)) {
            return false;
        }
        boolean unconscious = fabricState.getDataOrDefault(UNCONSCIOUS, false);
        boolean passenger = fabricState.getDataOrDefault(PASSENGER, false);
        return unconscious && !passenger;
    }
}
