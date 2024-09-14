package com.westeroscraft.westeroscraftcore.mixin;

import com.westeroscraft.westeroscraftcore.Config;
import com.westeroscraft.westeroscraftcore.WesterosCraftCore;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BambooStalkBlock.class)
public abstract class MixinBambooBlock
{
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void onRandomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        // Check if bamboo spreading should be disabled
        if (Config.disableBambooFadeSpread) {
            // Cancel the method execution, preventing bamboo from spreading
            ci.cancel();
        }
    }

    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable=true)
    private void doCanSurvive(BlockState bs, LevelReader lvl, BlockPos pos, CallbackInfoReturnable<Boolean> ci) {
            if (Config.bambooSurviveAny) {
                WesterosCraftCore.debugLog("Allow bamboo survive");
                ci.setReturnValue(true);
            }
        }

}
