package kr.acorntracker;

import java.util.Locale;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;

public final class HudSettingsScreen extends class_437 {
   private final class_437 parent;
   private final TrackerConfig config;
   private int tab;
   private int page;
   private int treePage;
   private int left;
   private int top;
   private int panelWidth;
   private int panelHeight;

   public HudSettingsScreen(class_437 parent) {
      this(parent, 0);
   }

   public HudSettingsScreen(class_437 parent, int tab) {
      super(class_2561.method_43470("도토리 HUD 설정"));
      this.config = AcornTrackerClient.INSTANCE.config();
      this.parent = parent;
      this.tab = tab;
   }

   protected void method_25426() {
      this.panelWidth = Math.min(420, this.field_22789 - 16);
      this.left = (this.field_22789 - this.panelWidth) / 2;
      this.panelHeight = Math.min(244, this.field_22790 - 8);
      this.top = (this.field_22790 - this.panelHeight) / 2;
      int bottom = this.top + this.panelHeight;
      int x = this.left + 23;
      int w = this.panelWidth - 46;
      int half = (w - 8) / 2;
      int var10004 = this.top + 43;
      String var10007 = this.config.hud ? "켜짐" : "꺼짐";
      this.method_37063((new AcornButton(x, var10004, half, 18, "HUD 표시  " + var10007, (button) -> {
         this.config.hud = !this.config.hud;
         this.refresh();
      })).selected(this.config.hud));
      int var10003 = x + half + 8;
      var10004 = this.top + 43;
      var10007 = AcornTrackerClient.INSTANCE.settingsKeyName();
      this.method_37063(new AcornButton(var10003, var10004, half, 18, "설정 키 변경: " + var10007, (button) -> AcornTrackerClient.INSTANCE.openKeySettings(this)));
      int third = (w - 16) / 3;
      this.method_37063((new AcornButton(x, this.top + 65, third, 18, "표시 항목", (button) -> {
         this.tab = 0;
         this.method_41843();
      })).selected(this.tab == 0));
      this.method_37063((new AcornButton(x + third + 8, this.top + 65, third, 18, "스타일", (button) -> {
         this.tab = 1;
         this.method_41843();
      })).selected(this.tab == 1));
      this.method_37063((new AcornButton(x + (third + 8) * 2, this.top + 65, w - (third + 8) * 2, 18, "측정 나무", (button) -> {
         this.tab = 2;
         this.method_41843();
      })).selected(this.tab == 2));
      if (this.tab == 0) {
         TrackerConfig.HudField[] fields = TrackerConfig.HudField.values();
         int pageCount = (fields.length + 7) / 8;
         this.page = Math.clamp((long)this.page, 0, pageCount - 1);

         for(int i = this.page * 8; i < Math.min(fields.length, (this.page + 1) * 8); ++i) {
            TrackerConfig.HudField field = fields[i];
            int slot = i % 8;
            this.method_37063((new AcornButton(x + slot % 2 * (half + 8), this.top + 91 + slot / 2 * 21, half, 18, field.label + "  " + (this.config.visible(field) ? "켜짐" : "꺼짐"), (button) -> {
               this.config.setVisible(field, !this.config.visible(field));
               this.refresh();
            })).selected(this.config.visible(field)));
         }

         int y = bottom - 49;
         ((AcornButton)this.method_37063(new AcornButton(x, y, 30, 18, "<", (button) -> {
            --this.page;
            this.method_41843();
         }))).field_22763 = this.page > 0;
         ((AcornButton)this.method_37063(new AcornButton(x + 34, y, 30, 18, ">", (button) -> {
            ++this.page;
            this.method_41843();
         }))).field_22763 = this.page + 1 < pageCount;
         this.method_37063(new AcornButton(x + w - 152, y, 72, 18, "모두 켜기", (button) -> this.setAll(true)));
         this.method_37063(new AcornButton(x + w - 76, y, 76, 18, "모두 끄기", (button) -> this.setAll(false)));
      } else if (this.tab == 2) {
         TreeType[] trees = TreeType.values();

         for(int i = this.treePage * 8; i < Math.min(trees.length, (this.treePage + 1) * 8); ++i) {
            TreeType tree = trees[i];
            int slot = i % 8;
            this.method_37063((new AcornButton(x + slot % 2 * (half + 8), this.top + 91 + slot / 2 * 21, half, 18, tree.label + "  " + (this.config.measures(tree) ? "켜짐" : "꺼짐"), (button) -> {
               this.config.setMeasured(tree, !this.config.measures(tree));
               this.refresh();
            })).selected(this.config.measures(tree)));
         }

         int y = bottom - 49;
         ((AcornButton)this.method_37063(new AcornButton(x, y, 30, 18, "<", (button) -> {
            --this.treePage;
            this.method_41843();
         }))).field_22763 = this.treePage > 0;
         ((AcornButton)this.method_37063(new AcornButton(x + 34, y, 30, 18, ">", (button) -> {
            ++this.treePage;
            this.method_41843();
         }))).field_22763 = (this.treePage + 1) * 8 < trees.length;
         this.method_37063(new AcornButton(x + w - 152, y, 72, 18, "모두 켜기", (button) -> this.setAllTrees(true)));
         this.method_37063(new AcornButton(x + w - 76, y, 76, 18, "모두 끄기", (button) -> this.setAllTrees(false)));
      } else {
         String var10000;
         switch (this.config.hudTheme == null ? "forest" : this.config.hudTheme) {
            case "amber" -> var10000 = "앰버";
            case "slate" -> var10000 = "슬레이트";
            default -> var10000 = "숲";
         }

         String theme = var10000;
         this.method_37063(new AcornButton(x, this.top + 91, w, 20, "테마  ·  " + theme, (button) -> {
            TrackerConfig var10000 = this.config;
            String var10001;
            switch (this.config.hudTheme == null ? "forest" : this.config.hudTheme) {
               case "forest" -> var10001 = "amber";
               case "amber" -> var10001 = "slate";
               default -> var10001 = "forest";
            }

            var10000.hudTheme = var10001;
            this.refresh();
         }));
         this.method_37063(new WoodlandSlider(x, this.top + 117, w, 20, (Math.clamp(this.config.hudScale, (double)0.5F, (double)2.0F) - (double)0.5F) / (double)1.5F) {
            {
               this.method_25346();
            }

            protected void method_25346() {
               this.method_25355(class_2561.method_43470(String.format(Locale.ROOT, "HUD 크기  %.0f%%", ((double)0.5F + this.field_22753 * (double)1.5F) * (double)100.0F)));
            }

            protected void method_25344() {
               HudSettingsScreen.this.config.hudScale = (double)0.5F + this.field_22753 * (double)1.5F;
            }
         });
         this.method_37063(new WoodlandSlider(x, this.top + 143, w, 20, (double)Math.clamp((long)this.config.hudOpacity, 0, 100) / (double)100.0F) {
            {
               this.method_25346();
            }

            protected void method_25346() {
               this.method_25355(class_2561.method_43470("배경 불투명도  " + Math.round(this.field_22753 * (double)100.0F) + "%"));
            }

            protected void method_25344() {
               HudSettingsScreen.this.config.hudOpacity = (int)Math.round(this.field_22753 * (double)100.0F);
            }
         });
         this.method_37063(new AcornButton(x, this.top + 169, w, 20, "위치 · 스타일 기본값으로", (button) -> {
            this.config.hudX = this.config.hudY = (double)0.0F;
            this.config.hudScale = (double)1.0F;
            this.config.hudOpacity = 85;
            this.config.hudTheme = "forest";
            this.refresh();
         }));
      }

      this.method_37063(new AcornButton(x, bottom - 27, half, 18, "위치 이동 · 미리보기", (button) -> this.field_22787.method_1507(new HudPositionScreen(this))));
      this.method_37063((new AcornButton(x + half + 8, bottom - 27, half, 18, "완료", (button) -> this.method_25419())).selected(true));
   }

   private void refresh() {
      AcornTrackerClient.INSTANCE.persistHud();
      this.method_41843();
   }

   private void setAll(boolean visible) {
      for(TrackerConfig.HudField field : TrackerConfig.HudField.values()) {
         this.config.setVisible(field, visible);
      }

      this.refresh();
   }

   private void setAllTrees(boolean enabled) {
      for(TreeType tree : TreeType.values()) {
         this.config.setMeasured(tree, enabled);
      }

      this.refresh();
   }

   public void method_25394(class_332 ctx, int mouseX, int mouseY, float delta) {
      ctx.method_25294(0, 0, this.field_22789, this.field_22790, -535287002);
      WoodlandSkin.panel(ctx, this.left, this.top, this.panelWidth, this.panelHeight, WoodlandSkin.tint(this.config.hudTheme, 100));
      ctx.method_25300(this.field_22793, "도토리 설정", this.field_22789 / 2, this.top + 14, -5699);
      ctx.method_25300(this.field_22793, this.tab == 2 ? "여러 나무를 함께 선택할 수 있어요" : "표시할 정보와 모양을 골라 주세요", this.field_22789 / 2, this.top + 29, -1784166);
      super.method_25394(ctx, mouseX, mouseY, delta);
      if (this.tab == 0) {
         ctx.method_25300(this.field_22793, this.page + 1 + " / " + (TrackerConfig.HudField.values().length + 7) / 8, this.left + 106, this.top + this.panelHeight - 44, -5699);
      }

      if (this.tab == 2) {
         ctx.method_25300(this.field_22793, this.treePage + 1 + " / " + (TreeType.values().length + 7) / 8, this.left + 106, this.top + this.panelHeight - 44, -5699);
      }

   }

   public void method_25419() {
      AcornTrackerClient.INSTANCE.persistHud();
      this.field_22787.method_1507(this.parent);
   }

   public void method_25432() {
      AcornTrackerClient.INSTANCE.persistHud();
   }

   public boolean method_25421() {
      return false;
   }
}
