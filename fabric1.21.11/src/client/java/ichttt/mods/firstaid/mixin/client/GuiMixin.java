package ichttt.mods.firstaid.mixin.client;

import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.client.gui.FirstaidIngameGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Inject(method = "renderPlayerHealth", at = @At("HEAD"), cancellable = true)
    private void firstaid$renderPlayerHealth(GuiGraphics guiGraphics, CallbackInfo ci) {
        FirstAidConfig.Client.VanillaHealthbarMode mode = FirstAidConfig.CLIENT.vanillaHealthBarMode.get();
        if (mode == FirstAidConfig.Client.VanillaHealthbarMode.NORMAL) {
            return;
        }

        ci.cancel();
        if (mode != FirstAidConfig.Client.VanillaHealthbarMode.HIGHLIGHT_CRITICAL_PATH
                || FirstAidConfig.SERVER.vanillaHealthCalculation.get() != FirstAidConfig.Server.VanillaHealthCalculationMode.AVERAGE_ALL) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Gui gui = (Gui) (Object) this;
        if (mc.gameMode != null && mc.gameMode.canHurtPlayer() && !mc.options.hideGui) {
            FirstaidIngameGui.renderHealth(gui, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight(), guiGraphics);
        }
    }
}
