package com.westeroscraft.westeroscraftcore;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

import com.google.inject.Inject;
import com.westeroscraft.westeroscraftcore.commands.HelloWorldCommand;

@Plugin(id = "westeroscraftcore", 
		name = "WesterosCraftCore",
		version = "1.0", 
		description = "Core Utility plugin for WesterosCraft", 
		authors = {"TheKraken7", "mikeprimm", "Will Blew"})
public class WesterosCraftCore {

	@Inject private Logger logger;
	@Inject private PluginContainer plugin;
	@Inject @ConfigDir(sharedRoot = false) private File config;
	
	public Logger getLogger(){
		return logger;
	}
	
	@Listener
	public void onGamePreInitialization(GamePreInitializationEvent e){
		getLogger().info("Enabling " + plugin.getName() + " version " + plugin.getVersion().get() + ".");
		
		//Testing command
		Sponge.getCommandManager().register(this, CommandSpec.builder()
				.description(Text.of("Test Command"))
				.permission(plugin.getId() + ".helloworld")
				.executor(new HelloWorldCommand())
				.build(), Arrays.asList("hello"));
	}
	
	
	/**
	 * Sponge Implementation of Hotfix1.
	 * 
	 * This method listens to block breaks and prevents fire from
	 * being broken unless the user has the permission necessary
	 * permission.
	 * 
	 * @param e ChangeBlockEvent.Break dispatched by Sponge.
	 */
	@Listener
	public void onHandInteract(ChangeBlockEvent.Break e){
		System.out.println("Called");
		System.out.println(e.getTransactions());
		for(Transaction<BlockSnapshot> t : e.getTransactions()){
			if(t.getOriginal().getState().getType().equals(BlockTypes.FIRE)){
				System.out.println("is fire");
				Optional<Player> playerOpt = e.getCause().first(Player.class);
				if(playerOpt.isPresent()){
					System.out.println("is player");
					Player p = playerOpt.get();
					if(!p.hasPermission(plugin.getId() + ".firepunch")){
						System.out.println("Should cancel.");
						e.setCancelled(true);
					}
				}
			}
		}
	}
}
	
