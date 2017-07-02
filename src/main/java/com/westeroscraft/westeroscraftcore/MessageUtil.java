package com.westeroscraft.westeroscraftcore;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

public class MessageUtil {

	private static Text prefix;
	
	static {
		prefix = Text.of(TextColors.GRAY, "| ", TextColors.DARK_RED, TextStyles.BOLD, "W", TextStyles.RESET, TextColors.DARK_RED, "esteros", TextStyles.BOLD, "C", TextStyles.RESET, TextColors.DARK_RED, "raft", TextColors.GRAY, " | ", TextColors.RESET);
	}
	
	/**
	 * Utility method which will prepend our command prefix to any message
	 * send through it.
	 * 
	 * @param sender The target MessageReceiver.
	 * @param txt The text to prepend our command prefix to.
	 */
	public static void sendMessage(MessageReceiver sender, Text txt){
		sender.sendMessage(Text.join(new Text[]{prefix, txt}));
	}
	
}
