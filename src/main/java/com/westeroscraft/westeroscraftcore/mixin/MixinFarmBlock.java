package com.westeroscraft.westeroscraftcore.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.westeroscraft.westeroscraftcore.WesterosCraftCore;

@Mixin(FarmBlock.class) 
public abstract class MixinFarmBlock
{	
	// This constructor is fake and never used
	protected MixinFarmBlock()
	{
	}

	@Inject(method = "turnToDirt(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V", at = @At("HEAD"), cancellable=true)	
    private static void doTurnToDirt(BlockState p_53297_, Level p_53298_, BlockPos p_53299_, CallbackInfo ci) {
		if (WesterosCraftCore.Config.disableFarmStomping.get()) {
			ci.cancel();
		}
	}
}
