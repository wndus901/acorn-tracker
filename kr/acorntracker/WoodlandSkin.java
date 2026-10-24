package kr.acorntracker;

import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_332;

public final class WoodlandSkin {
   private static final class_2960 PANEL = class_2960.method_60655("acorn_tracker", "textures/gui/wood_panel.png");
   private static final class_2960 BUTTON = class_2960.method_60655("acorn_tracker", "textures/gui/wood_button.png");
   public static final int INK = -5699;
   public static final int MUTED = -1784166;

   private WoodlandSkin() {
   }

   public static int tint(String theme, int opacity) {
      int var10000;
      switch (theme == null ? "forest" : theme) {
         case "slate" -> var10000 = 13031398;
         case "amber" -> var10000 = 16770740;
         default -> var10000 = 16777215;
      }

      int rgb = var10000;
      return (int)Math.round((double)(255 * Math.clamp((long)opacity, 0, 100)) / (double)100.0F) << 24 | rgb;
   }

   public static void panel(class_332 ctx, int x, int y, int width, int height, int color) {
      nineSlice(ctx, PANEL, x, y, width, height, 1254, 1254, 0, 0, 1254, 1254, 320, 320, 30, 30, color);
   }

   public static void button(class_332 ctx, int x, int y, int width, int height, int color) {
      nineSlice(ctx, BUTTON, x, y, width, height, 2172, 724, 30, 100, 2106, 512, 360, 120, 10, 5, color);
   }

   private static void nineSlice(class_332 ctx, class_2960 texture, int x, int y, int width, int height, int tw, int th, int ox, int oy, int sw, int sh, int sx, int sy, int dx, int dy, int color) {
      if (width > 0 && height > 0 && color >>> 24 != 0) {
         dx = Math.min(dx, width / 2);
         dy = Math.min(dy, height / 2);
         int[] srcX = new int[]{ox, ox + sx, ox + sw - sx, ox + sw};
         int[] srcY = new int[]{oy, oy + sy, oy + sh - sy, oy + sh};
         int[] dstX = new int[]{x, x + dx, x + width - dx, x + width};
         int[] dstY = new int[]{y, y + dy, y + height - dy, y + height};

         for(int row = 0; row < 3; ++row) {
            for(int col = 0; col < 3; ++col) {
               int w = dstX[col + 1] - dstX[col];
               int h = dstY[row + 1] - dstY[row];
               if (w > 0 && h > 0) {
                  ctx.method_25293(class_10799.field_56883, texture, dstX[col], dstY[row], (float)srcX[col], (float)srcY[row], w, h, srcX[col + 1] - srcX[col], srcY[row + 1] - srcY[row], tw, th, color);
               }
            }
         }

      }
   }
}
