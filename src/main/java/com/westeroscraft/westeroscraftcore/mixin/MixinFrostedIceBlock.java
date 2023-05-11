package com.westeroscraft.westeroscraftcore.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FrostedIceBlock;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.westeroscraft.westeroscraftcore.WesterosCraftCore;

@Mixin(FrostedIceBlock.class) 
public abstract class MixinFrostedIceBlock
{	
	// This constructor is fake and never used
	protected MixinFrostedIceBlock()
	{
	}

	@Inject(method = "randomTick(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Ljava/util/Random;)V", at = @At("HEAD"), cancellable=true)	
    private void doRandomTick(BlockState bs, ServerLevel lvl, BlockPos pos, Random rnd, CallbackInfo ci) {
		if (WesterosCraftCore.Config.disableIceMelt.get()) {
			WesterosCraftCore.log.info("Cancelled frosted ice melt random tick");
			ci.cancel();
		}
	}	

	@Inject(method = "tick(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Ljava/util/Random;)V", at = @At("HEAD"), cancellable=true)	
    private void doTick(BlockState bs, ServerLevel lvl, BlockPos pos, Random rnd, CallbackInfo ci) {
		if (WesterosCraftCore.Config.disableIceMelt.get()) {
			WesterosCraftCore.log.info("Cancelled frosted ice melt tick");
			ci.cancel();
		}
	}	

	@Inject(method = "slightlyMelt(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z", at = @At("HEAD"), cancellable=true)	
	private void doSlightlyMelt(BlockState bs, Level lvl, BlockPos pos, CallbackInfoReturnable<Boolean> ci) {
		if (WesterosCraftCore.Config.disableIceMelt.get()) {
			WesterosCraftCore.log.info("Cancelled frosted ice melt");
			ci.setReturnValue(Boolean.FALSE);
		}
	}

}
