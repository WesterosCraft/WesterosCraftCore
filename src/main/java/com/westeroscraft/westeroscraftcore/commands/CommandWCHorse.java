package com.westeroscraft.westeroscraftcore.commands;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.animal.Horse;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.UUID;

import com.westeroscraft.westeroscraftcore.WesterosCraftCore;


/**
 * 
 * Command to summon a horse
 * 
 * @author Mike Primm
 */
public class CommandWCHorse implements CommandExecutor {
	private WesterosCraftCore core;

	public CommandWCHorse(WesterosCraftCore instance) {
		core = instance;
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		args.checkPermission(src, core.getPlugin().getId() + ".wchorse.command");
		if (!(src instanceof Player)) {
			src.sendMessage(Text.of(TextColors.RED, "Need to be a player to summon a horse!"));
		} else {
			Player player = (Player) src;

			// Kill existing horse, if needed
			core.killHorseIfNeeded(player);
			//
			//Title title = Title.builder().title(Text.of("Here's A Horse!")).build();
			//player.sendTitle(title);
			Location<World> loc = player.getLocation();
			World w = loc.getExtent();
			Horse horse = (Horse) w.createEntity(EntityTypes.HORSE, loc.getPosition());
			// Make the player the horse's owner
			org.spongepowered.api.data.value.mutable.OptionalValue<UUID> owner = horse.getValue(Keys.TAMED_OWNER).get();
			owner.setTo(player.getUniqueId());
			horse.offer(owner);
			// Add saddle to horse
			Iterable<Slot> slots = horse.getInventory().slots();
			for (Slot s : slots) {
				ItemStack saddle = ItemStack.builder().itemType(ItemTypes.SADDLE).build();
				s.set(saddle);
				break;
			}
			// Add horse to the world
			try (StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
				frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLUGIN);
				w.spawnEntity(horse);
			}
			// Make player passenger
			horse.addPassenger(player);
			// Remember horse for cleanup
			core.horsesByPlayer.put(player.getUniqueId(), horse);
		}
		return CommandResult.success();
	}
}
