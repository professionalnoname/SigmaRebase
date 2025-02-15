package com.mentalfrostbyte.jello.module.impl.render.jello;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.game.network.EventSendPacket;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender3D;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRenderNameTag;
import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.player.Blink;
import com.mentalfrostbyte.jello.module.impl.render.Freecam;
import com.mentalfrostbyte.jello.module.impl.render.NameProtect;
import com.mentalfrostbyte.jello.module.impl.render.jello.esp.util.Class8781;
import com.mentalfrostbyte.jello.module.impl.render.jello.nametags.Class7070;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.util.client.render.ResourceRegistry;
import com.mentalfrostbyte.jello.util.client.render.Resources;
import com.mentalfrostbyte.jello.util.client.render.theme.ClientColors;
import com.mentalfrostbyte.jello.util.game.player.PlayerUtil;
import com.mentalfrostbyte.jello.util.game.player.combat.CombatUtil;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil;
import com.mentalfrostbyte.jello.util.game.world.PositionUtil;
import com.mentalfrostbyte.jello.util.game.world.blocks.BlockUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.client.gui.screen.inventory.FurnaceScreen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.horse.HorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.network.play.server.SOpenWindowPacket;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.network.play.server.SWindowPropertyPacket;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.opengl.Texture;
import team.sdhq.eventBus.annotations.EventTarget;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;

public class NameTags extends Module {
    public static final HashMap<String, Texture> field24003 = new HashMap<>();

    static {
        field24003.put("Tomygaims", Resources.tomyPNG);
        field24003.put("Andro24", Resources.androPNG);
        field24003.put("Gretorm", Resources.lpPNG);
        field24003.put("Flyinqq", Resources.codyPNG);
        field24003.put("cxbot", Resources.cxPNG);
    }

    public int field24008 = RenderUtil.applyAlpha(RenderUtil
            .method17690(ClientColors.LIGHT_GREYISH_BLUE.getColor(), ClientColors.DEEP_TEAL.getColor(), 75.0F), 0.5F);
    public final HashMap<BlockPos, Class7070> field24000 = new HashMap<>();
    public BlockPos field24001;
    public final List<Entity> entities = new ArrayList<>();
    public boolean field24006 = false;
    public final HashMap<UUID, String> field24007 = new HashMap<>();

    public NameTags() {
        super(ModuleCategory.RENDER, "NameTags", "Render better name tags");
        this.registerSetting(new BooleanSetting("Magnify", "Scales nametags to keep them readable", true));
        this.registerSetting(new BooleanSetting("Furnaces", "Shows furnaces info once open", true));
        this.registerSetting(new BooleanSetting("Mob Owners", "Shows mob owners", true));
        this.setAvailableOnClassic(false);
    }

    @EventTarget
    public void onTick(EventPlayerTick event) {
        if (this.isEnabled()) {
            this.field24006 = this.getBooleanValueFromSettingName("Furnaces");
            if (!this.field24006) {
                this.field24000.clear();
            } else {
                Iterator<Entry<BlockPos, Class7070>> var4 = this.field24000.entrySet().iterator();

                while (var4.hasNext()) {
                    Entry<BlockPos, Class7070> var5 = var4.next();
                    if (!(mc.world.getBlockState(var5.getKey()).getBlock() instanceof FurnaceBlock)) {
                        var4.remove();
                    }

                    var5.getValue().method21984();
                }
            }

            this.entities.clear();

            for (Entity var7 : BlockUtil.method34549(CombatUtil.getAllPlayersInWorld())) {
                if (var7 != mc.player
                        && var7 != Freecam.player
                        && var7 != Blink.clientPlayerEntity
                        && !var7.isInvisible()
                        && !Client.getInstance().combatManager.isTargetABot(var7)) {
                    this.entities.add(var7);
                }
            }
        }
    }

    @EventTarget
    public void onSendPacket(EventSendPacket event) {
        if (this.isEnabled()) {
            if (event.getPacket() instanceof CPlayerTryUseItemOnBlockPacket) {
                CPlayerTryUseItemOnBlockPacket var4 = (CPlayerTryUseItemOnBlockPacket) event.getPacket();
                if (mc.world.getBlockState(var4.func_218794_c().getPos()).getBlock() instanceof FurnaceBlock) {
                    this.field24001 = var4.func_218794_c().getPos();
                }
            }

            if (event.getPacket() instanceof CClickWindowPacket) {
                CClickWindowPacket var7 = (CClickWindowPacket) event.getPacket();
                Class7070 var5 = this.method16929(var7.getWindowId());
                if (var5 == null) {
                    return;
                }

                if (mc.currentScreen instanceof FurnaceScreen) {
                    FurnaceScreen var6 = (FurnaceScreen) mc.currentScreen;
                    var5.field30453 = var6.getContainer().getSlot(0).getStack();
                    var5.field30454 = new ItemStack(var6.getContainer().getSlot(1).getStack().getItem());
                    var5.field30454.count = var6.getContainer().getSlot(1).getStack().count;
                    var5.field30455 = var6.getContainer().getSlot(2).getStack();
                }
            }
        }
    }

    @EventTarget
    public void onReceivePacket(EventReceivePacket event) {
        if (this.isEnabled()) {
            if (event.getPacket() instanceof SOpenWindowPacket) {
                SOpenWindowPacket sOpenWindowPacket = (SOpenWindowPacket) event.getPacket();
                if (sOpenWindowPacket.getContainerType() != ContainerType.FURNACE) {
                    return;
                }

                this.field24000.put(this.field24001, new Class7070(sOpenWindowPacket.getWindowId()));
            }

            if (event.getPacket() instanceof SSetSlotPacket) {
                SSetSlotPacket sSetSlotPacket = (SSetSlotPacket) event.getPacket();
                Class7070 var5 = this.method16929(sSetSlotPacket.getWindowId());
                if (var5 == null) {
                    return;
                }

                if (sSetSlotPacket.getSlot() == 0) {
                    var5.field30453 = new ItemStack(sSetSlotPacket.getStack().getItem());
                    var5.field30453.count = sSetSlotPacket.getStack().count;
                } else if (sSetSlotPacket.getSlot() == 1) {
                    var5.field30454 = new ItemStack(sSetSlotPacket.getStack().getItem());
                    var5.field30454.count = sSetSlotPacket.getStack().count;
                } else if (sSetSlotPacket.getSlot() == 2) {
                    var5.field30455 = new ItemStack(sSetSlotPacket.getStack().getItem());
                    var5.field30455.count = sSetSlotPacket.getStack().count;
                }
            }

            if (event.getPacket() instanceof SWindowPropertyPacket) {
                SWindowPropertyPacket sWindowPropertyPacket = (SWindowPropertyPacket) event.getPacket();
                Class7070 var8 = this.method16929(sWindowPropertyPacket.getWindowId());
                if (var8 == null) {
                    return;
                }

                switch (sWindowPropertyPacket.getProperty()) {
                    case 0:
                        var8.field30452 = sWindowPropertyPacket.getValue();
                        break;
                    case 1:
                        var8.field30451 = sWindowPropertyPacket.getValue();
                        break;
                    case 2:
                        var8.field30450 = (float) sWindowPropertyPacket.getValue();
                        break;
                    case 3:
                        var8.field30449 = (float) sWindowPropertyPacket.getValue();
                }
            }
        }
    }

    public Class7070 method16929(int var1) {
        for (Entry<BlockPos, Class7070> var5 : this.field24000.entrySet()) {
            if (var5.getValue().field30448 == var1) {
                return var5.getValue();
            }
        }

        return null;
    }

    @EventTarget
    public void on3D(EventRender3D event) {
        if (this.isEnabled()) {
            RenderSystem.glMultiTexCoord2f(33986, 240.0F, 240.0F);
            boolean shouldMagnify = this.getBooleanValueFromSettingName("Magnify");

            for (Entity entity : this.entities) {
                float scale = 1.0F;
                if (shouldMagnify) {
                    scale = (float) Math.max(1.0, Math.sqrt(PositionUtil.calculateDistanceSquared(entity) / 30.0));
                }

                this.drawNametag(
                        PositionUtil.getEntityPosition(entity).x,
                        PositionUtil.getEntityPosition(entity).y + (double) entity.getHeight(),
                        PositionUtil.getEntityPosition(entity).z,
                        entity,
                        scale,
                        null);
                entity.getDataManager().set(Entity.CUSTOM_NAME_VISIBLE, false);
            }

            for (Entry var11 : this.field24000.entrySet()) {
                float var13 = 1.0F;
                if (shouldMagnify) {
                    var13 = (float) Math.max(0.8F,
                            Math.sqrt(PositionUtil.calculateDistanceSquared((BlockPos) var11.getKey()) / 30.0));
                }

                this.method16932((BlockPos) var11.getKey(), (Class7070) var11.getValue(), var13);
            }

            if (this.getBooleanValueFromSettingName("Mob Owners")) {
                for (Entity entity : mc.world.getAllEntities()) {
                    if (entity instanceof TameableEntity || entity instanceof HorseEntity) {
                        UUID uuid = (entity instanceof TameableEntity)
                                ? ((TameableEntity) entity).getOwnerId()
                                : ((HorseEntity) entity).getOwnerUniqueId();
                        if (uuid != null) {
                            if (!this.field24007.containsKey(uuid)) {
                                this.field24007.put(uuid, null);

                                new Thread(() -> {
                                    try {
                                        List<String> var4x = PlayerUtil.getMobOwners(uuid.toString());
                                        if (var4x == null || var4x.isEmpty()) {
                                            return;
                                        }
                                        this.field24007.put(uuid, var4x.get(var4x.size() - 1));
                                    } catch (Exception ignored) {
                                    }
                                }).start();
                            }

                            if (this.field24007.get(uuid) != null) {
                                float var8 = 1.0F;
                                if (this.getBooleanValueFromSettingName("Magnify")) {
                                    var8 = (float) Math.max(1.0,
                                            Math.sqrt(PositionUtil.calculateDistanceSquared(entity) / 30.0));
                                }

                                this.drawNametag(
                                        PositionUtil.getEntityPosition(entity).x,
                                        PositionUtil.getEntityPosition(entity).y + (double) entity.getHeight(),
                                        PositionUtil.getEntityPosition(entity).z,
                                        entity,
                                        var8,
                                        this.field24007.get(uuid));
                                entity.getDataManager().set(Entity.CUSTOM_NAME_VISIBLE, false);
                            }
                        }
                    }
                }
            }

            GL11.glDisable(2896);
            RenderSystem.glMultiTexCoord2f(33986, 240.0F, 240.0F);
            mc.getTextureManager().bindTexture(TextureManager.RESOURCE_LOCATION_EMPTY);
        }
    }

    public void method16931(float var1, float var2, float var3, float var4) {
        GL11.glColor4f(var1 / 255.0F, var2 / 255.0F, var3 / 255.0F, var4);
        GL11.glTranslatef(0.0F, 0.0F, 0.3F);
        GL11.glNormal3f(0.0F, 0.0F, 1.0F);
        GL11.glRotated(-37.0, 1.0, 0.0, 0.0);
        GL11.glBegin(6);
        GL11.glVertex2f(0.0F, 0.0F);
        GL11.glVertex2f(0.0F, 0.5F);
        GL11.glVertex2f(0.5F, 0.5F);
        GL11.glVertex2f(0.5F, 0.0F);
        GL11.glEnd();
    }

    public void method16932(BlockPos var1, Class7070 var2, float var3) {
        TrueTypeFont var6 = ResourceRegistry.JelloLightFont25;
        String var7 = "None";
        if (var2.field30453 != null) {
            var7 = var2.field30453.count + " " + var2.field30453.getDisplayName();
        }

        float var8 = (float) ((double) var1.getX() - mc.gameRenderer.getActiveRenderInfo().getPos().getX() + 0.5);
        float var9 = (float) ((double) var1.getY() - mc.gameRenderer.getActiveRenderInfo().getPos().getY() + 1.0);
        float var10 = (float) ((double) var1.getZ() - mc.gameRenderer.getActiveRenderInfo().getPos().getZ() + 0.5);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(3042);
        GL11.glEnable(2848);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDisable(2896);
        GL11.glDepthMask(false);
        float var11 = Math.min(var2.field30450 / var2.field30449, 1.0F);
        float var12 = Math.min((float) var2.field30452 / (float) var2.field30451, 1.0F);
        int var13 = 14;
        GL11.glPushMatrix();
        GL11.glAlphaFunc(519, 0.0F);
        GL11.glTranslated(var8, var9 + 0.6F - 0.33333334F * (1.0F - var3), var10);
        GL11.glRotatef(mc.gameRenderer.getActiveRenderInfo().getYaw(), 0.0F, -1.0F, 0.0F);
        GL11.glRotatef(mc.gameRenderer.getActiveRenderInfo().getPitch(), 1.0F, 0.0F, 0.0F);
        GL11.glPushMatrix();
        float var14 = 0.008F;
        GL11.glScalef(-var14 * var3, -var14 * var3, -var14 * var3);
        int var15 = 0;
        ItemStack var16 = var2.method21987();
        if (var16 != null) {
            var15 = Math.max(ResourceRegistry.JelloLightFont20.getWidth(var16.getDisplayName().getString()), 50);
        } else {
            var15 = 37;
        }

        int var17 = 51 + var15 + var13 * 2;
        int var18 = 85 + var13 * 2;
        GL11.glTranslated(-var17 / 2, -var18 / 2, 0.0);
        RenderUtil.drawRect(0.0F, 0.0F, (float) var17, (float) var18, this.field24008);
        RenderUtil.drawRoundedRect(0.0F, 0.0F, (float) var17, (float) var18, 20.0F, 0.5F);
        RenderUtil.drawString(var6, var13, (float) (var13 - 5), "Furnace", ClientColors.LIGHT_GREYISH_BLUE.getColor());
        if (var16 == null) {
            RenderUtil.drawString(
                    ResourceRegistry.JelloLightFont20, (float) (var13 + 15), (float) (var13 + 40), "Empty",
                    RenderUtil.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.6F));
        }

        ItemStack var20 = var2.method21987();
        if (var20 != null) {
            RenderUtil.method11479(var20, var13, var13 + 27, 45, 45);
            RenderUtil.drawString(ResourceRegistry.JelloLightFont20, (float) (var13 + 51), 40.0F,
                    var20.getDisplayName().getString(), ClientColors.LIGHT_GREYISH_BLUE.getColor());
            RenderUtil.drawString(ResourceRegistry.JelloLightFont14, (float) (var13 + 51), 62.0F,
                    "Count: " + var20.count, ClientColors.LIGHT_GREYISH_BLUE.getColor());
        }

        RenderUtil.drawRect(0.0F, (float) var18 - 12.0F, Math.min((float) var17 * var12, (float) var17),
                (float) var18 - 6.0F, RenderUtil.applyAlpha(-106750, 0.3F));
        RenderUtil.drawRect(
                0.0F, (float) var18 - 6.0F, Math.min((float) var17 * var11, (float) var17), (float) var18,
                RenderUtil.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.75F));
        GL11.glPopMatrix();
        GL11.glPopMatrix();
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glEnable(2896);
        GL11.glDisable(2848);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
    }

    public void drawNametag(double x, double y, double z, Entity var7, float var8, String var9) {
        TrueTypeFont var12 = ResourceRegistry.JelloLightFont25;
        String var13 = var9 == null ? var7.getName().getString().replaceAll("§.", "") : var9;
        if (Client.getInstance().moduleManager.getModuleByClass(NameProtect.class).isEnabled()
                && var13.equals(mc.getSession().getUsername())) {
            var13 = Client.getInstance().moduleManager.getModuleByClass(NameProtect.class)
                    .getStringSettingValueByName("Username");
        }

        if (var13.length() != 0) {
            float var14 = (float) (x - mc.gameRenderer.getActiveRenderInfo().getPos().getX());
            float var15 = (float) (y - mc.gameRenderer.getActiveRenderInfo().getPos().getY());
            float var16 = (float) (z - mc.gameRenderer.getActiveRenderInfo().getPos().getZ());
            GL11.glBlendFunc(770, 771);
            GL11.glEnable(3042);
            GL11.glEnable(2848);
            GL11.glDisable(2929);
            GL11.glDisable(2896);
            GL11.glDepthMask(false);
            String var17 = (float) Math.round(((LivingEntity) var7).getHealth() * 10.0F) / 10.0F + "";
            float var18 = Math.min(((LivingEntity) var7).getHealth() / ((LivingEntity) var7).getMaxHealth(), 1.0F);
            GL11.glPushMatrix();
            GL11.glAlphaFunc(519, 0.0F);
            GL11.glTranslated(var14, var15 + 0.6F - 0.33333334F * (1.0F - var8), var16);
            GL11.glRotatef(mc.gameRenderer.getActiveRenderInfo().getYaw(), 0.0F, -1.0F, 0.0F);
            GL11.glRotatef(mc.gameRenderer.getActiveRenderInfo().getPitch(), 1.0F, 0.0F, 0.0F);
            GL11.glScalef(-0.009F * var8, -0.009F * var8, -0.009F * var8);
            int var19 = this.field24008;
            if (!Client.getInstance().friendManager.method26997(var7)) {
                if (Client.getInstance().friendManager.isFriend(var7)) {
                    var19 = RenderUtil.applyAlpha(-6750208, 0.5F);
                }
            } else {
                var19 = RenderUtil.applyAlpha(-16171506, 0.5F);
            }

            int var20 = RenderUtil
                    .applyAlpha(!(var7 instanceof PlayerEntity) ? ClientColors.LIGHT_GREYISH_BLUE.getColor()
                            : new Color(Class8781.method31663((PlayerEntity) var7)).getRGB(), 0.5F);
            int var21 = var12.getWidth(var13) / 2;
            if (!field24003.containsKey(var13)) {
                RenderUtil.drawRoundedRect((float) (-var21 - 10), -25.0F, (float) (var21 * 2 + 20),
                        (float) (var12.getHeight() + 27), 20.0F, 0.5F);
            } else {
                int var22 = Color.getHSBColor((float) (System.currentTimeMillis() % 10000L) / 10000.0F, 0.5F, 1.0F)
                        .getRGB();

                RenderUtil.drawImage(
                        (float) (-var21 - 10 - 31),
                        -25.0F, (float) (var12.getHeight() + 27),
                        (float) (var12.getHeight() + 27),
                        field24003.get(var13),
                        RenderUtil.applyAlpha(var22, 0.7f)
                );

                RenderUtil.drawImage((float) (-var21 - 10 - 31 + var12.getHeight() + 27), -25.0F, 14.0F,
                        (float) (var12.getHeight() + 27), Resources.shadowRightPNG,
                        RenderUtil.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.6F));

                RenderUtil.drawRoundedRect((float) (-var21 - 10 - 31), -25.0F, (float) (var21 * 2 + 20 + 31 + 27),
                        (float) (var12.getHeight() + 27), 20.0F, 0.5F);
                GL11.glTranslatef(27.0F, 0.0F, 0.0F);
            }

            RenderUtil.drawRect((float) (-var21 - 10), -25.0F, (float) (var21 + 10), (float) (var12.getHeight() + 2),
                    var19);
            RenderUtil.drawRect((float) (-var21 - 10),
                    (float) (var12.getHeight() - 1) - (float) ((LivingEntity) var7).hurtTime / 3.0F,
                    Math.min((float) (var21 * 2 + 20) * (var18 - 0.5F), (float) (var21 + 10)),
                    (float) (var12.getHeight() + 2), var20);
            GL11.glPushMatrix();
            GL11.glTranslated(-var12.getWidth(var13) / 2, 0.0, 0.0);
            int var26 = ResourceRegistry.JelloLightFont14.getWidth("Health: 20.0");
            String var23 = "Health: ";
            int var24 = var12.getWidth(var13);
            if (var26 > var24) {
                var23 = "H: ";
            }

            RenderUtil.drawString(var12, 0.0F, -20.0F, var13, ClientColors.LIGHT_GREYISH_BLUE.getColor());
            RenderUtil.drawString(ResourceRegistry.JelloLightFont14, 0.0F, 10.0F, var23 + var17,
                    ClientColors.LIGHT_GREYISH_BLUE.getColor());
            //        SigmaIRC.Class8433 var25 = Client.getInstance().networkManager.field38429.method29512(var7);
            //       if (var25 != null) {
            //       RenderUtil.drawString(ResourceRegistry.JelloLightFont14, 0.0F, -30.0F, var25.field36141,
            //               ClientColors.LIGHT_GREYISH_BLUE.getColor());
        }

        GL11.glPopMatrix();
        GL11.glPopMatrix();
        GL11.glEnable(2929);
        GL11.glEnable(2896);
        GL11.glDisable(2848);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
    }


    @EventTarget
    public void method16934(EventRenderNameTag event) {
        if (this.isEnabled()
                && event.getEntity() instanceof PlayerEntity) {
            event.cancelled = true;
        }
    }
}

