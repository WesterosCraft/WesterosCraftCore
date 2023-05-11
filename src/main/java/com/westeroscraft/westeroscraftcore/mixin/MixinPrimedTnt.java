package com.westeroscraft.westeroscraftcore.mixin;

import net.minecraft.world.entity.item.PrimedTnt;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.westeroscraft.westeroscraftcore.WesterosCraftCore;

@Mixin(PrimedTnt.class) 
public abstract class MixinPrimedTnt
{	
	// This constructor is fake and never used
	protected MixinPrimedTnt()
	{
	}

	@Inject(method = "explode()V", at = @At("HEAD"), cancellable=true)	
	private void doExplode(CallbackInfo ci) {
		if (WesterosCraftCore.Config.disableTNTExplode.get()) {
			WesterosCraftCore.debugLog("Cancelled tnt explode");
			ci.cancel();
		}
	}

}
