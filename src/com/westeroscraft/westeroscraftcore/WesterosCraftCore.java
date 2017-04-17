package com.westeroscraft.westeroscraftcore;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.source.CommandBlockSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.google.inject.Inject;
import com.westeroscraft.westeroscraftcore.commands.CommandNightvision;
import com.westeroscraft.westeroscraftcore.listeners.ResponseListener;

@Plugin(id = "westeroscraftcore", 
		name = "WesterosCraftCore",
		version = "1.0", 
		description = "Core Utility plugin for WesterosCraft", 
		authors = {"TheKraken7", "mikeprimm", "Will Blew"})
public class WesterosCraftCore {

	@Inject private Logger logger;
	@Inject private PluginContainer plugin;
	@Inject @ConfigDir(sharedRoot = false) private File configDir;
	
	public Logger getLogger(){
		return logger;
	}
	
	public File getConfigDirectory(){
		return configDir;
	}
	
	public PluginContainer getPlugin(){
		return plugin;
	}
	
	/**
	 * 
	 * All initializations for the plugin should be done here.
	 * 
	 * @param e GamePreInitializationEvent dispatched by Sponge.
	 */
	@Listener
	public void onGamePreInitialization(GamePreInitializationEvent e){
		getLogger().info("Enabling " + plugin.getName() + " version " + plugin.getVersion().get() + ".");
		
		ResponseListener rl = new ResponseListener(this);
		
		Sponge.getEventManager().registerListener(this, MessageChannelEvent.Chat.class, rl);
		
		Sponge.getCommandManager().register(this, CommandSpec.builder()
				.description(Text.of("Enable or disable nightvision."))
				.permission(plugin.getId() + ".nightvision")
				.arguments(GenericArguments.onlyOne(GenericArguments.optionalWeak(GenericArguments.player(Text.of("player")))))
				.executor(new CommandNightvision(this))
				.build(), Arrays.asList("nightvision", "nv"));
	}
	
	/**
	 * Sponge Implementation of Hotfix1.
	 * 
	 * This method listens to block breaks and prevents fire from
	 * being broken unless the user has the necessary permission.
	 * 
	 * @param e ChangeBlockEvent.Break dispatched by Sponge.
	 */
	@Listener
	public void blockFirePunch(ChangeBlockEvent.Break e){
		for(Transaction<BlockSnapshot> t : e.getTransactions()){
			if(t.getOriginal().getState().getType().equals(BlockTypes.FIRE)){
				Optional<Player> playerOpt = e.getCause().first(Player.class);
				if(playerOpt.isPresent()){
					Player p = playerOpt.get();
					if(!p.hasPermission(plugin.getId() + ".firepunch")){
						e.setCancelled(true);
					}
				}
			}
		}
	}
	
	@Listener
	public void onPlayerJoin(ClientConnectionEvent.Join e){
		MessageUtil.sendMessage(e.getTargetEntity(), Text.of(TextColors.RED, "Welcome to Westeroscraft! Visit /warps for a list of warps!"));
	}
	
	/**
	 * 
	 * Any and all reload tasks will be called here if the command
	 * <code>/sponge plugins reload</code> is run.
	 * 
	 * @param e GameReloadEvent dispatched by Sponge.
	 */
	@Listener
	public void onReload(GameReloadEvent e){
		ResponseListener.loadResponses(this);
	}
	
	/**
	 * Log all command invocations to the console for security.
	 * 
	 * @param e SendCommandEvent dispatched by Sponge.
	 */
	@Listener
	public void logCommandInvocation(SendCommandEvent e){
        String source = "?unknown?";
        
        Optional<Player> pOpt;
        if ((pOpt = e.getCause().first(Player.class)).isPresent()) source = pOpt.get().getName();
        else if(e.getCause().first(ConsoleSource.class).isPresent()) source = "console";
        else if(e.getCause().first(CommandBlockSource.class).isPresent()) source = "command block";

        logger.info("{}: /{} {}", source, e.getCommand(), e.getArguments());
	}
	
}
	
