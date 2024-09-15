package com.westeroscraft.westeroscraftcore.mixin;

import com.westeroscraft.westeroscraftcore.Config;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public abstract class MixinFoodData
{
    @Shadow private int foodLevel;

    // This constructor is fake and never used
    protected MixinFoodData()
    {
    }

    @Inject(method = "tick(Lnet/minecraft/world/entity/player/Player;)V", at = @At("HEAD"))
    private void doTick(Player p, CallbackInfo ci) {
        if (Config.disableHunger) {
            foodLevel = 20;
        }
    }
}
