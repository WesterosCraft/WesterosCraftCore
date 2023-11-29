package com.westeroscraft.westeroscraftcore.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.westeroscraft.westeroscraftcore.WesterosCraftCore;

@Mixin(FallingBlock.class) 
public abstract class MixinFallingBlock
{	
	// This constructor is fake and never used
	protected MixinFallingBlock()
	{
	}

	@Inject(method = "isFree(Lnet/minecraft/world/level/block/state/BlockState;)Z", at = @At("HEAD"), cancellable=true)	
	private static void doIsFree(BlockState p_53242_, CallbackInfoReturnable<Boolean> ci) {
		if (WesterosCraftCore.Config.disableFallingBlocks.get()) {
			ci.setReturnValue(Boolean.FALSE);
		}
	}
}
