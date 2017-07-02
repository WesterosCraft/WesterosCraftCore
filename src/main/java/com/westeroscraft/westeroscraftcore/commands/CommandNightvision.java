package com.westeroscraft.westeroscraftcore.commands;

import java.util.Optional;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import com.westeroscraft.westeroscraftcore.MessageUtil;
import com.westeroscraft.westeroscraftcore.WesterosCraftCore;

/**
 * 
 * Command to toggle night vision on or off. If a user has night vision currently enabled,
 * this command will turn it off, and vice versa. When turning on night vision, the duration
 * of the effect will be infinite (equal to Integer.MAX_VALUE, so infinite for our purposes).
 * <br>
 * This command takes an optional argument, being a player name. If the user has the required
 * permission they may enable/disable night vision for any user online.
 * 
 * @author Daniel Scalzi (TheKraken7)
 *
 */
public class CommandNightvision implements CommandExecutor{

	private PotionEffect nv;
	private WesterosCraftCore instance;
	
	public CommandNightvision(WesterosCraftCore instance){
		nv = PotionEffect.builder().potionType(PotionEffectTypes.NIGHT_VISION).duration(Integer.MAX_VALUE).amplifier(1).build();
		this.instance = instance;
	}
	
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		
		Optional<Player> target = args.<Player>getOne(Text.of("player"));
		
		if(!target.isPresent() && !(src instanceof Player)){
			MessageUtil.sendMessage(src, Text.of(TextColors.RED, "Only players may use this command."));
			return CommandResult.empty();
		}
		
		if(target.isPresent() && !target.get().getName().equals(src.getName())){
			args.checkPermission(src, instance.getPlugin().getId() + ".nightvision.others");
		}
		
		Player p = target.isPresent() ? target.get() : (Player)src;
		Optional<PotionEffectData> effectsOpt = p.getOrCreate(PotionEffectData.class);
		
		if(!effectsOpt.isPresent()) return CommandResult.empty();
		PotionEffectData effects = effectsOpt.get();
		
		boolean wasEnabled = false;
		
		for(int i=0; i<effects.asList().size(); ++i){
			if(effects.get(i).isPresent()){
				if(effects.get(i).get().getType() == PotionEffectTypes.NIGHT_VISION){
					effects.remove(i);
					wasEnabled = true;
					break;
				}
			}
		}
		if(!wasEnabled) effects.addElement(nv);
		p.offer(effects);
		
		if(!src.getName().equals(p.getName())) MessageUtil.sendMessage(src, Text.of(TextColors.RED, (wasEnabled ? "Disabled" : "Enabled") + " nightvision for " + p.getName() + "."));
		MessageUtil.sendMessage(p, Text.of(TextColors.RED, "Nightvision " + (wasEnabled ? "disabled" : "enabled") + (src.getName().equals(p.getName()) ? "." : " by " + src.getName() + ".")));
		
		return CommandResult.success();
	}

}
