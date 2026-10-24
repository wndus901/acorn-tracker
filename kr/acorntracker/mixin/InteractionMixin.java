package kr.acorntracker.mixin;

import kr.acorntracker.AcornTrackerClient;
import net.minecraft.class_1657;
import net.minecraft.class_1713;
import net.minecraft.class_2338;
import net.minecraft.class_636;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_636.class})
public abstract class InteractionMixin {
   @Inject(
      method = {"method_2899"},
      at = {@At("HEAD")}
   )
   private void beforeBreak(class_2338 pos, CallbackInfoReturnable<Boolean> cir) {
      AcornTrackerClient.INSTANCE.beforeBreak(pos);
   }

   @Inject(
      method = {"method_2899"},
      at = {@At("RETURN")}
   )
   private void afterBreak(class_2338 pos, CallbackInfoReturnable<Boolean> cir) {
      AcornTrackerClient.INSTANCE.breakResult(pos, (Boolean)cir.getReturnValue());
   }

   @Inject(
      method = {"method_2906"},
      at = {@At("HEAD")}
   )
   private void click(int syncId, int slotId, int button, class_1713 type, class_1657 player, CallbackInfo ci) {
      AcornTrackerClient.INSTANCE.inventoryInteraction();
   }
}
