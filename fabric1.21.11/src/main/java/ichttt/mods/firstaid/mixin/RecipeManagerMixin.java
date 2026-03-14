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

package ichttt.mods.firstaid.mixin;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import ichttt.mods.firstaid.FirstAid;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {
    private static final FileToIdConverter FIRSTAID_RECIPE_LISTER = FileToIdConverter.json("recipe");

    @Shadow
    @Final
    private HolderLookup.Provider registries;

    @Inject(
            method = "prepare",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/packs/resources/SimpleJsonResourceReloadListener;scanDirectory(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/resources/FileToIdConverter;Lcom/mojang/serialization/DynamicOps;Lcom/mojang/serialization/Codec;Ljava/util/Map;)V",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void firstaid$loadCustomRecipes(ResourceManager resourceManager,
                                            ProfilerFiller profiler,
                                            CallbackInfoReturnable<RecipeMap> cir,
                                            SortedMap<Identifier, Recipe<?>> sortedMap) {
        Map<Identifier, Recipe<?>> extraRecipes = new HashMap<>();
        RegistryOps<JsonElement> registryOps = this.registries.createSerializationContext(JsonOps.INSTANCE);
        loadFirstAidRecipes(resourceManager, registryOps, extraRecipes);
        if (extraRecipes.isEmpty()) {
            return;
        }
        extraRecipes.forEach((id, recipe) -> {
            if (sortedMap.containsKey(id)) {
                FirstAid.LOGGER.warn("Overriding recipe {} from data/{}/recipe", id, id.getNamespace());
            }
            sortedMap.put(id, recipe);
        });
    }

    private static void loadFirstAidRecipes(ResourceManager resourceManager,
                                            RegistryOps<JsonElement> registryOps,
                                            Map<Identifier, Recipe<?>> output) {
        for (Map.Entry<Identifier, Resource> entry : FIRSTAID_RECIPE_LISTER.listMatchingResources(resourceManager).entrySet()) {
            Identifier resourceId = entry.getKey();
            if (!FirstAid.MODID.equals(resourceId.getNamespace())) {
                continue;
            }
            Identifier id = FIRSTAID_RECIPE_LISTER.fileToId(resourceId);
            try (Reader reader = entry.getValue().openAsReader()) {
                JsonElement jsonElement = StrictJsonParser.parse(reader);
                Recipe.CODEC.parse(registryOps, jsonElement)
                        .ifSuccess(recipe -> output.put(id, recipe))
                        .ifError(error -> FirstAid.LOGGER.error("Couldn't parse data file '{}' from '{}': {}", id, resourceId, error));
            } catch (IllegalArgumentException | IOException ex) {
                FirstAid.LOGGER.error("Couldn't parse data file '{}' from '{}'", id, resourceId, ex);
            }
        }
    }
}
