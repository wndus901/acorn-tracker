package kr.acorntracker;

import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_357;

public abstract class WoodlandSlider extends class_357 {
   protected WoodlandSlider(int x, int y, int width, int height, double value) {
      super(x, y, width, height, class_2561.method_43473(), value);
   }

   public void method_48579(class_332 ctx, int mouseX, int mouseY, float delta) {
      WoodlandSkin.button(ctx, this.method_46426(), this.method_46427(), this.field_22758, this.field_22759, -6456997);
      int handleX = this.method_46426() + 3 + (int)Math.round((double)(this.field_22758 - 16) * this.field_22753);
      WoodlandSkin.button(ctx, handleX, this.method_46427(), 13, this.field_22759, !this.method_49606() && !this.method_25370() ? -6304148 : -3478135);
      this.method_49604(ctx, class_310.method_1551().field_1772, 9, -5699);
      if (this.method_25370()) {
         ctx.method_73198(this.method_46426() + 3, this.method_46427() + 2, this.field_22758 - 6, this.field_22759 - 4, -5699);
      }

   }
}
