package kr.acorntracker;

import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4185;

public final class AcornButton extends class_4185 {
   private boolean selected;

   public AcornButton(int x, int y, int width, int height, String label, class_4185.class_4241 action) {
      super(x, y, width, height, class_2561.method_43470(label), action, field_40754);
   }

   public AcornButton selected(boolean selected) {
      this.selected = selected;
      return this;
   }

   protected void method_48579(class_332 ctx, int mouseX, int mouseY, float delta) {
      boolean hover = this.method_49606() || this.method_25370();
      int tint = !this.field_22763 ? -7634831 : (this.selected ? -5120113 : (hover ? -6737 : -1));
      WoodlandSkin.button(ctx, this.method_46426(), this.method_46427(), this.field_22758, this.field_22759, tint);
      if (this.method_25370()) {
         ctx.method_73198(this.method_46426() + 3, this.method_46427() + 2, this.field_22758 - 6, this.field_22759 - 4, -5699);
      }

      this.method_49604(ctx, class_310.method_1551().field_1772, 8, !this.field_22763 ? -4412270 : -5699);
   }
}
