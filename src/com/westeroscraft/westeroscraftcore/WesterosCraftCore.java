package com.westeroscraft.westeroscraftcore;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.source.CommandBlockSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.hanging.ItemFrame;
import org.spongepowered.api.entity.hanging.Painting;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.google.inject.Inject;
import com.westeroscraft.westeroscraftcore.commands.CommandNightvision;
import com.westeroscraft.westeroscraftcore.commands.CommandPList;
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
		Sponge.getCommandManager().register(this, CommandSpec.builder()
				.description(Text.of("Display player list, with groups."))
				.permission(plugin.getId() + ".plist")
				.executor(new CommandPList(this))
				.build(), Arrays.asList("plist"));	}
	
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
	
    @Listener
    public void onPostInit(GamePostInitializationEvent event) {
        Optional<PermissionService> ops = Sponge.getServiceManager().provide(PermissionService.class);
        if (ops.isPresent()) {
            Optional<PermissionDescription.Builder> opdb = ops.get().newDescriptionBuilder(this);
            if (opdb.isPresent()) {
            	opdb.get().assign(PermissionDescription.ROLE_ADMIN, true).description(Text.of("Fire punch")).id(plugin.getId() + ".firepunch").register();
            	opdb.get().assign(PermissionDescription.ROLE_USER, true).description(Text.of("Use [Warp] signs")).id(plugin.getId() + ".warpsign.use").register();
            	opdb.get().assign(PermissionDescription.ROLE_USER, true).description(Text.of("Toggle nightvision.")).id(plugin.getId() + ".nightvision").register();
            	opdb.get().assign(PermissionDescription.ROLE_ADMIN, true).description(Text.of("Toggle nightvision for others.")).id(plugin.getId() + ".nightvision.others").register();
            	opdb.get().assign(PermissionDescription.ROLE_USER, true).description(Text.of("View player list with groups.")).id(plugin.getId() + ".plist").register();
            	opdb.get().assign(PermissionDescription.ROLE_ADMIN, true).description(Text.of("Allow item frame changes.")).id(plugin.getId() + ".itemframe.change").register();
            	opdb.get().assign(PermissionDescription.ROLE_ADMIN, true).description(Text.of("Allow painting changes.")).id(plugin.getId() + ".painting.change").register();
            }
        }
    }
    
    @Listener
    public void onSignTarget(InteractBlockEvent.Secondary event, @Root Player player) {
		Optional<Location<World>> optLocation = event.getTargetBlock().getLocation();
		if (optLocation.isPresent() && optLocation.get().getTileEntity().isPresent()) {
			Location<World> location = optLocation.get();
			TileEntity clickedEntity = location.getTileEntity().get();

			if (event.getTargetBlock().getState().getType().equals(BlockTypes.STANDING_SIGN) || event.getTargetBlock().getState().getType().equals(BlockTypes.WALL_SIGN)) {
				Optional<SignData> signData = clickedEntity.getOrCreate(SignData.class);

				if (signData.isPresent()) {
					SignData data = signData.get();
					CommandManager cmdService = Sponge.getGame().getCommandManager();
					String line0 = data.getValue(Keys.SIGN_LINES).get().get(0).toPlain();
					String line1 = data.getValue(Keys.SIGN_LINES).get().get(1).toPlain();
					String command = "warp " + line1;

					if (line0.indexOf("[Warp]") >= 0) {
						if (player.hasPermission(plugin.getId() + ".warpsign.use")) {
							cmdService.process(player, command);
						}
						else {
							player.sendMessage(Text.of(TextColors.DARK_RED, "Error! ", TextColors.RED, "You do not have permission to use Warp Signs!"));
						}
					}
				}
			}
		}
	}
    @Listener
    public void onEntityInteract(InteractEntityEvent event, @Root Player player) {
    	// Prevent unauthorized folks from messing with item frames
    	Entity ent = event.getTargetEntity();
    	if (ent instanceof ItemFrame) {
			if (!player.hasPermission(plugin.getId() + ".itemframe.change")) {
				event.setCancelled(true);
			}
    	}
    	else if (ent instanceof Painting) {
			if (!player.hasPermission(plugin.getId() + ".painting.change")) {
				event.setCancelled(true);
			}
    	}
    }
    @Listener
    public void onItemInteract(InteractItemEvent event, @Root Player player) {
    	ItemStackSnapshot item = event.getItemStack();
    	if (item.getType() == ItemTypes.ITEM_FRAME) {
			if (!player.hasPermission(plugin.getId() + ".itemframe.change")) {
				event.setCancelled(true);
			}
    	}
    	else if (item.getType() == ItemTypes.PAINTING) {
			if (!player.hasPermission(plugin.getId() + ".painting.change")) {
				event.setCancelled(true);
			}
    	}
    }
    
    @Listener
    public void onBlockChangePre(ChangeBlockEvent.Pre event) {
    	Cause c = event.getCause();
    	if (c.containsNamed(NamedCause.DECAY) || c.containsNamed("LeavesDecay")) {	// Workaround for current SpongeForge vs SpongeAPI mismatch
    		event.setCancelled(true);
    		return;
    	}
    }
    @Listener
    public void onBlockChangeDecay(ChangeBlockEvent.Decay event) {
    	// Cancel all decay events - seem to only be for leaves, so we're OK for now
		event.setCancelled(true);
    }
}
	
