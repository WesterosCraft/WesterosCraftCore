package com.westeroscraft.westeroscraftcore.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.westeroscraft.westeroscraftcore.WesterosCraftCore;

@Mixin(IceBlock.class) 
public abstract class MixinIceBlock
{	
	// This constructor is fake and never used
	protected MixinIceBlock()
	{
	}

	@Inject(method = "melt(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V", at = @At("HEAD"), cancellable=true)	
	private void doMelt(BlockState bs, Level lvl, BlockPos pos, CallbackInfo ci) {
		if (WesterosCraftCore.Config.disableIceMelt.get()) {
			WesterosCraftCore.debugLog("Cancelled ice melt");
			ci.cancel();
		}
	}

}
