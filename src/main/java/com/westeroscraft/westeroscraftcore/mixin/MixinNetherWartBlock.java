package com.westeroscraft.westeroscraftcore.mixin;

import com.westeroscraft.westeroscraftcore.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.westeroscraft.westeroscraftcore.WesterosCraftCore;

@Mixin(NetherWartBlock.class)
public abstract class MixinNetherWartBlock
{
    // This constructor is fake and never used
    protected MixinNetherWartBlock()
    {
    }

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable=true)
    private void doRandomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom, CallbackInfo ci) {
        if (Config.disableNetherWartGrowFade) {
            WesterosCraftCore.debugLog("Cancelled netherwart fade/spread rand ticking");
            ci.cancel();
        }
    }

    @Inject(method = "isRandomlyTicking(Lnet/minecraft/world/level/block/state/BlockState;)Z", at = @At("HEAD"), cancellable=true)
    private void doIsRandomlyTicking(BlockState bs, CallbackInfoReturnable<Boolean> ci) {
        if (Config.disableNetherWartGrowFade) {
            WesterosCraftCore.debugLog("Cancelled netherwart fade/spread tick");
            ci.setReturnValue(false);
        }
    }
}
