package com.westeroscraft.westeroscraftcore.commands;

import org.spongepowered.api.entity.EntityTypes;

import com.westeroscraft.westeroscraftcore.WesterosCraftCore;
import com.westeroscraft.westeroscraftcore.commands.CommandWCHorse;


/**
 * 
 * Command to summon a horse
 * 
 * @author Mike Primm
 */
public class CommandWCSkeletonHorse extends CommandWCHorse {
	public CommandWCSkeletonHorse(WesterosCraftCore instance) {
		super(instance, EntityTypes.SKELETON_HORSE, instance.getPlugin().getId() + ".wcskeletonhorse.command");
	}
}
