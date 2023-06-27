package com.westeroscraft.westeroscraftcore.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.westeroscraft.westeroscraftcore.WesterosCraftCore;

@Mixin(SnowLayerBlock.class) 
public abstract class MixinSnowLayerBlock
{	
	// This constructor is fake and never used
	protected MixinSnowLayerBlock()
	{
	}

	@Inject(method = "randomTick(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Ljava/util/Random;)V", at = @At("HEAD"), cancellable=true)	
    private void doRandomTick(BlockState bs, ServerLevel lvl, BlockPos pos, Random rnd, CallbackInfo ci) {
		if (WesterosCraftCore.Config.disableSnowMelt.get()) {
			WesterosCraftCore.debugLog("Cancelled snow layer melt");
			ci.cancel();
		}
	}
	@Inject(method = "canSurvive(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z", at = @At("HEAD"), cancellable=true)	
    private void doCanSurvive(BlockState bs, LevelReader lvl, BlockPos pos, CallbackInfoReturnable<Boolean> ci) {
		if (WesterosCraftCore.Config.snowLayerSurviveAny.get()) {
			WesterosCraftCore.debugLog("Allow snow layer survive");
			ci.setReturnValue(true);
		}
	}

}
