package kr.acorntracker.mixin;

import kr.acorntracker.AcornTrackerClient;
import net.minecraft.class_2649;
import net.minecraft.class_2653;
import net.minecraft.class_634;
import net.minecraft.class_9834;
import net.minecraft.class_9835;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_634.class})
public abstract class NetworkMixin {
   @Inject(
      method = {"method_11153"},
      at = {@At("TAIL")}
   )
   private void inventory(class_2649 packet, CallbackInfo ci) {
      AcornTrackerClient.INSTANCE.onInventory(packet);
   }

   @Inject(
      method = {"method_11109"},
      at = {@At("TAIL")}
   )
   private void slot(class_2653 packet, CallbackInfo ci) {
      AcornTrackerClient.INSTANCE.onSlot(packet);
   }

   @Inject(
      method = {"method_61188"},
      at = {@At("TAIL")}
   )
   private void playerSlot(class_9835 packet, CallbackInfo ci) {
      AcornTrackerClient.INSTANCE.onPlayerSlot(packet);
   }

   @Inject(
      method = {"method_61187"},
      at = {@At("TAIL")}
   )
   private void cursor(class_9834 packet, CallbackInfo ci) {
      AcornTrackerClient.INSTANCE.onCursor(packet);
   }
}
