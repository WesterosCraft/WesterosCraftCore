package com.westeroscraft.westeroscraftcore.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;

/**
 * Testing command
 *
 */
public class HelloWorldCommand implements CommandExecutor{

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		
		src.sendMessage(Text.of("Hello, " + src.getName()));
		
		return CommandResult.success();
	}

}
