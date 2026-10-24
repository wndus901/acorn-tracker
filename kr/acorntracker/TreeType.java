package kr.acorntracker;

public enum TreeType {
   OAK("oak", "참나무"),
   SPRUCE("spruce", "가문비나무"),
   BIRCH("birch", "자작나무"),
   JUNGLE("jungle", "정글 나무"),
   ACACIA("acacia", "아카시아나무"),
   DARK_OAK("dark_oak", "짙은 참나무"),
   MANGROVE("mangrove", "맹그로브나무"),
   CHERRY("cherry", "벚나무"),
   PALE_OAK("pale_oak", "창백한 참나무"),
   CRIMSON("crimson", "진홍빛 균"),
   WARPED("warped", "뒤틀린 균");

   public final String key;
   public final String label;
   public final String blockId;

   private TreeType(String key, String label) {
      this.key = key;
      this.label = label;
      this.blockId = "minecraft:" + key + (!key.equals("crimson") && !key.equals("warped") ? "_log" : "_stem");
   }

   public static TreeType fromBlockId(String id) {
      for(TreeType tree : values()) {
         if (tree.blockId.equals(id)) {
            return tree;
         }
      }

      return null;
   }

   // $FF: synthetic method
   private static TreeType[] $values() {
      return new TreeType[]{OAK, SPRUCE, BIRCH, JUNGLE, ACACIA, DARK_OAK, MANGROVE, CHERRY, PALE_OAK, CRIMSON, WARPED};
   }
}
