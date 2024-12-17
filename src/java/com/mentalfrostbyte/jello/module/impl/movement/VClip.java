package com.mentalfrostbyte.jello.module.impl.movement;

import com.mentalfrostbyte.Client;
import team.sdhq.eventBus.annotations.EventTarget;
import com.mentalfrostbyte.jello.event.impl.SendPacketEvent;
import com.mentalfrostbyte.jello.event.impl.ClickEvent;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.notification.Notification;
import com.mentalfrostbyte.jello.unmapped.ResourceList;
import net.minecraft.network.play.client.CChatMessagePacket;
import com.mentalfrostbyte.jello.util.MultiUtilities;
import net.minecraft.util.math.MathHelper;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.math.BlockPos;

public class VClip extends Module {
    public ModuleCategory field23576;
    public Module field23577;
    private final String field23578 = ">";

    public VClip() {
        super(ModuleCategory.MOVEMENT, "VClip", "Climp Walls like spiders!");
    }

    public static boolean method16291(String var0) {
        return var0.matches("-?\\d+(\\.\\d+)?");
    }

    @EventTarget
    private void method16289(ClickEvent var1) {
        if (this.isEnabled()) {
            if (mc.gameSettings.keyBindSneak.isKeyDown() && var1.getButton() == ClickEvent.Button.RIGHT) {
                if (!(mc.player.rotationPitch < 0.0F)) {
                    this.method16294(this.method16292());
                } else {
                    this.method16294(this.method16293());
                }
            }
        }
    }

    @EventTarget
    private void method16290(SendPacketEvent var1) {
        if (var1.getPacket() instanceof CChatMessagePacket) {
            CChatMessagePacket var4 = (CChatMessagePacket) var1.getPacket();
            String var5 = var4.getMessage();
            String var6 = "hclip";
            if (!var5.startsWith("/" + var6)) {
                return;
            }

            var1.setCancelled(true);
            int var7 = 0;
            var5 = var5.replace("/" + var6, "").replaceAll("\\s", "");
            if (method16291(var5)) {
                var7 = Integer.parseInt(var5);
            }

            float var8 = (float) Math.toRadians(mc.player.rotationYaw + 90.0F);
            double var9 = MathHelper.cos(var8) * (float) var7;
            double var11 = MathHelper.sin(var8) * (float) var7;
            mc.player
                    .setPosition(mc.player.getPosX() + var9, mc.player.getPosY(), mc.player.getPosZ() + var11);
        }
    }

    private int method16292() {
        boolean var3 = false;
        int var4 = 0;

        for (int var5 = 0; var5 < 10; var5++) {
            BlockPos var6 = new BlockPos(mc.player.getPosX(), mc.player.getPosY() - (double) var5, mc.player.getPosZ());
            if (mc.world.getBlockState(var6).isSolid() && var3) {
                var4 = -var5;
                break;
            }

            var3 = mc.world.getBlockState(var6).isSolid();
        }

        return var4;
    }

    private int method16293() {
        boolean var3 = false;
        int var4 = 0;

        for (int var5 = 10; var5 > 0; var5--) {
            BlockPos var6 = new BlockPos(mc.player.getPosX(), mc.player.getPosY() + (double) var5, mc.player.getPosZ());
            if (mc.world.getBlockState(var6).isSolid() && var3 && !mc.world.getBlockState(var6.down()).isSolid()) {
                var4 = var5;
                break;
            }

            var3 = mc.world.getBlockState(var6).isSolid();
        }

        return var4;
    }

    private void method16294(int var1) {
        if (var1 == 0) {
            MultiUtilities.addChatMessage("§cCouldn't VClip");
        } else {
            mc.getConnection()
                    .sendPacket(
                            new CPlayerPacket.PositionPacket(mc.player.getPosX(), mc.player.getPosY() + (double) var1, mc.player.getPosZ(), false)
                    );
            mc.player
                    .setPosition(mc.player.getPosX(), mc.player.getPosY() + (double) var1, mc.player.getPosZ());
            Client.getInstance().getNotificationManager().send(new Notification("Successfuly VCliped", var1 + " Blocks", 2000, ResourceList.directionIconPNG));
        }
    }
}
