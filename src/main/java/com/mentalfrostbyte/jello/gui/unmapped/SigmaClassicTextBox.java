package com.mentalfrostbyte.jello.gui.unmapped;

import com.mentalfrostbyte.jello.gui.base.CustomGuiScreen;
import com.mentalfrostbyte.jello.util.ClientColors;
import com.mentalfrostbyte.jello.util.ColorHelper;
import com.mentalfrostbyte.jello.util.ResourceRegistry;
import com.mentalfrostbyte.jello.util.render.ColorUtils;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import org.newdawn.slick.TrueTypeFont;

public class SigmaClassicTextBox extends UIInput {
   private static String[] field20759;

   public SigmaClassicTextBox(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, ColorHelper var7, String var8, String var9, TrueTypeFont var10) {
      super(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10);
      this.setTextColor(new ColorHelper(var7).method19410(ClientColors.LIGHT_GREYISH_BLUE.getColor()));
      this.method13156(false);
   }

   @Override
   public void draw(float partialTicks) {
      this.setFont(ResourceRegistry.DefaultClientFont);
      RenderUtil.drawRoundedRect(
         (float)this.xA,
         (float)this.yA,
         (float)(this.xA + this.widthA),
         (float)(this.yA + this.heightA),
         ClientColors.DEEP_TEAL.getColor()
      );
      RenderUtil.method11429(
         (float)(this.xA - 2),
         (float)this.yA,
         (float)(this.xA + this.widthA + 2),
         (float)(this.yA + this.heightA),
         2,
         ColorUtils.method17690(ClientColors.LIGHT_GREYISH_BLUE.getColor(), ClientColors.DEEP_TEAL.getColor(), 625.0F)
      );
      super.draw(partialTicks);
   }
}
