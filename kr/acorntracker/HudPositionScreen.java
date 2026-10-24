package kr.acorntracker;

import java.util.Locale;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_364;
import net.minecraft.class_437;

public final class HudPositionScreen extends class_437 {
   private final class_437 parent;
   private final TrackerConfig config;
   private HudLayout.Bounds bounds;
   private boolean dragging;
   private double offsetX;
   private double offsetY;

   public HudPositionScreen(class_437 parent) {
      super(class_2561.method_43470("HUD 위치 이동"));
      this.config = AcornTrackerClient.INSTANCE.config();
      this.parent = parent;
   }

   protected void method_25426() {
      int total = Math.min(332, this.field_22789 - 16);
      int x = (this.field_22789 - total) / 2;
      int unit = (total - 12) / 3;
      this.method_37063(new WoodlandSlider(x, this.field_22790 - 54, total, 20, (Math.clamp(this.config.hudScale, (double)0.5F, (double)2.0F) - (double)0.5F) / (double)1.5F) {
         {
            this.method_25346();
         }

         protected void method_25346() {
            this.method_25355(class_2561.method_43470(String.format(Locale.ROOT, "HUD 크기  %.0f%%", ((double)0.5F + this.field_22753 * (double)1.5F) * (double)100.0F)));
         }

         protected void method_25344() {
            HudPositionScreen.this.config.hudScale = (double)0.5F + this.field_22753 * (double)1.5F;
            HudPositionScreen.this.save();
         }
      });
      this.method_37063(new AcornButton(x, this.field_22790 - 28, unit, 20, "왼쪽 위", (button) -> {
         this.config.hudX = this.config.hudY = (double)0.0F;
         this.save();
      }));
      this.method_37063(new AcornButton(x + unit + 6, this.field_22790 - 28, unit, 20, "오른쪽 위", (button) -> {
         this.config.hudX = (double)1.0F;
         this.config.hudY = (double)0.0F;
         this.save();
      }));
      this.method_37063((new AcornButton(x + 2 * (unit + 6), this.field_22790 - 28, unit, 20, "완료", (button) -> this.method_25419())).selected(true));
   }

   public void method_25394(class_332 ctx, int mouseX, int mouseY, float delta) {
      ctx.method_25294(0, 0, this.field_22789, this.field_22790, 1696082726);
      if (this.dragging) {
         ctx.method_25294(this.field_22789 / 2, 0, this.field_22789 / 2 + 1, this.field_22790, 947823751);
         ctx.method_25294(0, this.field_22790 / 2, this.field_22789, this.field_22790 / 2 + 1, 947823751);
      }

      this.bounds = HudRenderer.render(ctx, this.config, AcornTrackerClient.INSTANCE.hudRows(true), true);
      WoodlandSkin.button(ctx, Math.max(4, this.field_22789 / 2 - 132), this.field_22790 - 86, Math.min(264, this.field_22789 - 8), 25, -1);
      ctx.method_25300(this.field_22793, "HUD를 드래그 · 방향키 1px / Shift 10px", this.field_22789 / 2, this.field_22790 - 79, -5699);
      if (!this.config.hud) {
         ctx.method_25300(this.field_22793, "미리보기 · 현재 HUD 표시 꺼짐", this.field_22789 / 2, this.field_22790 - 99, -1458825);
      }

      super.method_25394(ctx, mouseX, mouseY, delta);
   }

   public boolean method_25402(class_11909 click, boolean doubled) {
      if (super.method_25402(click, doubled)) {
         return true;
      } else if (click.method_74245() == 0 && this.bounds != null && this.bounds.contains(click.comp_4798(), click.comp_4799())) {
         this.method_25395((class_364)null);
         this.dragging = true;
         this.offsetX = click.comp_4798() - (double)this.bounds.x();
         this.offsetY = click.comp_4799() - (double)this.bounds.y();
         return true;
      } else {
         return false;
      }
   }

   public boolean method_25403(class_11909 click, double dx, double dy) {
      if (this.dragging && this.bounds != null && click.method_74245() == 0) {
         HudLayout.move(this.config, click.comp_4798() - this.offsetX, click.comp_4799() - this.offsetY, this.field_22789, this.field_22790, this.bounds);
         return true;
      } else {
         return super.method_25403(click, dx, dy);
      }
   }

   public boolean method_25406(class_11909 click) {
      if (this.dragging && click.method_74245() == 0) {
         this.dragging = false;
         this.save();
         return true;
      } else {
         return super.method_25406(click);
      }
   }

   public boolean method_25404(class_11908 key) {
      if (this.method_25399() instanceof WoodlandSlider) {
         return super.method_25404(key);
      } else if (this.bounds != null && key.comp_4795() >= 262 && key.comp_4795() <= 265) {
         int step = (key.comp_4797() & 1) != 0 ? 10 : 1;
         int dx = key.comp_4795() == 262 ? step : (key.comp_4795() == 263 ? -step : 0);
         int dy = key.comp_4795() == 264 ? step : (key.comp_4795() == 265 ? -step : 0);
         HudLayout.move(this.config, (double)(this.bounds.x() + dx), (double)(this.bounds.y() + dy), this.field_22789, this.field_22790, this.bounds);
         this.save();
         return true;
      } else {
         return super.method_25404(key);
      }
   }

   private void save() {
      AcornTrackerClient.INSTANCE.persistHud();
   }

   public void method_25419() {
      this.save();
      this.field_22787.method_1507(this.parent);
   }

   public void method_25432() {
      this.save();
   }

   public boolean method_25421() {
      return false;
   }
}
