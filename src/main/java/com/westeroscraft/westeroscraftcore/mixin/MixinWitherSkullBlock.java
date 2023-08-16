package com.westeroscraft.westeroscraftcore.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WitherSkullBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.westeroscraft.westeroscraftcore.WesterosCraftCore;

@Mixin(WitherSkullBlock.class) 
public abstract class MixinWitherSkullBlock
{	
	// This constructor is fake and never used
	protected MixinWitherSkullBlock()
	{
	}

	@Inject(method = "checkSpawn(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/SkullBlockEntity;)V", at = @At("HEAD"), cancellable=true)	
    private static void doCheckSpawn(Level p_58256_, BlockPos p_58257_, SkullBlockEntity p_58258_, CallbackInfo ci) {
		if (WesterosCraftCore.Config.blockWitherSpawn.get()) {
			WesterosCraftCore.debugLog("Block wither spawn");
			ci.cancel();
		}
	}
}
