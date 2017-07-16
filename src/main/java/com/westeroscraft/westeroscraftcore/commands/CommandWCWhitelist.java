package com.westeroscraft.westeroscraftcore.commands;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.westeroscraft.westeroscraftcore.WesterosCraftCore;

/**
 * 
 * Command to toggle whitelist status of server
 * 
 * @author Mike Primm
 *
 */
public class CommandWCWhitelist implements CommandExecutor{
	private WesterosCraftCore core;
	
	public CommandWCWhitelist(WesterosCraftCore instance){
		core = instance;
	}
	
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
	    args.checkPermission(src, core.getPlugin().getId() + ".whitelist.command");
	    
	    if (core.server_is_whitelist) {    // We're whitelisted
	        core.server_is_whitelist = false;  // Allow guests
            src.sendMessage(Text.of(TextColors.GREEN, "Now allowing guest logins!"));
	    }
	    else {
            core.server_is_whitelist = true;  // Do not allow guests
            src.sendMessage(Text.of(TextColors.RED, "Not allowing guests!!!"));
            for (Player p : Sponge.getServer().getOnlinePlayers()) {
                if(!p.hasPermission(core.getPlugin().getId() + ".whitelist.allowed")) {
                    p.kick(Text.of(TextColors.RED, "Not allowing guests right now!!!"));
                }
            }
	    }
		return CommandResult.success();
	}
}
