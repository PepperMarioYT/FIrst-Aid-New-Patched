package ichttt.mods.firstaid.mixin;

import ichttt.mods.firstaid.common.FirstAidDamageModelHolder;
import ichttt.mods.firstaid.common.damagesystem.PlayerDamageModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(Player.class)
public abstract class PlayerEntityMixin implements FirstAidDamageModelHolder {
    private static final String FIRSTAID_NBT_KEY = "FirstAidDamageModel";

    @Unique
    private PlayerDamageModel firstaid$damageModel;

    @Override
    public PlayerDamageModel firstaid$getDamageModel() {
        if (firstaid$damageModel == null) {
            firstaid$damageModel = new PlayerDamageModel();
        }
        return firstaid$damageModel;
    }

    @Override
    public @Nullable PlayerDamageModel firstaid$getDamageModelNullable() {
        return firstaid$damageModel;
    }

    @Override
    public void firstaid$setDamageModel(PlayerDamageModel model) {
        this.firstaid$damageModel = model;
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void firstaid$readAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains(FIRSTAID_NBT_KEY, Tag.TAG_COMPOUND)) {
            firstaid$getDamageModel().deserializeNBT(tag.getCompound(FIRSTAID_NBT_KEY));
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void firstaid$addAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        PlayerDamageModel model = firstaid$getDamageModelNullable();
        if (model != null) {
            tag.put(FIRSTAID_NBT_KEY, model.serializeNBT());
        }
    }
}
