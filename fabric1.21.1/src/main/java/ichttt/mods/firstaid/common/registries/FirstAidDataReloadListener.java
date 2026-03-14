package ichttt.mods.firstaid.common.registries;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.debuff.IDebuffBuilder;
import ichttt.mods.firstaid.api.distribution.IDamageDistributionTarget;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public final class FirstAidDataReloadListener extends SimplePreparableReloadListener<FirstAidDataReloadListener.Data> implements IdentifiableResourceReloadListener {
    private static final FileToIdConverter DAMAGE_LISTER = FileToIdConverter.json("firstaid/damage_distributions");
    private static final FileToIdConverter DEBUFF_LISTER = FileToIdConverter.json("firstaid/debuffs");
    private static final ResourceLocation RELOAD_ID = ResourceLocation.fromNamespaceAndPath(FirstAid.MODID, "data_reload");

    @Override
    protected Data prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, IDamageDistributionTarget> damageTargets = new HashMap<>();
        Map<ResourceLocation, IDebuffBuilder> debuffBuilders = new HashMap<>();
        loadFirstAidData(resourceManager, DAMAGE_LISTER, FirstAidBaseCodecs.DAMAGE_DISTRIBUTION_TARGETS_DIRECT_CODEC, damageTargets);
        loadFirstAidData(resourceManager, DEBUFF_LISTER, FirstAidBaseCodecs.DEBUFF_BUILDERS_DIRECT_CODEC, debuffBuilders);
        return new Data(damageTargets, debuffBuilders);
    }

    @Override
    protected void apply(Data data, ResourceManager resourceManager, ProfilerFiller profiler) {
        FirstAidRegistryLookups.updateData(data.damageTargets, data.debuffBuilders);
    }

    @Override
    public ResourceLocation getFabricId() {
        return RELOAD_ID;
    }

    private static <T> void loadFirstAidData(ResourceManager resourceManager,
                                             FileToIdConverter lister,
                                             Codec<T> codec,
                                             Map<ResourceLocation, T> output) {
        for (Map.Entry<ResourceLocation, Resource> entry : lister.listMatchingResources(resourceManager).entrySet()) {
            ResourceLocation resourceId = entry.getKey();
            if (!FirstAid.MODID.equals(resourceId.getNamespace())) {
                continue;
            }
            ResourceLocation id = lister.fileToId(resourceId);
            try (Reader reader = entry.getValue().openAsReader()) {
                JsonElement jsonElement = JsonParser.parseReader(reader);
                codec.parse(JsonOps.INSTANCE, jsonElement)
                        .ifSuccess(value -> output.put(id, value))
                        .ifError(error -> FirstAid.LOGGER.error("Couldn't parse data file '{}' from '{}': {}", id, resourceId, error));
            } catch (IllegalArgumentException | IOException ex) {
                FirstAid.LOGGER.error("Couldn't parse data file '{}' from '{}'", id, resourceId, ex);
            }
        }
    }

    record Data(Map<ResourceLocation, IDamageDistributionTarget> damageTargets, Map<ResourceLocation, IDebuffBuilder> debuffBuilders) {
    }
}
