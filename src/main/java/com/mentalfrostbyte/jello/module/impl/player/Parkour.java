package com.mentalfrostbyte.jello.module.impl.player;

import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.util.game.player.MovementUtil2;
import team.sdhq.eventBus.annotations.EventTarget;

public class Parkour extends Module {
    public Parkour() {
        super(ModuleCategory.PLAYER, "Parkour", "Automatically jumps at the edge of blocks");
    }

    // doesnt jump not fixing rn
    @EventTarget
    public void EventWalkingUpdate(EventPlayerTick event) {
        if (this.isEnabled()) {
            if (mc.player.isOnGround()) {
                if (!MovementUtil2.method17729()) {
                    mc.player.jump();
                }
            }
        }
    }
}
