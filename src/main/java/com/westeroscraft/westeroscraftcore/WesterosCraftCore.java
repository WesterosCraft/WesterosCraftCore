package com.westeroscraft.westeroscraftcore;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import java.util.UUID;
import javax.inject.Inject;

import com.westeroscraft.westeroscraftcore.commands.*;
import org.slf4j.Logger;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.trait.IntegerTraits;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.data.type.HorseColor;
import org.spongepowered.api.data.type.HorseColors;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.hanging.ItemFrame;
import org.spongepowered.api.entity.hanging.Painting;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.animal.Horse;
import org.spongepowered.api.entity.living.animal.Wolf;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scoreboard.CollisionRules;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionDescription.Builder;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.westeroscraft.westeroscraftcore.listeners.ResponseListener;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

@Plugin(id = "westeroscraftcore")
public class WesterosCraftCore {

    @Inject private Logger logger;
    @Inject private PluginContainer plugin;
    @Inject @ConfigDir(sharedRoot = false) private File configDir;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> configManager;
    
    private Set<ItemType> guest_blacklist = new HashSet<ItemType>();
    private Set<ItemType> general_blacklist = new HashSet<ItemType>();
    private Set<BlockType> stop_snow_form_on = new HashSet<BlockType>();
    private Set<BlockType> stop_grow = new HashSet<BlockType>();
    private Set<BlockType> stop_form = new HashSet<BlockType>();
    private Set<BlockType> stop_spread = new HashSet<BlockType>();
    private Set<BlockType> stop_modify = new HashSet<BlockType>();
    private Set<BlockType> stop_block_entity_spawn = new HashSet<BlockType>();
    private Set<BlockType> guest_interact_blacklist = new HashSet<BlockType>();
    private int max_wheat_grow_size = -1;
    private int max_carrot_grow_size = -1;
    private int max_potato_grow_size = -1;
    
    // Map of horses by player
    public HashMap<UUID, Horse> horsesByPlayer = new HashMap<UUID, Horse>();

    // Map of wolves by player
    public HashMap<UUID, Wolf> wolvesByPlayer = new HashMap<UUID, Wolf>();

    public boolean server_is_whitelist = false;
    
    private Team builderTeam = null;

    public void initBlacklist() {
        Asset asset = plugin.getAsset("westeroscraftcore.conf").orElse(null);
        Path configPath = configDir.toPath().resolve("westeroscraftcore.conf");
        
        if (Files.notExists(configPath)) {
            if (asset != null) {
                try {
                    asset.copyToFile(configPath);
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.error("Could not unpack the default config from the jar!");
                    return;
                }
            } else {
                logger.error("Could not find the default config file in the jar!");
                return;
            }
        }
        CommentedConfigurationNode rootNode;
        try {
            rootNode = configManager.load();
        } catch (IOException e) {
            logger.error("An IOException occured while trying to load the config");
            return;
        }
        GameRegistry gr = Sponge.getRegistry();
        // Get guest blacklist
        for (CommentedConfigurationNode ch : rootNode.getNode("config", "guest_blacklist").getChildrenMap().values()) {
            String id = ch.getString("");
            ItemType it = gr.getType(ItemType.class, id).orElse(null);
            if (it == null) {
                logger.error("Error finding guest_blacklist item type: " + id);
            }
            else {
                guest_blacklist.add(it);
            }
        }
        // Get general blacklist
        for (CommentedConfigurationNode ch : rootNode.getNode("config", "general_blacklist").getChildrenMap().values()) {
            String id = ch.getString("");
            ItemType it = gr.getType(ItemType.class, id).orElse(null);
            if (it == null) {
                logger.error("Error finding general_blacklist item type: " + id);
            }
            else {
                general_blacklist.add(it);
            }
        }
        // Get guest interact blacklist
        for (CommentedConfigurationNode ch : rootNode.getNode("config", "guest_interact_blacklist").getChildrenMap().values()) {
            String id = ch.getString("");
            BlockType bt = gr.getType(BlockType.class, id).orElse(null);
            if (bt == null) {
                logger.error("Error finding guest_interact_blacklist block type: " + id);
            }
            else {
                guest_interact_blacklist.add(bt);
            }
        }
        // Get stop_snow_form_on
        for (CommentedConfigurationNode ch : rootNode.getNode("config", "stop_snow_form_on").getChildrenMap().values()) {
            String id = ch.getString("");
            BlockType bt = gr.getType(BlockType.class, id).orElse(null);
            if (bt == null) {
                logger.error("Error finding stop_snow_form_on block type: " + id);
            }
            else {
                stop_snow_form_on.add(bt);
            }
        }
        // Get stop_grow
        for (CommentedConfigurationNode ch : rootNode.getNode("config", "stop_grow").getChildrenMap().values()) {
            String id = ch.getString("");
            BlockType bt = gr.getType(BlockType.class, id).orElse(null);
            if (bt == null) {
                logger.error("Error finding stop_grow block type: " + id);
            }
            else {
                stop_grow.add(bt);
            }
        }
        // Get stop_form
        for (CommentedConfigurationNode ch : rootNode.getNode("config", "stop_form").getChildrenMap().values()) {
            String id = ch.getString("");
            BlockType bt = gr.getType(BlockType.class, id).orElse(null);
            if (bt == null) {
                logger.error("Error finding stop_form block type: " + id);
            }
            else {
                stop_form.add(bt);
            }
        }
        // Get stop_modify
        for (CommentedConfigurationNode ch : rootNode.getNode("config", "stop_modify").getChildrenMap().values()) {
            String id = ch.getString("");
            BlockType bt = gr.getType(BlockType.class, id).orElse(null);
            if (bt == null) {
                logger.error("Error finding stop_modify block type: " + id);
            }
            else {
                stop_modify.add(bt);
            }
        }
        // Get stop_spread
        for (CommentedConfigurationNode ch : rootNode.getNode("config", "stop_spread").getChildrenMap().values()) {
            String id = ch.getString("");
            BlockType bt = gr.getType(BlockType.class, id).orElse(null);
            if (bt == null) {
                logger.error("Error finding stop_spread block type: " + id);
            }
            else {
                stop_spread.add(bt);
            }
        }
        // Get stop_block_entity_spawn
        for (CommentedConfigurationNode ch : rootNode.getNode("config", "stop_block_entity_spawn").getChildrenMap().values()) {
            String id = ch.getString("");
            BlockType bt = gr.getType(BlockType.class, id).orElse(null);
            if (bt == null) {
                logger.error("Error finding stop_block_entity_spawn block type: " + id);
            }
            else {
            	stop_block_entity_spawn.add(bt);
            }
        }
        // Migration of current settigns - switch to parameter file
        max_wheat_grow_size = rootNode.getNode("config", "max_wheat_grow_size").getInt(-1);
        max_carrot_grow_size = rootNode.getNode("config", "max_carrot_grow_size").getInt(-1);
        max_potato_grow_size = rootNode.getNode("config", "max_potato_grow_size").getInt(-1);
        
        server_is_whitelist = rootNode.getNode("config", "server_is_whitelist").getBoolean(false);
    }

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
        getLogger().info("Enabling " + plugin.getName() + " version " + plugin.getVersion().orElse("") + ".");

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
                .build(), Arrays.asList("plist"));	
        Sponge.getCommandManager().register(this, CommandSpec.builder()
                .description(Text.of("Toggle whitelist for server."))
                .permission(plugin.getId() + ".whitelist.command")
                .executor(new CommandWCWhitelist(this))
                .build(), Arrays.asList("wcwhitelist"));  
        Sponge.getCommandManager().register(this, CommandSpec.builder()
                .description(Text.of("Get a horse!"))
                .permission(plugin.getId() + ".wchorse.command")
                .executor(new CommandWCHorse(this))
                .arguments(GenericArguments.optional(GenericArguments.choices(Text.of("color"), CommandWCHorse.colors, true)),
                    GenericArguments.optional(GenericArguments.choices(Text.of("pattern"), CommandWCHorse.styles, true)))
                .build(), Arrays.asList("wchorse"));  
            Sponge.getCommandManager().register(this, CommandSpec.builder()
                .description(Text.of("Get a skeleton horse!"))
                .permission(plugin.getId() + ".wcskeletonhorse.command")
                .executor(new CommandWCSkeletonHorse(this))
                .build(), Arrays.asList("wcskeletonhorse"));
        Sponge.getCommandManager().register(this, CommandSpec.builder()
                .description(Text.of("Get a direwolf!"))
                .permission(plugin.getId() + ".wcwolf.command")
                .executor(new CommandWCWolf(this))
                .arguments(GenericArguments.optional(GenericArguments.string(Text.of("name"))))
                .build(), Arrays.asList("wcwolf"));
    }

    @Listener
    public void onGameStartedServer(GameStartedServerEvent e){
        initBlacklist();
        
        Scoreboard sb = Sponge.getServer().getServerScoreboard().orElse(null);
        if (sb != null) {
        	builderTeam = sb.getTeam("Builders").orElse(null);
        	if (builderTeam == null) {
        		builderTeam = Team.builder().name("Builders").collisionRule(CollisionRules.NEVER).build();
        		sb.registerTeam(builderTeam);
        	}
        }
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
    public void onPlayerLogoff(ClientConnectionEvent.Disconnect event) {
        // When player disconnects, kill their horse and wolf, if any
        killHorseIfNeeded(event.getTargetEntity());
        killWolfIfNeeded(event.getTargetEntity());
    }

    @Listener
    public void onPlayerLogin(ClientConnectionEvent.Login event) {
        User player = event.getTargetUser();
        if (server_is_whitelist) {
            if(!player.hasPermission(plugin.getId() + ".whitelist.allowed")) {
                event.setMessage(Text.of(TextColors.RED, "Server is not currently allowing guests - please try again later!"));
                event.setCancelled(true);
            }
        }
    }
    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join e){
        MessageUtil.sendMessage(e.getTargetEntity(), Text.of(TextColors.RED, "Welcome to Westeroscraft! Visit /warps for a list of warps!"));
        Player player = e.getTargetEntity();
        if (builderTeam != null) {
        	builderTeam.addMember(Text.of(player.getName()));
        }
        // Let folks that can use /wcwhitelist command that server is whitelisted
        if (server_is_whitelist && (player.hasPermission(plugin.getId() + ".whitelist.command"))) {
            MessageUtil.sendMessage(player, Text.of(TextColors.YELLOW, "Server is not currently allowing guests - use /wcwhitelist to toggle whitelist"));
        }

        // Set players to creative on login
        player.offer(Keys.GAME_MODE, GameModes.CREATIVE);
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
// Disabled due to privacy concerns
//    @Listener
//    public void logCommandInvocation(SendCommandEvent e){
//        String source = "?unknown?";
//
//        Optional<Player> pOpt;
//        if ((pOpt = e.getCause().first(Player.class)).isPresent()) source = pOpt.get().getName();
//        else if(e.getCause().first(ConsoleSource.class).isPresent()) source = "console";
//        else if(e.getCause().first(CommandBlockSource.class).isPresent()) source = "command block";
//
//        logger.info("{}: /{} {}", source, e.getCommand(), e.getArguments());
//    }

    @Listener
    public void onPostInit(GamePostInitializationEvent event) {
        Optional<PermissionService> ops = Sponge.getServiceManager().provide(PermissionService.class);
        if (ops.isPresent()) {
            Builder opdb = ops.get().newDescriptionBuilder(this);
            if (opdb != null) {
                opdb.assign(PermissionDescription.ROLE_ADMIN, true).description(Text.of("Fire punch")).id(plugin.getId() + ".firepunch").register();
                opdb.assign(PermissionDescription.ROLE_USER, true).description(Text.of("Use [Warp] signs")).id(plugin.getId() + ".warpsign.use").register();
                opdb.assign(PermissionDescription.ROLE_USER, true).description(Text.of("Toggle nightvision.")).id(plugin.getId() + ".nightvision").register();
                opdb.assign(PermissionDescription.ROLE_ADMIN, true).description(Text.of("Toggle nightvision for others.")).id(plugin.getId() + ".nightvision.others").register();
                opdb.assign(PermissionDescription.ROLE_USER, true).description(Text.of("View player list with groups.")).id(plugin.getId() + ".plist").register();
                opdb.assign(PermissionDescription.ROLE_ADMIN, true).description(Text.of("Allow item frame changes.")).id(plugin.getId() + ".itemframe.change").register();
                opdb.assign(PermissionDescription.ROLE_ADMIN, true).description(Text.of("Allow painting changes.")).id(plugin.getId() + ".painting.change").register();
                opdb.assign(PermissionDescription.ROLE_ADMIN, true).description(Text.of("Use guest blacklisted items.")).id(plugin.getId() + ".blacklist.use").register();
                opdb.assign(PermissionDescription.ROLE_ADMIN, true).description(Text.of("Use general blacklisted items.")).id(plugin.getId() + ".generalblacklist.use").register();
                opdb.assign(PermissionDescription.ROLE_ADMIN, true).description(Text.of("Use guest interact blacklisted blocks.")).id(plugin.getId() + ".blockinteract.blacklist.use").register();
                opdb.assign(PermissionDescription.ROLE_ADMIN, true).description(Text.of("Allowed access when server is whitelisted.")).id(plugin.getId() + ".whitelist.allowed").register();
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
    
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onEntityInteract(InteractEntityEvent event, @Root Player player) {
        // Prevent unauthorized folks from messing with item frames, paintings
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

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onItemInteract(InteractItemEvent event, @Root Player player) {
        ItemStackSnapshot item = event.getItemStack();
        if (guest_blacklist.contains(item.getType())) {
            if (!player.hasPermission(plugin.getId() + ".blacklist.use")) {
                event.setCancelled(true);
            }
        }
        if (general_blacklist.contains(item.getType())) {
            if (!player.hasPermission(plugin.getId() + ".generalblacklist.use")) {
                event.setCancelled(true);
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockChangePre(ChangeBlockEvent.Pre event) {
        @SuppressWarnings("unused")
		Cause c = event.getCause();
        EventContext ctx = event.getContext();
        if (ctx.containsKey(EventContextKeys.LEAVES_DECAY)) {
            event.setCancelled(true);
            return;
        }
    }
    
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockChangeDecay(ChangeBlockEvent.Decay event) {
        //for (Transaction<BlockSnapshot> trans : event.getTransactions()) {
        //    logger.info("Decay: " + trans.getOriginal().getState().getType() + " at " + trans.getOriginal().getLocation());
        //}
        // Cancel all decay events - seem to only be for leaves, so we're OK for now
        event.setCancelled(true);
    }
    
    public static User getEventUser(Event event) {
        final Cause cause = event.getCause();
        final EventContext context = event.getContext();
        User user = null;
        if (cause != null) {
            user = cause.first(User.class).orElse(null);
        }

        if (user == null) {
            user = context.get(EventContextKeys.NOTIFIER)
                    .orElse(context.get(EventContextKeys.OWNER)
                            .orElse(context.get(EventContextKeys.CREATOR)
                                    .orElse(null)));
        }

        if (user == null) {
            if (event instanceof ExplosionEvent) {
                // Check igniter
                final Living living = context.get(EventContextKeys.IGNITER).orElse(null);
                if (living != null && living instanceof User) {
                    user = (User) living;
                }
            }
        }

        return user;
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockChangePlace(ChangeBlockEvent.Place event) {
        User user = getEventUser(event);
        //logger.info("onBlockChangePlace() : user=" + ((user != null)?user.getName():"none"));
        if (user != null) {
            for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
                BlockType btinit = transaction.getOriginal().getState().getType();
                BlockSnapshot block = transaction.getFinal();
                BlockType bt = block.getState().getType();
                // If user changing from modify blocked block to non-AIR, assume trouncing protected blocks
                if (stop_modify.contains(btinit) && (bt != BlockTypes.AIR)) {
                    transaction.setValid(false);
                    //logger.info("Cancel place by user for  " + btinit.getId());
                }
            }
            return;
        }
        for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            BlockType btinit = transaction.getOriginal().getState().getType();
            BlockSnapshot block = transaction.getFinal();
            BlockType bt = block.getState().getType();
            Location<World> location = block.getLocation().orElse(null);
            if (location == null) {
                continue;
            }
            //logger.info("onBlockChangePlace() : init=" + btinit.getId() + ", new=" + bt.getId());
            // Handle snow
            if (bt == BlockTypes.SNOW_LAYER) {
                BlockType below_bt = location.add(0, -1, 0).getBlockType();
                // Block type we're blocking snow formation on
                if ((below_bt != null) && (stop_snow_form_on.contains(below_bt))) {
                    transaction.setValid(false);
                }
            }
            // If stop form or stop spread or stop form, cancel transaction
            if (stop_form.contains(bt) || (stop_spread.contains(bt)) || (stop_grow.contains(bt))) {
                transaction.setValid(false);
                //logger.info("Place: " + btinit + "->" + bt + " at " + block.getLocation() + " cancelled");
            }
            else if ((btinit == BlockTypes.GRASS) && (bt == BlockTypes.DIRT)) {
                transaction.setValid(false);
            }
            if (transaction.isValid()) {
                //logger.info("Place: " + btinit + "->" + bt + " at " + block.getLocation());
            }
        }
    }
    
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockChangeModify(ChangeBlockEvent.Modify event) {
        User user = getEventUser(event);
        //logger.info("onBlockChangeModify() : user=" + ((user != null)?user.getName():"none"));
        if (user != null) {	// User action?
            for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
                BlockType btinit = transaction.getOriginal().getState().getType();
                BlockSnapshot block = transaction.getFinal();
                BlockType bt = block.getState().getType();
                // If user changing from modify blocked block to non-AIR, assume trouncing protected blocks
                if (stop_modify.contains(btinit) && (bt != BlockTypes.AIR)) {
                    transaction.setValid(false);
                    //logger.info("Cancel modify by user for  " + btinit.getId());
                }
            }
            return;	// Nothing to do here yet
        }
        else {	// Else automatic placement of some sort
            for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
				BlockType btinit = transaction.getOriginal().getState().getType();
                BlockSnapshot block = transaction.getFinal();
                BlockType bt = block.getState().getType();
                //logger.info("onBlockChangeModify() : init=" + btinit.getId() + ", new=" + bt.getId());
                if ((bt == BlockTypes.WHEAT) && (max_wheat_grow_size >= 0)) {   // If wheat
                    int newage = block.getState().getTraitValue(IntegerTraits.WHEAT_AGE).orElse(0);
                    if (newage > max_wheat_grow_size) {
                        transaction.setValid(false);
                        //logger.info("Cancel wheat grow to " + newage);
                    }
                }
                else if ((bt == BlockTypes.CARROTS) && (max_carrot_grow_size >= 0)) {   // If wheat
                    int newage = block.getState().getTraitValue(IntegerTraits.CARROTS_AGE).orElse(0);
                    if (newage > max_carrot_grow_size) {
                        transaction.setValid(false);
                        //logger.info("Cancel carrot grow to " + newage);
                    }
                }
                else if ((bt == BlockTypes.POTATOES) && (max_potato_grow_size >= 0)) {   // If wheat
                    int newage = block.getState().getTraitValue(IntegerTraits.POTATOES_AGE).orElse(0);
                    if (newage > max_potato_grow_size) {
                        transaction.setValid(false);
                        //logger.info("Cancel potato grow to " + newage);
                    }
                }
                if (stop_modify.contains(btinit)) { // If in no-modify list
                    transaction.setValid(false);
                    //logger.info("Cancel modify for  " + btinit.getId());
                }
                if (transaction.isValid()) {
                    //logger.info("Modify: " + btinit + "->" + bt + " at " + block.getLocation());
                }
            }
        }
    }
    // Handle entity spawns : block drops of apples and saplings by leaves, for example
    @Listener(beforeModifications=true)
    public void onEntitySpawn(SpawnEntityEvent event, @Root Object source) {
        // Stop item drops
        for (Entity ent : event.getEntities()) {
            if (ent.getType() == EntityTypes.ITEM) {
                ent.remove();
            }
        }
    	if (source instanceof BlockSnapshot) {
    		BlockType type = ((BlockSnapshot)source).getState().getType();
    		// If source of item is leaves
    		if (stop_block_entity_spawn.contains(type)) {
    			event.setCancelled(true);
    		}
    	}
    }
    
    @Listener(beforeModifications=true)
    public void onBlockInteract(InteractBlockEvent event, @Root Player user) {
        BlockSnapshot bs = event.getTargetBlock();
        BlockType bt = bs.getState().getType();
        if (guest_interact_blacklist.contains(bt)) {
            if (!user.hasPermission(plugin.getId() + ".blockinteract.blacklist.use")) {
                event.setCancelled(true);
            }
        }
    }

    public void killHorseIfNeeded(Player player) {
        Horse horse = horsesByPlayer.get(player.getUniqueId());
        if (horse != null) {
            horse.remove();
            horsesByPlayer.remove(player.getUniqueId());
        }
    }

    public void killWolfIfNeeded(Player player) {
        Wolf wolf = wolvesByPlayer.get(player.getUniqueId());
        if (wolf != null) {
            wolf.remove();
            wolvesByPlayer.remove(player.getUniqueId());
        }
    }
}