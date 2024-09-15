package com.westeroscraft.westeroscraftcore.mixin;

import com.westeroscraft.westeroscraftcore.Config;
import net.minecraft.world.entity.vehicle.MinecartTNT;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.westeroscraft.westeroscraftcore.WesterosCraftCore;

@Mixin(MinecartTNT.class)
public abstract class MixinMinecartTNT
{
    @Inject(method = "explode(D)V", at = @At("HEAD"), cancellable=true)
    private void doExplode(double radius, CallbackInfo ci) {
        if (Config.disableTNTExplode) {
            WesterosCraftCore.debugLog("Cancelled minecart tnt explode");
            ci.cancel();
        }
    }

}
