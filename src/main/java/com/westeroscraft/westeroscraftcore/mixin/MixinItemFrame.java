package com.westeroscraft.westeroscraftcore.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.westeroscraft.westeroscraftcore.WesterosCraftCore;

@Mixin(ItemFrame.class) 
public abstract class MixinItemFrame
{	
	// This constructor is fake and never used
	protected MixinItemFrame()
	{
	}

	@Inject(method = "interact(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;", at = @At("HEAD"), cancellable=true)	
	private void doInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> ci) {
		if ((!player.isCreative()) && WesterosCraftCore.Config.blockItemFrameChanges.get()) {
			WesterosCraftCore.debugLog("Blocked item frame interaction");
			ci.setReturnValue(InteractionResult.CONSUME);
		}
	}
	@Inject(method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", at = @At("HEAD"), cancellable=true)	
	private void doHurt(DamageSource src, float damage, CallbackInfoReturnable<Boolean> ci) {
		if ((!src.isCreativePlayer()) && WesterosCraftCore.Config.blockItemFrameChanges.get()) {
			WesterosCraftCore.debugLog("Blocked item frame damage");
			ci.setReturnValue(Boolean.FALSE);
		}		
	}
	
	@Inject(method = "dropItem(Lnet/minecraft/world/entity/Entity;Z)V", at = @At("HEAD"), cancellable=true)	
	private void doDropItem(@Nullable Entity entity, boolean p_31804_, CallbackInfo ci) {
		Player p = (entity instanceof Player) ? (Player) entity : null;
		if (((p == null) || (!p.isCreative())) && WesterosCraftCore.Config.blockItemFrameChanges.get()) {
			ci.cancel();
		}
	}
	
	@Inject(method = "survives()Z", at = @At("HEAD"), cancellable=true)	
	public void doSurvives(CallbackInfoReturnable<Boolean> ci) {
		if (WesterosCraftCore.Config.blockItemFrameChanges.get()) {
			ci.setReturnValue(Boolean.TRUE);
		}
	}

}
