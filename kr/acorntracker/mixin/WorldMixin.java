package kr.acorntracker.mixin;

import kr.acorntracker.AcornTrackerClient;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_638;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_638.class})
public abstract class WorldMixin {
   @Inject(
      method = {"method_41928"},
      at = {@At("HEAD")}
   )
   private void serverUpdate(class_2338 pos, class_2680 state, int flags, CallbackInfo ci) {
      AcornTrackerClient.INSTANCE.serverBlock(pos, state);
   }

   @Inject(
      method = {"method_41926"},
      at = {@At("HEAD")}
   )
   private void acknowledgedState(class_2338 pos, class_2680 state, class_243 playerPos, CallbackInfo ci) {
      AcornTrackerClient.INSTANCE.serverBlock(pos, state);
   }
}
