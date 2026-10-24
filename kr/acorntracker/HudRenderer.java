package kr.acorntracker;

import java.util.List;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import org.joml.Matrix3x2fStack;

public final class HudRenderer {
   public static Palette palette(String theme) {
      Palette var10000;
      switch (theme == null ? "forest" : theme) {
         case "amber" -> var10000 = new Palette(2169879, 3286811, 15316840);
         case "slate" -> var10000 = new Palette(1449257, 2108733, 9423080);
         default -> var10000 = new Palette(1319710, 1979435, 12047756);
      }

      return var10000;
   }

   private static int rowHeight(Row row) {
      byte var10000;
      switch (row.kind.ordinal()) {
         case 0 -> var10000 = 25;
         case 6 -> var10000 = 20;
         case 7 -> var10000 = 17;
         default -> var10000 = 15;
      }

      return var10000;
   }

   public static HudLayout.Bounds render(class_332 ctx, TrackerConfig config, List<Row> rows, boolean preview) {
      class_327 font = class_310.method_1551().field_1772;
      int width = 128;

      for(Row row : rows) {
         int valueWidth = textWidth(font, config, row.value);
         int valueGap = 0;
         if (row.kind == HudRenderer.Kind.VALUE) {
            valueWidth = Math.max(valueWidth, textWidth(font, config, "10,000,000원"));
            valueGap = 16;
         }

         int contentWidth = textWidth(font, config, row.label) + valueWidth + 40 + valueGap;
         width = Math.max(width, contentWidth);
      }

      int height = 34 + rows.stream().mapToInt(HudRenderer::rowHeight).sum();
      if (rows.isEmpty()) {
         width = Math.max(width, textWidth(font, config, "표시 항목이 모두 꺼져 있습니다") + 40);
      }

      HudLayout.Bounds bounds = HudLayout.bounds(ctx.method_51421(), ctx.method_51443(), width, height, config);
      if (rows.isEmpty() && !preview) {
         return bounds;
      } else {
         Palette p = palette(config.hudTheme);
         Matrix3x2fStack matrices = ctx.method_51448();
         matrices.pushMatrix();
         matrices.translate((float)bounds.x(), (float)bounds.y());
         matrices.scale(bounds.scale(), bounds.scale());

         try {
            WoodlandSkin.panel(ctx, 0, 0, width, height, WoodlandSkin.tint(config.hudTheme, config.hudOpacity));
            int y = 20;

            for(Row row : rows) {
               int h = rowHeight(row);
               switch (row.kind.ordinal()) {
                  case 0:
                     WoodlandSkin.button(ctx, 30, y - 5, width - 60, 23, WoodlandSkin.tint(config.hudTheme, config.hudOpacity));
                     drawCentered(ctx, font, config, row.label, width / 2, y + 2, -5699);
                     break;
                  case 6:
                     draw(ctx, font, config, row.label, 20, y + 7, -1784166);
                     break;
                  default:
                     int label = row.kind == HudRenderer.Kind.WARNING ? -16003 : (row.kind == HudRenderer.Kind.DETAIL ? -1784166 : -5699);
                     int var10000;
                     switch (row.kind.ordinal()) {
                        case 2:
                        case 3:
                           var10000 = -8824;
                           break;
                        case 4:
                           var10000 = -2756705;
                           break;
                        case 5:
                           var10000 = -1784166;
                           break;
                        case 6:
                        default:
                           var10000 = -1;
                           break;
                        case 7:
                           var10000 = -21931;
                     }

                     int value = var10000;
                     draw(ctx, font, config, row.label, 20, y + 1, label);
                     draw(ctx, font, config, row.value, width - 20 - textWidth(font, config, row.value), y + 1, value);
               }

               y += h;
            }

            if (rows.isEmpty()) {
               drawCentered(ctx, font, config, "표시 항목이 모두 꺼져 있습니다", width / 2, 20, -5699);
            }

            if (preview) {
               ctx.method_73198(-2, -2, width + 4, height + 4, -1442840576 | p.accent);

               for(int dx : new int[]{-3, width - 1}) {
                  for(int dy : new int[]{-3, height - 1}) {
                     ctx.method_25294(dx, dy, dx + 4, dy + 4, -16777216 | p.accent);
                  }
               }
            }
         } finally {
            matrices.popMatrix();
         }

         return bounds;
      }
   }

   private static int textWidth(class_327 font, TrackerConfig config, String value) {
      return font.method_27525(HudFonts.text(value, config.hudFont));
   }

   private static void draw(class_332 ctx, class_327 font, TrackerConfig config, String value, int x, int y, int color) {
      ctx.method_51433(font, HudFonts.text(value, config.hudFont), x, y, color, HudFonts.isDefault(config.hudFont));
   }

   private static void drawCentered(class_332 ctx, class_327 font, TrackerConfig config, String value, int centerX, int y, int color) {
      class_2561 text = HudFonts.text(value, config.hudFont);
      ctx.method_51433(font, text, centerX - font.method_27525(text) / 2, y, color, HudFonts.isDefault(config.hudFont));
   }

   public static void rounded(class_332 ctx, int x, int y, int width, int height, int color) {
      if (width >= 6 && height >= 6) {
         ctx.method_25294(x + 3, y, x + width - 3, y + height, color);
         ctx.method_25294(x + 1, y + 1, x + 3, y + height - 1, color);
         ctx.method_25294(x + width - 3, y + 1, x + width - 1, y + height - 1, color);
         ctx.method_25294(x, y + 3, x + 1, y + height - 3, color);
         ctx.method_25294(x + width - 1, y + 3, x + width, y + height - 3, color);
      } else {
         ctx.method_25294(x, y, x + width, y + height, color);
      }
   }

   public static enum Kind {
      TITLE,
      NORMAL,
      ACCENT,
      VALUE,
      AVERAGE,
      DETAIL,
      STATUS,
      WARNING;

      // $FF: synthetic method
      private static Kind[] $values() {
         return new Kind[]{TITLE, NORMAL, ACCENT, VALUE, AVERAGE, DETAIL, STATUS, WARNING};
      }
   }

   public static record Row(String label, String value, Kind kind) {
   }

   public static record Palette(int background, int surface, int accent) {
   }
}
