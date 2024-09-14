package com.westeroscraft.westeroscraftcore.mixin;

import com.westeroscraft.westeroscraftcore.Config;
import com.westeroscraft.westeroscraftcore.WesterosCraftCore;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LiquidBlock.class)
public abstract class MixinLiquidBlock {
    @Inject(method = "neighborChanged", at = @At("HEAD"), cancellable = true)
    private void onNeighborChanged(BlockState state, Level level, BlockPos pos, net.minecraft.world.level.block.Block block, BlockPos fromPos, boolean isMoving, CallbackInfo ci) {
        // Cancel the method execution, preventing liquid updates
        ci.cancel();
    }

    @Inject(method = "shouldSpreadLiquid", at = @At("HEAD"), cancellable = true)
    private void onShouldSpreadLiquid(Level pLevel, BlockPos pPos, BlockState pState, CallbackInfoReturnable<Boolean> cir) {
        // Cancel the method execution, preventing liquid ticks
        if (Config.disableFluidTicking) {
            WesterosCraftCore.debugLog("Cancelled fluid tick");
           cir.setReturnValue(false);
        }
    }

    @Inject(method = "isRandomlyTicking", at = @At("HEAD"), cancellable=true)
    private void doIsRandomlyTicking(BlockState state, CallbackInfoReturnable<Boolean> ci) {
        if (Config.disableFluidTicking) {
            WesterosCraftCore.debugLog("Cancelled fluid tick");
            ci.setReturnValue(false);
        }
    }

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable=true)
    private void doRandomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom, CallbackInfo ci) {
        if (Config.disableFluidTicking) {
            WesterosCraftCore.debugLog("Cancelled fluid tick");
            ci.cancel();
        }
    }
}
