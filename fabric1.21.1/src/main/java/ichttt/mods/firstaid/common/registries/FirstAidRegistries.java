/*
 * FirstAid
 * Copyright (C) 2017-2024
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ichttt.mods.firstaid.common.registries;

import com.mojang.serialization.MapCodec;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.debuff.IDebuffBuilder;
import ichttt.mods.firstaid.api.distribution.IDamageDistributionAlgorithm;
import ichttt.mods.firstaid.api.distribution.IDamageDistributionTarget;
import ichttt.mods.firstaid.common.apiimpl.StaticDamageDistributionTarget;
import ichttt.mods.firstaid.common.apiimpl.TagDamageDistributionTarget;
import ichttt.mods.firstaid.common.damagesystem.debuff.builder.ConstantDebuffBuilder;
import ichttt.mods.firstaid.common.damagesystem.debuff.builder.OnHitDebuffBuilder;
import ichttt.mods.firstaid.common.damagesystem.distribution.DirectDamageDistributionAlgorithm;
import ichttt.mods.firstaid.common.damagesystem.distribution.EqualDamageDistributionAlgorithm;
import ichttt.mods.firstaid.common.damagesystem.distribution.RandomDamageDistributionAlgorithm;
import ichttt.mods.firstaid.common.damagesystem.distribution.StandardDamageDistributionAlgorithm;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;

public final class FirstAidRegistries {

    public static final class Keys {
        public static final ResourceKey<Registry<MapCodec<? extends IDamageDistributionAlgorithm>>> DAMAGE_DISTRIBUTION_ALGORITHMS = key("damage_distribution_algorithms");
        public static final ResourceKey<Registry<MapCodec<? extends IDamageDistributionTarget>>> DAMAGE_DISTRIBUTION_TARGETS = key("damage_distribution_targets");

        public static final ResourceKey<Registry<MapCodec<? extends IDebuffBuilder>>> DEBUFF_BUILDERS = key("debuff_builders");

        private static <T> ResourceKey<Registry<T>> key(String name) {
            return ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(FirstAid.MODID, name));
        }

        private Keys() {
        }
    }

    public static final Registry<MapCodec<? extends IDamageDistributionAlgorithm>> DAMAGE_DISTRIBUTION_ALGORITHMS = FabricRegistryBuilder
            .createSimple(Keys.DAMAGE_DISTRIBUTION_ALGORITHMS)
            .attribute(RegistryAttribute.SYNCED)
            .buildAndRegister();

    public static final Registry<MapCodec<? extends IDamageDistributionTarget>> DAMAGE_DISTRIBUTION_TARGETS = FabricRegistryBuilder
            .createSimple(Keys.DAMAGE_DISTRIBUTION_TARGETS)
            .attribute(RegistryAttribute.SYNCED)
            .buildAndRegister();

    public static final Registry<MapCodec<? extends IDebuffBuilder>> DEBUFF_BUILDERS = FabricRegistryBuilder
            .createSimple(Keys.DEBUFF_BUILDERS)
            .attribute(RegistryAttribute.SYNCED)
            .buildAndRegister();

    static {
        Registry.register(DAMAGE_DISTRIBUTION_ALGORITHMS, id("direct"), DirectDamageDistributionAlgorithm.CODEC);
        Registry.register(DAMAGE_DISTRIBUTION_ALGORITHMS, id("equal"), EqualDamageDistributionAlgorithm.CODEC);
        Registry.register(DAMAGE_DISTRIBUTION_ALGORITHMS, id("random"), RandomDamageDistributionAlgorithm.CODEC);
        Registry.register(DAMAGE_DISTRIBUTION_ALGORITHMS, id("standard"), StandardDamageDistributionAlgorithm.CODEC);

        Registry.register(DAMAGE_DISTRIBUTION_TARGETS, id("static"), StaticDamageDistributionTarget.CODEC);
        Registry.register(DAMAGE_DISTRIBUTION_TARGETS, id("tag"), TagDamageDistributionTarget.CODEC);

        Registry.register(DEBUFF_BUILDERS, id("constant"), ConstantDebuffBuilder.CODEC);
        Registry.register(DEBUFF_BUILDERS, id("on_hit"), OnHitDebuffBuilder.CODEC);
    }

    private FirstAidRegistries() {
    }

    public static void bootstrap() {
        // Trigger class loading.
    }

    public static void clearDataRegistries() {
        // No-op: data-driven registries are loaded once at startup.
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(FirstAid.MODID, path);
    }
}
