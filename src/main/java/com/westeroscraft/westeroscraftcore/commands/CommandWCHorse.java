package com.westeroscraft.westeroscraftcore.commands;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HorseColor;
import org.spongepowered.api.data.type.HorseColors;
import org.spongepowered.api.data.type.HorseStyle;
import org.spongepowered.api.data.type.HorseStyles;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.EntityType;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
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
	private EntityType et;
	private String perm;
	private Random rnd = new Random();

	public static Map<String, HorseColor> colors = new HashMap<String, HorseColor>();
	public static Map<String, HorseStyle> styles = new HashMap<String, HorseStyle>();

	static {
		colors.put("black", HorseColors.BLACK);
		colors.put("brown", HorseColors.BROWN);
		colors.put("chestnut", HorseColors.CHESTNUT);
		colors.put("creamy", HorseColors.CREAMY);
		colors.put("darkbrown", HorseColors.DARK_BROWN);
		colors.put("gray", HorseColors.GRAY);
		colors.put("white", HorseColors.WHITE);
		styles.put("none", HorseStyles.NONE);
		styles.put("blackdots", HorseStyles.BLACK_DOTS);
		styles.put("white", HorseStyles.WHITE);
		styles.put("whitefield", HorseStyles.WHITEFIELD);
		styles.put("whitedots", HorseStyles.WHITE_DOTS);
	};

	public CommandWCHorse(WesterosCraftCore instance, EntityType et, String perm) {
		core = instance;
		this.et = et;
		this.perm = perm;
	}

	public CommandWCHorse(WesterosCraftCore instance) {
		this(instance, EntityTypes.HORSE, instance.getPlugin().getId() + ".wchorse.command");
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		args.checkPermission(src, perm);
		if (!(src instanceof Player)) {
			src.sendMessage(Text.of(TextColors.RED, "Need to be a player to summon a horse!"));
		} else {
			Player player = (Player) src;

			// Kill existing horse, if needed
			core.killHorseIfNeeded(player);
			//
			// Title title = Title.builder().title(Text.of("Here's A Horse!")).build();
			// player.sendTitle(title);
			Location<World> loc = player.getLocation();
			World w = loc.getExtent();
			Horse horse = (Horse) w.createEntity(et, loc.getPosition());
			// Make the player the horse's owner
			org.spongepowered.api.data.value.mutable.OptionalValue<UUID> owner = horse.getValue(Keys.TAMED_OWNER).get();
			owner.setTo(player.getUniqueId());
			horse.offer(owner);
			// Set the horse's color
			if (horse.getValue(Keys.HORSE_COLOR).isPresent()) {
				HorseColor color = args.<HorseColor>getOne("color").orElse(colors.values().toArray(new HorseColor[0])[rnd.nextInt(colors.size())]);
				Value<HorseColor> col = horse.getValue(Keys.HORSE_COLOR).get();
				col.set(color);
				horse.offer(col);
			}
			// Set the horse's style
			if (horse.getValue(Keys.HORSE_STYLE).isPresent()) {
				HorseStyle style = args.<HorseStyle>getOne("pattern").orElse(styles.values().toArray(new HorseStyle[0])[rnd.nextInt(styles.size())]);
				Value<HorseStyle> sty = horse.getValue(Keys.HORSE_STYLE).get();
				sty.set(style);
				horse.offer(sty);
			}
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
