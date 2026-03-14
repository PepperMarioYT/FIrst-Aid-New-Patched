package ichttt.mods.firstaid.mixin.client;

import ichttt.mods.firstaid.client.ClientEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
    private void firstaid$turnPlayer(double movementTime, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && ClientEventHandler.isUnconscious(mc.player)) {
            ci.cancel();
        }
    }
}
