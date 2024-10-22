package net.wensc.mitemod.mitebroadcasttool.trans;

import net.minecraft.MobEffectList;
import net.wensc.mitemod.mitebroadcasttool.potion.PotionCannotHarm;
import net.wensc.mitemod.mitebroadcasttool.util.StaticKeeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(MobEffectList.class)
public class PotionTrans {
    private static MobEffectList cannotHarm;
    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void staticInject(CallbackInfo callbackInfo) {
        cannotHarm = (new PotionCannotHarm(31, true, 9062085)).setPotionName("potion.cannotHarm");
    }
}
