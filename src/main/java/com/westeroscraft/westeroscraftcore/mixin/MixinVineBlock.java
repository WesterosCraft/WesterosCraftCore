package com.westeroscraft.westeroscraftcore.mixin;

import com.westeroscraft.westeroscraftcore.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.westeroscraft.westeroscraftcore.WesterosCraftCore;

@Mixin(VineBlock.class)
public abstract class MixinVineBlock
{
    // This constructor is fake and never used
    protected MixinVineBlock()
    {
    }

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable=true)
    private void doRandomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom, CallbackInfo ci) {
        if (Config.disableVineGrowFade) {
            WesterosCraftCore.debugLog("Cancelled vine grow/fade rand ticking");
            ci.cancel();
        }
    }

    @Inject(method = "canSurvive(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z", at = @At("HEAD"), cancellable=true)
    private void doCanSurvive(BlockState bs, LevelReader lvl, BlockPos pos, CallbackInfoReturnable<Boolean> ci) {
        if (Config.vineSurviveAny) {
            WesterosCraftCore.debugLog("Allow vine survive");
            ci.setReturnValue(true);
        }
    }
}
