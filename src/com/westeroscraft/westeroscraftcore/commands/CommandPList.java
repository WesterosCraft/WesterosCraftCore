package com.westeroscraft.westeroscraftcore.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Text.Builder;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.SafeTextSerializer;
import org.spongepowered.api.text.serializer.TextSerializers;

import com.westeroscraft.westeroscraftcore.WesterosCraftCore;

/**
 * 
 * Command to provide list of online players, organized by their primary group
 * 
 * @author Mike Primm
 *
 */
public class CommandPList implements CommandExecutor{
	public CommandPList(WesterosCraftCore instance){
	}
	
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		Map<String, List<Text>> playersByGroup = new HashMap<String, List<Text>>();
		
		for (Player p : Sponge.getServer().getOnlinePlayers()) {
			List<Subject> grps = p.getParents();
			Subject best_grp = null;
			for (Subject grp : grps) {
				if (best_grp == null) {
					best_grp = grp;
				}
				else if (grp.isChildOf(best_grp)) { // Assume child group is more privileged
					best_grp = grp;
				}
			}
			String gid = "default";
			if (best_grp != null) {
				gid = best_grp.getIdentifier();
			}
			List<Text> pid = playersByGroup.get(gid);
			if (pid == null) {
				pid = new ArrayList<Text>();
				playersByGroup.put(gid,  pid);
			}
			Text prefix = TextSerializers.FORMATTING_CODE.deserialize(p.getOption(p.getActiveContexts(), "prefix").orElse(""));
			Text suffix = TextSerializers.FORMATTING_CODE.deserialize(p.getOption(p.getActiveContexts(), "suffix").orElse(""));
			// Check for prefix for player
			pid.add(prefix.concat(p.getDisplayNameData().displayName().get()).concat(suffix));
		}
		String[] grpids = playersByGroup.keySet().toArray(new String[0]);
		Arrays.sort(grpids);
		for (String grp : grpids) {
			Builder bld = Text.builder();
			bld.append(Text.of(TextColors.NONE, grp + ": "));
			boolean first = true;
			for (Text user : playersByGroup.get(grp)) {
				if (!first) {
					bld.append(Text.of(TextColors.NONE, ", "));
				}
				bld.append(user);
				first = false;
			}
			src.sendMessage(bld.build());
		}
		if (grpids.length == 0) {	// Nobody found
			src.sendMessage(Text.of(TextColors.RED, "No players currently online!"));
		}
		return CommandResult.success();
	}

}
