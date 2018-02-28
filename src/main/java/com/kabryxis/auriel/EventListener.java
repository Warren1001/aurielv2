package com.kabryxis.auriel;

import com.kabryxis.auriel.command.UserIssuer;
import com.kabryxis.kabutils.cache.Cache;
import com.kabryxis.kabutils.command.CommandManager;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;

public class EventListener {
	
	private final Auriel auriel;
	private final CommandManager commandManager;
	
	public EventListener(Auriel auriel) {
		this.auriel = auriel;
		this.commandManager = auriel.getCommandManager();
	}
	
	@EventSubscriber
	public void onMessage(MessageReceivedEvent event) {
		IMessage msg = event.getMessage();
		String message = msg.getContent();
		if(commandManager.isCommand(message)) {
			UserIssuer user = Cache.get(UserIssuer.class);
			user.reuse(msg);
			commandManager.handle(user, commandManager.getAlias(message), commandManager.getArgs(message));
			user.cache();
		}
	}
	
}
