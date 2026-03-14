/*
 * FirstAid API
 * Copyright (c) 2017-2024
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

package ichttt.mods.firstaid.api.event;

import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;

/**
 * Fired when the damage has been applied.
 * Canceling this event will cause the damage to be reset to {@link #getBeforeDamage()}.
 */
public final class FirstAidLivingDamageEvent {

    public interface Callback {
        void onDamage(FirstAidLivingDamageEvent event);
    }

    public static final Event<Callback> EVENT = EventFactory.createArrayBacked(Callback.class, callbacks -> event -> {
        for (Callback callback : callbacks) {
            callback.onDamage(event);
        }
    });

    private final Player player;
    private final AbstractPlayerDamageModel afterDamageDone;
    private final AbstractPlayerDamageModel beforeDamageDone;
    private final DamageSource source;
    private final float undistributedDamage;
    private boolean canceled;

    public FirstAidLivingDamageEvent(Player player, AbstractPlayerDamageModel afterDamageDone, AbstractPlayerDamageModel beforeDamageDone, DamageSource source, float undistributedDamage) {
        this.player = player;
        this.afterDamageDone = afterDamageDone;
        this.beforeDamageDone = beforeDamageDone;
        this.source = source;
        this.undistributedDamage = undistributedDamage;
    }

    public Player getPlayer() {
        return player;
    }

    /**
     * @return The damage model before this damage got applied. Canceling this event causes this to be restored.
     */
    public AbstractPlayerDamageModel getAfterDamage() {
        return this.afterDamageDone;
    }

    /**
     * @return The damage model after this damage got applied. Not canceling this event causes this to applied.
     */
    public AbstractPlayerDamageModel getBeforeDamage() {
        return this.beforeDamageDone;
    }

    /**
     * @return The source from where the damage came.
     */
    public DamageSource getSource() {
        return this.source;
    }

    /**
     * @return The damage that could not be distributed on any.
     */
    public float getUndistributedDamage() {
        return this.undistributedDamage;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
}

