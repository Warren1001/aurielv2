package com.kabryxis.auriel.command;

import com.kabryxis.auriel.Auriel;
import com.kabryxis.auriel.game.HGEntry;
import com.kabryxis.auriel.game.HGEntryType;
import com.kabryxis.auriel.user.UserData;
import com.kabryxis.kabutils.command.Com;
import com.kabryxis.kabutils.command.CommandIssuer;
import com.kabryxis.kabutils.data.file.yaml.Config;
import sx.blah.discord.handle.obj.IMessage;

public class CommandListener {
	
	private final Auriel auriel;
	
	public CommandListener(Auriel auriel) {
		this.auriel = auriel;
	}
	
	@Com(aliases = { "stop" }, permission = "auriel.admin.shutdown")
	public boolean onStop(CommandIssuer issuer, String alias, String[] args) {
		issuer.sendMessage("Bye bye :)");
		auriel.close();
		return true;
	}
	
	@Com(aliases = { "perm" })
	public boolean onPerm(UserIssuer issuer, String alias, String[] args) {
		UserData userData = UserData.getData(issuer.getMsg().getAuthor());
		userData.givePermission("auriel.*");
		userData.save();
		return true;
	}
	
	@Com(aliases = { "district" })
	public boolean onDistrict(UserIssuer issuer, String alias, String[] args) {
		if(args.length == 1) {
			int district;
			try {
				district = Integer.parseInt(args[0]);
			}
			catch(NumberFormatException e) {
				issuer.sendMessage(args[0] + " is not a number you idiot.");
				return true;
			}
			IMessage msg = issuer.getMsg();
			auriel.getGame().getDistrict(district).addPlayer(msg, auriel.getGame().getPlayer(msg.getAuthor()));
			return true;
		}
		return false;
	}
	
	@Com(aliases = { "entry" }, permission = "auriel.game.hg.modify")
	public boolean onEntry(UserIssuer issuer, String alias, String[] args) {
		String message = issuer.getMsg().getContent();
		String[] splitMessage = message.split("\"");
		String action = splitMessage[1];
		message = splitMessage[0].trim();
		message = message.substring(message.indexOf(' ') + 1);
		args = message.split(" ");
		if(args.length == 3) {
			if(args[0].equalsIgnoreCase("add")) {
				auriel.getGame().addEntry(new HGEntry(auriel.getGame(), action, HGEntryType.valueOf(args[1].toUpperCase()), Integer.parseInt(args[2])));
				return true;
			}
		}
		return false;
	}
	
	@Com(aliases = { "game" }, permission = "auriel.game.hg.game")
	public boolean onGame(UserIssuer issuer, String alias, String[] args) {
		if(args.length == 1) {
			if(args[0].equalsIgnoreCase("start")) {
				if(!issuer.hasPermission("auriel.game.hg.game.start")) {
					issuer.sendMessage("u no has permission u noob");
					return true;
				}
				IMessage msg = issuer.getMsg();
				Long gameGuildChannelId = auriel.getGuildData(msg.getGuild()).get("hg-channel", Long.class);
				if(gameGuildChannelId == null) {
					issuer.sendMessage("You need to set the text channel the bot will use for Hunger Games using '" + auriel.getCommandManager().getPrefix() + "game channel set'.");
					return true;
				}
				if(gameGuildChannelId != msg.getChannel().getLongID()) {
					issuer.sendMessage("You need to use the " + msg.getGuild().getChannelByID(gameGuildChannelId).mention() + " channel to use this command.");
					return true;
				}
				auriel.getGame().start(issuer.getMsg().getChannel());
				return true;
			}
			if(args[0].equalsIgnoreCase("stop")) {
				if(!issuer.hasPermission("auriel.game.hg.game.stop")) {
					issuer.sendMessage("u no has permission u noob");
					return true;
				}
				auriel.getGame().stop();
				return true;
			}
		}
		else if(args.length == 2) {
			if(args[0].equalsIgnoreCase("tag")) {
				if(args[1].equalsIgnoreCase("true")) {
					auriel.getGame().getPlayer(issuer.getMsg().getAuthor()).wantsTag(true);
					issuer.sendMessage("You will now be tagged while a Hunger Games is in session.");
					return true;
				}
				if(args[1].equalsIgnoreCase("false")) {
					auriel.getGame().getPlayer(issuer.getMsg().getAuthor()).wantsTag(false);
					issuer.sendMessage("You will no longer be tagged while a Hunger Games is in session.");
					return true;
				}
			}
			else if(args[0].equalsIgnoreCase("channel")) {
				if(args[1].equalsIgnoreCase("set")) {
					if(!issuer.hasPermission("auriel.game.hg.channel.set")) {
						issuer.sendMessage("u no has permission u noob");
						return true;
					}
					IMessage msg = issuer.getMsg();
					Config data = auriel.getGuildData(msg.getGuild());
					data.set("hg-channel", msg.getChannel().getLongID());
					data.save();
					return true;
				}
			}
			else if(args[0].equalsIgnoreCase("data")) {
				if(!issuer.hasPermission("auriel.game.hg.data.game")) {
					issuer.sendMessage("u no has permission u noob");
					return true;
				}
				if(args[1].equalsIgnoreCase("save")) {
					auriel.getGame().saveData();
					issuer.sendMessage("Game data has been saved.");
					return true;
				}
			}
			else if(args[0].equalsIgnoreCase("playerdata")) {
				if(!issuer.hasPermission("auriel.game.hg.data.player")) {
					issuer.sendMessage("u no has permission u noob");
					return true;
				}
				if(args[1].equalsIgnoreCase("save")) {
					auriel.getGame().savePlayerData();
					issuer.sendMessage("Player data has been saved.");
					return true;
				}
			}
		}
		return false;
	}
	
}
