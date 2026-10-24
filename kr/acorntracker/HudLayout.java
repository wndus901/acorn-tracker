package kr.acorntracker;

public final class HudLayout {
   private HudLayout() {
   }

   public static Bounds bounds(int screenWidth, int screenHeight, int panelWidth, int panelHeight, TrackerConfig config) {
      double scale = Double.isFinite(config.hudScale) ? Math.clamp(config.hudScale, (double)0.5F, (double)2.0F) : (double)1.0F;
      scale = Math.min(scale, Math.min((double)Math.max(1, screenWidth - 16) / (double)panelWidth, (double)Math.max(1, screenHeight - 16) / (double)panelHeight));
      int width = (int)Math.ceil((double)panelWidth * scale);
      int height = (int)Math.ceil((double)panelHeight * scale);
      int x = 8 + (int)Math.round(unit(config.hudX) * (double)Math.max(0, screenWidth - 16 - width));
      int y = 8 + (int)Math.round(unit(config.hudY) * (double)Math.max(0, screenHeight - 16 - height));
      return new Bounds(x, y, width, height, (float)scale);
   }

   public static void move(TrackerConfig config, double x, double y, int screenWidth, int screenHeight, Bounds bounds) {
      config.hudX = unit((x - (double)8.0F) / (double)Math.max(1, screenWidth - 16 - bounds.width));
      config.hudY = unit((y - (double)8.0F) / (double)Math.max(1, screenHeight - 16 - bounds.height));
   }

   private static double unit(double value) {
      return Double.isFinite(value) ? Math.clamp(value, (double)0.0F, (double)1.0F) : (double)0.0F;
   }

   public static record Bounds(int x, int y, int width, int height, float scale) {
      public boolean contains(double px, double py) {
         return px >= (double)this.x && py >= (double)this.y && px < (double)(this.x + this.width) && py < (double)(this.y + this.height);
      }
   }
}
