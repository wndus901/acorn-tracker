package kr.acorntracker;

import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_3675;
import net.minecraft.class_437;
import net.minecraft.class_3675.class_307;

public final class SettingsKeyScreen extends class_437 {
   private final class_437 parent;
   private int left;
   private int top;
   private int panelWidth;

   public SettingsKeyScreen(class_437 parent) {
      super(class_2561.method_43470("설정 키 변경"));
      this.parent = parent;
   }

   protected void method_25426() {
      this.panelWidth = Math.min(360, this.field_22789 - 16);
      this.left = (this.field_22789 - this.panelWidth) / 2;
      this.top = (this.field_22790 - 170) / 2;
      int buttonWidth = (this.panelWidth - 56) / 3;
      int x = this.left + 22;
      this.method_37063(new AcornButton(x, this.top + 133, buttonWidth, 20, "기본값 O", (button) -> this.assign(class_307.field_1668.method_1447(79))));
      this.method_37063(new AcornButton(x + buttonWidth + 6, this.top + 133, buttonWidth, 20, "할당 해제", (button) -> this.assign(class_3675.field_16237)));
      this.method_37063(new AcornButton(x + (buttonWidth + 6) * 2, this.top + 133, buttonWidth, 20, "취소", (button) -> this.method_25419()));
   }

   public void method_25394(class_332 ctx, int mouseX, int mouseY, float delta) {
      ctx.method_25294(0, 0, this.field_22789, this.field_22790, -535287002);
      WoodlandSkin.panel(ctx, this.left, this.top, this.panelWidth, 170, WoodlandSkin.tint(AcornTrackerClient.INSTANCE.config().hudTheme, 100));
      ctx.method_25300(this.field_22793, "설정 키 변경", this.field_22789 / 2, this.top + 19, -5699);
      ctx.method_25300(this.field_22793, "현재 키: " + AcornTrackerClient.INSTANCE.settingsKeyName(), this.field_22789 / 2, this.top + 45, -1784166);
      ctx.method_25300(this.field_22793, "변경할 키를 누르세요", this.field_22789 / 2, this.top + 70, -5699);
      ctx.method_25300(this.field_22793, "마우스 버튼도 지정할 수 있어요", this.field_22789 / 2, this.top + 87, -1784166);
      ctx.method_25300(this.field_22793, "Esc: 취소 · 다른 조작과 겹치지 않는 키를 권장해요", this.field_22789 / 2, this.top + 108, -1784166);
      super.method_25394(ctx, mouseX, mouseY, delta);
   }

   public boolean method_25404(class_11908 key) {
      if (key.comp_4795() == 256) {
         this.method_25419();
      } else {
         this.assign(class_3675.method_15985(key));
      }

      return true;
   }

   public boolean method_25402(class_11909 click, boolean doubled) {
      if (super.method_25402(click, doubled)) {
         return true;
      } else if (click.method_74245() < 0) {
         return false;
      } else {
         this.assign(class_307.field_1672.method_1447(click.method_74245()));
         return true;
      }
   }

   private void assign(class_3675.class_306 key) {
      AcornTrackerClient.INSTANCE.setSettingsKey(key);
      this.method_25419();
   }

   public void method_25419() {
      this.field_22787.method_1507(this.parent);
   }

   public boolean method_25421() {
      return false;
   }
}
