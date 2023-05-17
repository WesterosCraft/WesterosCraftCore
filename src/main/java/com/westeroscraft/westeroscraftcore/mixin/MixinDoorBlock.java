package com.westeroscraft.westeroscraftcore.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BambooBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.westeroscraft.westeroscraftcore.WesterosCraftCore;

@Mixin(DoorBlock.class) 
public abstract class MixinDoorBlock
{	
	// This constructor is fake and never used
	protected MixinDoorBlock()
	{
	}

	@Inject(method = "use(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;", at = @At("RETURN"))	
	private void doUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitrslt, CallbackInfoReturnable<InteractionResult> ci) {
		// If no action, just return
		if (ci.getReturnValue() == InteractionResult.PASS) return;
		// Is this a door we should be planning to close
		if (WesterosCraftCore.isAutoCloseDoor(state.getBlock())) {
			WesterosCraftCore.debugLog("Is auto close door");
			BlockState newstate = world.getBlockState(pos);	// Get new state
			if (newstate.getValue(DoorBlock.OPEN)) {	// If it is opened, we should plan to close it with tick hander
				WesterosCraftCore.debugLog("And it is now open");
				WesterosCraftCore.setPendingDoorClose(world, pos);
			}
		}
	}
}
