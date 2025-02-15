package com.mentalfrostbyte.jello.module.impl.render;

import com.mentalfrostbyte.jello.event.impl.game.network.EventGetLocationSkin;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRenderEntity;
import com.mentalfrostbyte.jello.event.impl.game.EventReplaceText;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.module.settings.impl.InputSetting;
import team.sdhq.eventBus.annotations.EventTarget;

public class Streaming extends Module {
    public Streaming() {
        super(ModuleCategory.RENDER, "Streaming", "Useful module when recording or streaming");
        this.registerSetting(new BooleanSetting("Hide skins", "Spoof all players skin", true));
        this.registerSetting(new BooleanSetting("Hide server name", "Spoof server name", false));
        this.registerSetting(new InputSetting("Server name", "The server name that you need to hide", "servernamehere"));
        this.registerSetting(new BooleanSetting("Hide date", "Hide date on scoreboard", false));
    }

    @EventTarget
    public void onTextReplace(EventReplaceText event) {
        if (this.isEnabled()) {
            if (this.getBooleanValueFromSettingName("Hide server name") && this.getStringSettingValueByName("Server name").length() > 1) {
                event.setText(event.setText().replaceAll(this.getStringSettingValueByName("Server name"), "sigmaclient"));
                event.setText(event.setText().replaceAll(this.getStringSettingValueByName("Server name").toLowerCase(), "sigmaclient"));
                event.setText(event.setText().replaceAll(this.getStringSettingValueByName("Server name").toUpperCase(), "sigmaclient"));
            }
        }
    }

    @EventTarget
    public void onGetLocationSkin(EventGetLocationSkin event) {
        if (this.getBooleanValueFromSettingName("Hide skins")) {
            event.cancelled = true;
        }
    }
}