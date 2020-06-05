package com.westeroscraft.westeroscraftcore.commands;

import com.westeroscraft.westeroscraftcore.WesterosCraftCore;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.DisplayNameData;
import org.spongepowered.api.data.manipulator.mutable.entity.SizeData;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.animal.Wolf;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.UUID;

/**
 *
 * Command to summon a wolf
 * Code closely ported from CommandWCHorse by Mike Primm
 *
 * @author Yvan Titov
 *
 */
public class CommandWCWolf implements CommandExecutor {
    private WesterosCraftCore core;
    private EntityType et;
    private String perm;

    public CommandWCWolf(WesterosCraftCore instance, EntityType et, String perm) {
        core = instance;
        this.et = et;
        this.perm = perm;
    }

    public CommandWCWolf(WesterosCraftCore instance) {
        this(instance, EntityTypes.WOLF, instance.getPlugin().getId() + ".wcwolf.command");
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        args.checkPermission(src, perm);
        if(!(src instanceof Player)) {
            src.sendMessage(Text.of(TextColors.RED, "Need to be a player to summon a wolf!"));
        } else {
            Player player = (Player) src;

            // kill existing wolf, if needed
            core.killWolfIfNeeded(player);

            // create the wolf
            Location<World> loc = player.getLocation();
            World w = loc.getExtent();
            Wolf wolf = (Wolf) w.createEntity(et, loc.getPosition());

            // set the wolf's owner
            org.spongepowered.api.data.value.mutable.OptionalValue<UUID> owner = wolf.getValue(Keys.TAMED_OWNER).get();
            owner.setTo(player.getUniqueId());
            wolf.offer(owner);

            // add the wolf to the world
            try (StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLUGIN);
                w.spawnEntity(wolf);
            }

            // name the wolf, if a name has been provided
            String name = args.<String>getOne("name").orElse("");
            if (!name.equals("")) {
                wolf.offer(Keys.DISPLAY_NAME, Text.of(name));
            }

            // remember the wolf for cleanup
            core.wolvesByPlayer.put(player.getUniqueId(), wolf);
        }
        return CommandResult.success();
    }
}




















