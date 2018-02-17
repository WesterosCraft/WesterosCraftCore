package com.westeroscraft.westeroscraftcore.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectReference;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Text.Builder;
import org.spongepowered.api.text.format.TextColors;
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
	private Logger logger;
	
	public CommandPList(WesterosCraftCore instance){
		logger = instance.getLogger();
	}
	
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		Map<String, List<Text>> playersByGroup = new HashMap<String, List<Text>>();
		
		for (Player p : Sponge.getServer().getOnlinePlayers()) {
			List<SubjectReference> grps = p.getParents();
			SubjectReference best_grp_ref = null;
			Subject best_grp = null;
			for (SubjectReference grp : grps) {
				Subject g = null;
				try {
					g = grp.resolve().get();
				} catch (InterruptedException e) {
				} catch (ExecutionException e) {
				}
				if (g == null) continue;
				if (best_grp == null) {
					best_grp = g;
					best_grp_ref = grp;
				}
				else if (g.isChildOf(best_grp_ref)) { // Assume child group is more privileged
					best_grp = g;
					best_grp_ref = grp;
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
			Text prefix = TextSerializers.FORMATTING_CODE.deserialize(getOptionFromSubject(p, "prefix", ""));
			Text suffix = TextSerializers.FORMATTING_CODE.deserialize(getOptionFromSubject(p, "suffix", ""));
			String namecolor = getOptionFromSubject(p, "namecolor", "");
			Text name = p.getDisplayNameData().displayName().get();
			if (namecolor.length() > 0) {
				name = TextSerializers.FORMATTING_CODE.deserialize("&" + namecolor + name.toPlainSingle());
			}
			// Check for prefix for player
			pid.add(prefix.concat(name).concat(suffix));
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
	private static String getOptionFromSubject(Player p, String option, String def) {
		Optional<String> v = p.getOption(p.getActiveContexts(), option);
		if (v.isPresent())
			return v.get();
		v = p.getOption(option);
		if (v.isPresent())
			return v.get();
		return def;
	}
}
