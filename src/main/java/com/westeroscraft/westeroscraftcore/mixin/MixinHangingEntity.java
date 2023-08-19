package com.westeroscraft.westeroscraftcore.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.westeroscraft.westeroscraftcore.WesterosCraftCore;

@Mixin(HangingEntity.class) 
public abstract class MixinHangingEntity extends Entity
{	
	// This constructor is fake and never used
	protected MixinHangingEntity(EntityType<? extends HangingEntity> p_31703_, Level p_31704_) {
      super(p_31703_, p_31704_);
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand hand) {
		if ((!player.isCreative()) && WesterosCraftCore.Config.blockHangingItemChanges.get()) {
			WesterosCraftCore.debugLog("Blocked hanging entity interaction");
			return InteractionResult.CONSUME;
		}
		return super.interact(player, hand);
	}
	@Inject(method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", at = @At("HEAD"), cancellable=true)	
	private void doHurt(DamageSource src, float damage, CallbackInfoReturnable<Boolean> ci) {
		if ((!src.isCreativePlayer()) && WesterosCraftCore.Config.blockHangingItemChanges.get()) {
			WesterosCraftCore.debugLog("Blocked hanging entity damage");
			ci.setReturnValue(Boolean.FALSE);
		}		
	}
	
	//@Inject(method = "survives()Z", at = @At("HEAD"), cancellable=true)	
	//public void doSurvives(CallbackInfoReturnable<Boolean> ci) {
	//	if (WesterosCraftCore.Config.blockHangingItemChanges.get()) {
	//		WesterosCraftCore.debugLog("Blocked hanging entity survive");
	//		ci.setReturnValue(Boolean.TRUE);
	//	}
	//}

	@Inject(method = "skipAttackInteraction(Lnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"), cancellable=true)	
	public void doSkipAttackInteraction(Entity entity, CallbackInfoReturnable<Boolean> ci) {
		if (entity instanceof Player) {
			Player p = (Player) entity;
			if ((!p.isCreative()) && WesterosCraftCore.Config.blockHangingItemChanges.get()) {
				WesterosCraftCore.debugLog("Blocked hanging entity damage interaction");
				ci.cancel();
			}
		}
	}
}
