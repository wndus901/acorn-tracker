package kr.acorntracker;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class TrackerConfig {
   public int waitTicks = 10;
   public int confirmationTicks = 100;
   public int rollbackTicks = 200;
   public boolean maxOneConfirmed = false;
   public boolean hud = true;
   public double hudX = (double)0.0F;
   public double hudY = (double)0.0F;
   public double hudScale = (double)1.0F;
   public int hudOpacity = 85;
   public String hudTheme = "forest";
   public String hudFont = "default";
   public Map<String, Boolean> hudFields = new LinkedHashMap();
   public Map<String, Boolean> measuredTrees = new LinkedHashMap();
   public JsonElement acornStack;

   public void useBundledAcornIfMissing() {
      if (this.acornStack == null || this.acornStack.isJsonNull()) {
         try {
            InputStream stream = TrackerConfig.class.getResourceAsStream("/assets/acorn_tracker/default_acorn.json");

            try {
               if (stream == null) {
                  throw new IOException("Bundled acorn definition is missing");
               }

               this.acornStack = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            } catch (Throwable var5) {
               if (stream != null) {
                  try {
                     stream.close();
                  } catch (Throwable var4) {
                     var5.addSuppressed(var4);
                  }
               }

               throw var5;
            }

            if (stream != null) {
               stream.close();
            }

         } catch (IOException e) {
            throw new IllegalStateException("Cannot read bundled acorn definition", e);
         }
      }
   }

   public boolean measures(TreeType tree) {
      if (tree == null) {
         return false;
      } else {
         Boolean enabled = this.measuredTrees == null ? null : (Boolean)this.measuredTrees.get(tree.key);
         return enabled == null ? tree == TreeType.BIRCH : enabled;
      }
   }

   public void setMeasured(TreeType tree, boolean enabled) {
      if (this.measuredTrees == null) {
         this.measuredTrees = new LinkedHashMap();
      }

      this.measuredTrees.put(tree.key, enabled);
   }

   public boolean measuresAnyTree() {
      for(TreeType tree : TreeType.values()) {
         if (this.measures(tree)) {
            return true;
         }
      }

      return false;
   }

   public boolean visible(HudField field) {
      return this.hudFields == null || !Boolean.FALSE.equals(this.hudFields.get(field.key));
   }

   public void setVisible(HudField field, boolean visible) {
      if (this.hudFields == null) {
         this.hudFields = new LinkedHashMap();
      }

      this.hudFields.put(field.key, visible);
   }

   public static enum HudField {
      TITLE("title", "제목"),
      LOGS("logs", "원목"),
      ACORNS("acorns", "도토리"),
      VALUE("value", "도토리 총가격"),
      FORTUNE("fortune", "행운"),
      AVERAGE("average", "평균 획득량"),
      ESTIMATED("estimated", "추정 구간 포함"),
      WARNINGS("warnings", "최대 1개 조건 경고");

      public final String key;
      public final String label;

      private HudField(String key, String label) {
         this.key = key;
         this.label = label;
      }

      public String koreanCommand() {
         String var10000;
         switch (this.ordinal()) {
            case 0 -> var10000 = "제목";
            case 1 -> var10000 = "원목";
            case 2 -> var10000 = "도토리";
            case 3 -> var10000 = "총가격";
            case 4 -> var10000 = "행운";
            case 5 -> var10000 = "평균";
            case 6 -> var10000 = "추정구간";
            case 7 -> var10000 = "경고";
            default -> throw new MatchException((String)null, (Throwable)null);
         }

         return var10000;
      }

      // $FF: synthetic method
      private static HudField[] $values() {
         return new HudField[]{TITLE, LOGS, ACORNS, VALUE, FORTUNE, AVERAGE, ESTIMATED, WARNINGS};
      }
   }
}
