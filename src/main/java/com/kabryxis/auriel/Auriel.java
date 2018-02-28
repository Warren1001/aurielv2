package com.kabryxis.auriel;

import com.kabryxis.auriel.command.CommandListener;
import com.kabryxis.auriel.user.UserData;
import com.kabryxis.kabutils.Console;
import com.kabryxis.kabutils.ConsoleIssuer;
import com.kabryxis.kabutils.command.CommandManager;
import com.kabryxis.kabutils.concurrent.Threads;
import com.kabryxis.kabutils.data.file.yaml.Config;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IUser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Auriel {
	
	public static void main(String[] args) {
		String token;
		try {
			token = new String(Files.readAllBytes(Paths.get("token.txt")));
		}
		catch(IOException e) {
			System.out.println("Could not load token from token.txt:");
			e.printStackTrace();
			Threads.sleep(5000);
			System.exit(0);
			return;
		}
		new ClientBuilder().withToken(token).login().getDispatcher().registerTemporaryListener((ReadyEvent event) -> new Auriel(event.getClient()));
	}
	
	private final IDiscordClient client;
	private final CommandManager commandManager;
	
	public Auriel(IDiscordClient client) {
		this.client = client;
		this.commandManager = new CommandManager();
		new Console(msg -> {
			if(commandManager.isCommand(msg)) commandManager.handle(ConsoleIssuer.get(), commandManager.getAlias(msg), commandManager.getArgs(msg));
		}).start();
		commandManager.registerListener(new CommandListener(this));
		client.getDispatcher().registerListener(new EventListener(this));
	}
	
	public IDiscordClient getClient() {
		return client;
	}
	
	public CommandManager getCommandManager() {
		return commandManager;
	}
	
	public void close() {
		UserData.saveAll();
		Threads.stopThreads();
		client.logout();
		System.exit(0);
	}
	
}
