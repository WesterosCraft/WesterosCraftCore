package com.westeroscraft.westeroscraftcore.mixin;

import com.westeroscraft.westeroscraftcore.Config;
import com.westeroscraft.westeroscraftcore.WesterosCraftCore;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IceBlock.class)
public abstract class MixinIceBlock {

    // Inject into the method responsible for ice melting behavior
    @Inject(method = "melt", at = @At("HEAD"), cancellable = true)
    private void disableIceMelt(BlockState pState, Level pLevel, BlockPos pPos, CallbackInfo ci) {
        if (Config.disableIceMelt) {
            WesterosCraftCore.debugLog("Cancelling ice melt");
            // Cancel the ice melting by cancelling the tick event
            ci.cancel();
        }
    }
}
