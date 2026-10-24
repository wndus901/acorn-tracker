package kr.acorntracker;

import net.minecraft.class_11719;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_5250;
import net.minecraft.class_2960;

public final class HudFonts {
   private static final Choice[] CHOICES = new Choice[]{
      new Choice("default", "마인크래프트 기본"),
      new Choice("galmuri_11_regular", "갈무리 11 Regular"),
      new Choice("nanum_square", "나눔스퀘어"),
      new Choice("jua", "주아체")
   };

   private HudFonts() {
   }

   public static String label(String id) {
      return choice(id).label;
   }

   public static boolean isDefault(String id) {
      return choice(id) == CHOICES[0];
   }

   public static String next(String id) {
      Choice current = choice(id);

      for (int i = 0; i < CHOICES.length; ++i) {
         if (CHOICES[i] == current) {
            return CHOICES[(i + 1) % CHOICES.length].id;
         }
      }

      return CHOICES[0].id;
   }

   public static class_2561 text(String value, String id) {
      class_5250 text = class_2561.method_43470(value);
      Choice choice = choice(id);
      if (choice == CHOICES[0]) {
         return text;
      }

      class_2960 fontId = class_2960.method_60655("acorn_tracker", choice.id);
      return text.method_10862(class_2583.field_24360.method_27704(new class_11719.class_11721(fontId)));
   }

   private static Choice choice(String id) {
      if (id != null) {
         for (Choice choice : CHOICES) {
            if (choice.id.equals(id)) {
               return choice;
            }
         }
      }

      return CHOICES[0];
   }

   private static record Choice(String id, String label) {
   }
}
