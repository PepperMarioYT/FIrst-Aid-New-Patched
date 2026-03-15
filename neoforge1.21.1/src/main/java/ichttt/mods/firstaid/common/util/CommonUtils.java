/*
 * FirstAid
 * Copyright (C) 2017-2024
 * Modified 2026 by PepperMarioYT
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package ichttt.mods.firstaid.common.util;

import com.google.common.primitives.Ints;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.compat.playerrevive.IPRCompatHandler;
import ichttt.mods.firstaid.common.compat.playerrevive.PRCompatManager;
import ichttt.mods.firstaid.common.damagesystem.distribution.HealthDistribution;
import ichttt.mods.firstaid.common.init.FirstAidDataAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class CommonUtils {
    @Nonnull
    public static final EquipmentSlot[] ARMOR_SLOTS;
    @Nonnull
    private static final Map<EquipmentSlot, List<EnumPlayerPart>> SLOT_TO_PARTS;

    static {
        ARMOR_SLOTS = new EquipmentSlot[4];
        ARMOR_SLOTS[3] = EquipmentSlot.HEAD;
        ARMOR_SLOTS[2] = EquipmentSlot.CHEST;
        ARMOR_SLOTS[1] = EquipmentSlot.LEGS;
        ARMOR_SLOTS[0] = EquipmentSlot.FEET;
        SLOT_TO_PARTS = new EnumMap<>(EquipmentSlot.class);
        SLOT_TO_PARTS.put(EquipmentSlot.HEAD, Collections.singletonList(EnumPlayerPart.HEAD));
        SLOT_TO_PARTS.put(EquipmentSlot.CHEST, Arrays.asList(EnumPlayerPart.LEFT_ARM, EnumPlayerPart.RIGHT_ARM, EnumPlayerPart.BODY));
        SLOT_TO_PARTS.put(EquipmentSlot.LEGS, Arrays.asList(EnumPlayerPart.LEFT_LEG, EnumPlayerPart.RIGHT_LEG));
        SLOT_TO_PARTS.put(EquipmentSlot.FEET, Arrays.asList(EnumPlayerPart.LEFT_FOOT, EnumPlayerPart.RIGHT_FOOT));
    }

    public static List<EnumPlayerPart> getPartListForSlot(EquipmentSlot slot) {
        List<EnumPlayerPart> parts = SLOT_TO_PARTS.get(slot);
        return parts == null ? new ArrayList<>() : new ArrayList<>(parts);
    }

    public static EnumPlayerPart[] getPartArrayForSlot(EquipmentSlot slot) {
        return getPartListForSlot(slot).toArray(new EnumPlayerPart[0]);
    }

    public static void killPlayer(@Nonnull AbstractPlayerDamageModel damageModel, @Nonnull Player player, @Nullable DamageSource source) {
        if (player.level().isClientSide()) {
            try {
                throw new RuntimeException("Tried to kill the player on the client!");
            } catch (RuntimeException e) {
                FirstAid.LOGGER.warn("Tried to kill the player on the client! This should only happen on the server! Ignoring...", e);
            }
        }
        IPRCompatHandler handler = PRCompatManager.getHandler();
        if (!handler.isBleeding(player)) {
            if (!handler.tryKnockOutPlayer(player, source)) {
                killPlayerDirectly(player, source);
            }
        }
    }

    public static void killPlayerDirectly(@Nonnull Player player, @Nullable DamageSource source) {
        DamageSource resolvedSource = source != null ? source : player.damageSources().generic();
        player.setHealth(0.0F);
        player.die(resolvedSource);
    }

    public static boolean isValidArmorSlot(EquipmentSlot slot) {
        return slot != null && slot.isArmor() && SLOT_TO_PARTS.containsKey(slot);
    }

    @Nonnull
    public static String getActiveModidSafe() {
        ModContainer activeModContainer = ModLoadingContext.get().getActiveContainer();
        return activeModContainer == null ? "UNKNOWN-NULL" : activeModContainer.getModId();
    }

    public static void healPlayerByPercentage(double percentage, AbstractPlayerDamageModel damageModel, Player player) {
        Objects.requireNonNull(damageModel);
        int healValue = Ints.checkedCast(Math.round(damageModel.getCurrentMaxHealth() * percentage));
        HealthDistribution.manageHealth(healValue, damageModel, player, true, false);
    }

    public static void healAllPartsByPercentage(double percentage, AbstractPlayerDamageModel damageModel, Player player) {
        Objects.requireNonNull(damageModel);
        boolean applyDebuff = !player.level().isClientSide();
        for (AbstractDamageablePart part : damageModel) {
            float missingHealth = part.getMaxHealth() - part.currentHealth;
            if (missingHealth <= 0.0F) {
                continue;
            }
            float healAmount = Math.max(0.0F, Math.round((float) (part.getMaxHealth() * percentage) * 100.0F) / 100.0F);
            if (healAmount <= 0.0F) {
                continue;
            }
            part.heal(healAmount, player, applyDebuff);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.syncData(FirstAidDataAttachments.DAMAGE_MODEL.get());
        }
    }

    public static void debugLogStacktrace(String name) {
        if (!FirstAidConfig.GENERAL.debug.get()) return;
        try {
            throw new RuntimeException("DEBUG:" + name);
        } catch (RuntimeException e) {
            FirstAid.LOGGER.info("DEBUG: " + name, e);
        }
    }

    @Nullable
    public static AbstractPlayerDamageModel getDamageModel(Player player) {
        if (player == null) return null; // Added safety check
        LazyOptional<AbstractPlayerDamageModel> optionalDamageModel = getOptionalDamageModel(player);
        try {
            return optionalDamageModel.orElseThrow(() -> new IllegalArgumentException("Player " + player.getName().getString() + " is missing a damage model!"));
        } catch (IllegalArgumentException e) {
            if (FirstAidConfig.GENERAL.debug.get()) {
                FirstAid.LOGGER.fatal("Mandatory damage model missing!", e);
                throw e;
            } else {
                FirstAid.LOGGER.error("Missing a damage model, skipping further processing!", e);
                return null;
            }
        }
    }

    @Nonnull
    public static LazyOptional<AbstractPlayerDamageModel> getOptionalDamageModel(Player player) {
        // PATCH: Added null check to prevent NPE during keypress events
        if (player == null) {
            return LazyOptional.empty();
        }
        return LazyOptional.of(() -> player.getData(FirstAidDataAttachments.DAMAGE_MODEL.get()));
    }

    @Nullable
    public static AbstractPlayerDamageModel getExistingDamageModel(Player player) {
        if (player == null) return null;
        return player.getExistingDataOrNull(FirstAidDataAttachments.DAMAGE_MODEL.get());
    }

    public static boolean hasDamageModel(Entity entity) {
        return entity instanceof Player && !(entity instanceof FakePlayer);
    }

    @Nonnull
    public static ServerPlayer checkServer(NetworkEvent.Context context) {
        if (context.getDirection() != NetworkDirection.PLAY_TO_SERVER)
            throw new IllegalArgumentException("Wrong side for server packet handler " + context.getDirection());
        context.setPacketHandled(true);
        return Objects.requireNonNull(context.getSender());
    }

    public static void checkClient(NetworkEvent.Context context) {
        if (context.getDirection() != NetworkDirection.PLAY_TO_CLIENT)
            throw new IllegalArgumentException("Wrong side for client packet handler: " + context.getDirection());
        context.setPacketHandled(true);
    }
}
