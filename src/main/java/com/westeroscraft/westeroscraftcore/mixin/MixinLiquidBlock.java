package com.westeroscraft.westeroscraftcore.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.westeroscraft.westeroscraftcore.WesterosCraftCore;

@Mixin(LiquidBlock.class) 
public abstract class MixinLiquidBlock
{	
	// This constructor is fake and never used
	protected MixinLiquidBlock()
	{
	}

	@Inject(method = "isRandomlyTicking", at = @At("HEAD"), cancellable=true)	
	private void doIsRandomlyTicking(BlockState state, CallbackInfoReturnable<Boolean> ci) {
		if (WesterosCraftCore.Config.disableFluidTicking.get()) {
			WesterosCraftCore.debugLog("Cancelled fluid tick");
			ci.setReturnValue(false);
		}		
	}
	@Inject(method = "randomTick", at = @At("HEAD"), cancellable=true)	   
	private void doRandomTick(BlockState state, ServerLevel level, BlockPos pos, Random rnd, CallbackInfo ci) {
		if (WesterosCraftCore.Config.disableFluidTicking.get()) {
			WesterosCraftCore.debugLog("Cancelled fluid tick");
			ci.cancel();
		}				
	}
	@Inject(method = "shouldSpreadLiquid", at = @At("HEAD"), cancellable=true)	
	private void doShouldSpreadLiquid(Level p_54697_, BlockPos p_54698_, BlockState p_54699_, CallbackInfoReturnable<Boolean> ci) {
		if (WesterosCraftCore.Config.disableFluidTicking.get()) {
			WesterosCraftCore.debugLog("Cancelled fluid tick");
			ci.setReturnValue(false);
		}		
	}
}
