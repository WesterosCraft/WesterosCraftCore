package com.westeroscraft.westeroscraftcore.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BambooBlock;
import net.minecraft.world.level.block.GrowingPlantBodyBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.westeroscraft.westeroscraftcore.WesterosCraftCore;

@Mixin(GrowingPlantHeadBlock.class) 
public abstract class MixinGrowingPlantHeadBlock
{	
	// This constructor is fake and never used
	protected MixinGrowingPlantHeadBlock()
	{
	}

	@Inject(method = "randomTick(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Ljava/util/Random;)V", at = @At("HEAD"), cancellable=true)	
    private void doRandomTick(BlockState bs, ServerLevel lvl, BlockPos pos, Random rnd, CallbackInfo ci) {
		if (WesterosCraftCore.Config.disablePlantGrowFade.get()) {
			WesterosCraftCore.log.info("Cancelled plant fade/spread rand ticking");
			ci.cancel();
		}
	}

	@Inject(method = "isRandomlyTicking(Lnet/minecraft/world/level/block/state/BlockState;)Z", at = @At("HEAD"), cancellable=true)	
    private void doIsRandomlyTicking(BlockState bs, CallbackInfoReturnable<Boolean> ci) {
		if (WesterosCraftCore.Config.disablePlantGrowFade.get()) {
			WesterosCraftCore.log.info("Cancelled plant fade/spread tick");
			ci.setReturnValue(false);
		}
	}
	
}
