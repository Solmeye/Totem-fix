package net.solmey.totemfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DeathProtection;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Unique
    private ItemStack globalProtectionItem;

    @Unique
    private DeathProtection globalProtection;

    @Inject(
        method = "baseTick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;isDeadOrDying()Z"
        )
    )
    private void totemfix$baseTick(CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (!livingEntity.wasHurtRecently()) {
            this.globalProtectionItem = null;
            this.globalProtection = null;
        }
    }

    @Inject(
        at = @At("HEAD"),
        method = "checkTotemDeathProtection",
        cancellable = true
    )
    private void totemfix$checkTotemDeathProtectionHEAD(
        DamageSource killingDamage,
        CallbackInfoReturnable<Boolean> ci
    ) {
        if (killingDamage.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            ci.setReturnValue(false);
        } else if (this.globalProtection != null) {
            LivingEntity livingEntity = (LivingEntity) (Object) this;
            livingEntity.setHealth(1.0F);
            /*this.globalProtection.applyEffects(
                this.globalProtectionItem,
                livingEntity
            );*/
            ci.setReturnValue(true);
        }
    }

    @Inject(
        method = "checkTotemDeathProtection",
        at = @At("RETURN"),
        locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void totemfix$CheckTotemDeathProtectionRETURN(
        DamageSource killingDamage,
        CallbackInfoReturnable<Boolean> cir,
        ItemStack protectionItem,
        DeathProtection protection
    ) {
        if (
            (Boolean) cir.getReturnValue() &&
            this.globalProtectionItem == null &&
            this.globalProtection == null
        ) {
            this.globalProtectionItem = protectionItem.copy();
            this.globalProtection = protection;
        }
    }
}
