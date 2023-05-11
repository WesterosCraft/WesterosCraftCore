package com.westeroscraft.westeroscraftcore.mixin;

import net.minecraft.world.entity.vehicle.MinecartTNT;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.westeroscraft.westeroscraftcore.WesterosCraftCore;

@Mixin(MinecartTNT.class) 
public abstract class MixinMinecartTNT
{	
	// This constructor is fake and never used
	protected MixinMinecartTNT()
	{
	}

	@Inject(method = "explode(D)V", at = @At("HEAD"), cancellable=true)	
	private void doExplode(double radius, CallbackInfo ci) {
		if (WesterosCraftCore.Config.disableTNTExplode.get()) {
			WesterosCraftCore.debugLog("Cancelled minecart tnt explode");
			ci.cancel();
		}
	}

}
