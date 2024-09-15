package com.westeroscraft.westeroscraftcore.mixin;

import com.westeroscraft.westeroscraftcore.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FarmBlock.class)
public abstract class MixinFarmBlock
{
    @Inject(method = "turnToDirt", at = @At("HEAD"), cancellable=true)
    private static void doTurnToDirt(Entity pEntity, BlockState pState, Level pLevel, BlockPos pPos, CallbackInfo ci) {
        if (Config.disableFarmStomping) {
            ci.cancel();
        }
    }
}
