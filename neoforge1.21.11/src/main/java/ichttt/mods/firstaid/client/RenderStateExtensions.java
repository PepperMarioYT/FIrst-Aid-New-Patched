package ichttt.mods.firstaid.client;

import ichttt.mods.firstaid.FirstAid;
import net.minecraft.util.context.ContextKey;
import net.neoforged.neoforge.client.renderstate.BaseRenderState;

public final class RenderStateExtensions {

    public static final ContextKey<Boolean> PASSENGER = create("passenger");
    public static final ContextKey<Boolean> UNCONSCIOUS = create("unconscious");

    private RenderStateExtensions() {
    }

    private static <T> ContextKey<T> create(String code) {
        return new ContextKey<>(net.minecraft.resources.Identifier.fromNamespaceAndPath(FirstAid.MODID, code));
    }

    public static boolean shouldApplyUnconsciousAttributes(BaseRenderState renderState) {
        boolean unconscious = renderState.getRenderDataOrDefault(UNCONSCIOUS, false);
        boolean passenger = renderState.getRenderDataOrDefault(PASSENGER, false);
        return unconscious && !passenger;
    }
}
