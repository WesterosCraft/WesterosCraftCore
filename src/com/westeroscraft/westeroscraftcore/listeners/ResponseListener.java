package com.westeroscraft.westeroscraftcore.listeners;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;

import com.westeroscraft.westeroscraftcore.WesterosCraftCore;

public class ResponseListener implements EventListener<MessageChannelEvent.Chat>{

	private static Map<String, String> responses;
	
	public ResponseListener(WesterosCraftCore instance){
		if(responses == null) loadResponses(instance);
	}
	
	/**
	 * Method to load the responses into memory. If a responses.properties
	 * file is already saved to our data directory, we will load that. If
	 * not, we will save the default responses.properties to the data
	 * directory. 
	 * 
	 * @param instance Instance of our main plugin class.
	 */
	public static void loadResponses(WesterosCraftCore instance){
		
		if(responses == null) responses = new HashMap<String, String>();
		else responses.clear();
		
		File responseFile = new File(instance.getConfigDirectory(), "responses.properties");
		
		if(!responseFile.exists()){
			Optional<Asset> asset = Sponge.getAssetManager().getAsset(instance, "responses.properties");
			if(asset.isPresent()){
				Asset a = asset.get();
				try {
					a.copyToFile(responseFile.toPath());
				} catch (IOException e) {
					instance.getLogger().error("Could not save responses file.", e);
					return;
				}
			}
		}
		
		try(FileInputStream fis = new FileInputStream(responseFile)){
			
			Properties props = new Properties();
			props.load(fis);
			
			Enumeration<Object> keys = props.keys();
			
			while(keys.hasMoreElements()){
				String key = ((String) keys.nextElement()).toLowerCase();
				String value = props.getProperty(key);
				responses.put(key, value);
			}
			
		} catch (FileNotFoundException e) {
			instance.getLogger().error("Resources file could not be found.", e);
		} catch (IOException e) {
			instance.getLogger().error("Error while reading resources file.", e);
		}
		
	}
	
	/**
	 * 
	 * Remove punctuation from a message.
	 * 
	 * @param s The String to simplify.
	 * @return The same String without punctuation.
	 */
	private static String simplify(String s) {
    	return s.replaceAll("[ \\?!\\.]", "");
    }
	
	@Override
	public void handle(MessageChannelEvent.Chat e) throws Exception {
		String response = responses.get(simplify(e.getRawMessage().toPlain()).toLowerCase());
		if(response != null){
			Optional<MessageReceiver> senderOpt = e.getCause().first(MessageReceiver.class);
			if(senderOpt.isPresent()){
				MessageReceiver sender = senderOpt.get();
				e.setCancelled(true);
				sender.sendMessage(Text.of(response));
			}
		}
	}
	
}
