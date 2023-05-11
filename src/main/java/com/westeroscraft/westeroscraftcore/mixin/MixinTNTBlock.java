package com.westeroscraft.westeroscraftcore.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.westeroscraft.westeroscraftcore.WesterosCraftCore;

@Mixin(TntBlock.class) 
public abstract class MixinTNTBlock
{	
	// This constructor is fake and never used
	protected MixinTNTBlock()
	{
	}

	@Inject(method = "onCaughtFire(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lnet/minecraft/world/entity/LivingEntity;)V", at = @At("HEAD"), cancellable=true, remap=false)	
	private void doOnCaughtFire(BlockState state, Level lvl, BlockPos pos, Direction dir, LivingEntity ent, CallbackInfo ci) {
		if (WesterosCraftCore.Config.disableTNTExplode.get()) {
			WesterosCraftCore.debugLog("Cancelled tnt explode");
			ci.cancel();
		}
	}

}
