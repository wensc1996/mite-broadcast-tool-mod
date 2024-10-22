package net.wensc.mitemod.mitebroadcasttool.trans;

import net.minecraft.Damage;
import net.minecraft.EntityDamageResult;
import net.minecraft.EntityLiving;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityLiving.class)
public class EntityLivingTrans {
    @Inject(method = "attackEntityFrom", at = @At("HEAD"), cancellable = true)
    public void attackEntityFromCannotHarm(Damage damage, CallbackInfoReturnable<EntityDamageResult> callback) {
        if(damage.getSource().getResponsibleEntity() instanceof EntityLiving){
            EntityLiving entityLivingBase = (EntityLiving)damage.getSource().getResponsibleEntity();
            if(entityLivingBase.getActivePotionEffect(31) != null){
                callback.setReturnValue(null);
                callback.cancel();
            }
        }

    }
}
