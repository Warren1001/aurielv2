package com.kabryxis.auriel.command;

import com.kabryxis.auriel.Auriel;
import com.kabryxis.auriel.game.HGAction;
import com.kabryxis.auriel.game.HGPlayer;
import com.kabryxis.auriel.user.UserData;
import com.kabryxis.kabutils.command.Com;
import com.kabryxis.kabutils.command.CommandIssuer;
import com.kabryxis.kabutils.data.file.yaml.Config;
import sx.blah.discord.handle.obj.IMessage;

import java.util.HashMap;
import java.util.Map;

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
			IMessage msg = issuer.getMsg();
			auriel.getGame().getDistrict(Integer.parseInt(args[0])).addPlayer(msg, HGPlayer.getPlayer(auriel.getGame(), msg.getAuthor()));
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
		String extraString = splitMessage[2].trim();
		args = message.split(" ");
		if(args.length == 6) {
			if(args[0].equalsIgnoreCase("add")) {
				int playersRequired = Integer.parseInt(args[2]);
				int[] dies;
				if(!args[3].equalsIgnoreCase("null")) {
					if(args[3].contains(",")) {
						String[] diesArray = args[3].split(",");
						dies = new int[diesArray.length];
						for(int i = 0; i < diesArray.length; i++) {
							dies[i] = Integer.parseInt(diesArray[i].trim());
						}
					}
					else {
						dies = new int[1];
						dies[0] = Integer.parseInt(args[3]);
					}
				}
				else dies = new int[0];
				Map<Integer, Integer> killCredit;
				if(!args[4].equalsIgnoreCase("null")) {
					if(args[4].contains(",")) {
						String[] killCreditArray = args[4].split(",");
						killCredit = new HashMap<>(killCreditArray.length);
						for(String aKillCreditArray : killCreditArray) {
							String[] killCreditData = aKillCreditArray.trim().split("-");
							killCredit.put(Integer.parseInt(killCreditData[0].trim()), Integer.parseInt(killCreditData[1].trim()));
						}
					}
					else {
						String[] killCreditData = args[4].split("-");
						killCredit = new HashMap<>(1);
						killCredit.put(Integer.parseInt(killCreditData[0].trim()), Integer.parseInt(killCreditData[1].trim()));
					}
				}
				else killCredit = new HashMap<>();
				int rarity = Integer.parseInt(args[5]);
				HGAction entry = new HGAction(auriel.getGame(), action, playersRequired, dies, killCredit, rarity, extraString.isEmpty() ? null : extraString);
				if(args[1].equalsIgnoreCase("standard")) {
					auriel.getGame().addStandardEntry(entry);
					return true;
				}
				if(args[1].equalsIgnoreCase("start")) {
					auriel.getGame().addStartEntry(entry);
					return true;
				}
				if(args[1].equalsIgnoreCase("feast")) {
					auriel.getGame().addFeastEntry(entry);
					return true;
				}
			}
		}
		return false;
	}
	
	@Com(aliases = { "game" }, permission = "auriel.game.hg.start")
	public boolean onGame(UserIssuer issuer, String alias, String[] args) {
		if(args.length == 1) {
			if(args[0].equalsIgnoreCase("start")) {
				IMessage msg = issuer.getMsg();
				Long gameGuildChannelId = auriel.getGuildData(msg.getGuild()).get("hg-channel", Long.class);
				if(gameGuildChannelId == null) {
					issuer.sendMessage("You need to set the text channel the bot will use for Hunger Games using '" + auriel.getCommandManager().getPrefix() + "hgchannel'.");
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
				auriel.getGame().stop();
				return true;
			}
		}
		return false;
	}
	
	@Com(aliases = { "hgchannel" })
	public boolean onHGChannel(UserIssuer issuer, String alias, String[] args) {
		IMessage msg = issuer.getMsg();
		Config data = auriel.getGuildData(msg.getGuild());
		data.set("hg-channel", msg.getChannel().getLongID());
		data.save();
		return true;
	}
	
	@Com(aliases = { "savegamedata" })
	public boolean onSaveGameData(UserIssuer issuer, String alias, String[] args) {
		auriel.getGame().saveData();
		return true;
	}
	
	@Com(aliases = { "saveallplayergamedata" })
	public boolean onSaveAllPlayerGameData(UserIssuer issuer, String alias, String[] args) {
		HGPlayer.saveAll();
		return true;
	}
	
}
