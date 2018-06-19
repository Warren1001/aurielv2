package com.kabryxis.auriel.game;

import com.kabryxis.kabutils.data.Arrays;
import com.kabryxis.kabutils.data.file.yaml.ConfigSection;
import com.kabryxis.kabutils.random.weighted.Weighted;
import sx.blah.discord.handle.obj.IChannel;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class HGAction implements Weighted {
	
	private final HungerGames game;
	
	private final String action;
	private final int extraPlayersRequired;
	private final int rarity;
	
	private Set<Predicate<HGPlayer>> predicates;
	private Set<Consumer<HGPlayer>> consumers;
	private int[] dies;
	private Map<Integer, Integer> killCredit;
	private String extra;
	
	public HGAction(HungerGames game, ConfigSection section) {
		this.game = game;
		this.action = section.get("action", String.class);
		this.extraPlayersRequired = section.get("extra-players-required", Integer.class, 0);
		this.rarity = section.get("rarity", Integer.class, 100);
		String diesString = section.get("dies", String.class);
		if(diesString != null) {
			if(diesString.contains(",")) {
				String[] diesArray = diesString.split(",");
				dies = new int[diesArray.length];
				for(int i = 0; i < diesArray.length; i++) {
					dies[i] = Integer.parseInt(diesArray[i].trim());
				}
			}
			else {
				dies = new int[1];
				dies[0] = Integer.parseInt(diesString);
			}
		}
		String killCreditString = section.get("kill-credit", String.class);
		if(killCreditString != null) {
			if(killCreditString.contains(",")) {
				String[] killCreditArray = killCreditString.split(",");
				killCredit = new HashMap<>(killCreditArray.length);
				for(String aKillCreditArray : killCreditArray) {
					String[] killCreditData = aKillCreditArray.trim().split("-");
					killCredit.put(Integer.parseInt(killCreditData[0].trim()), Integer.parseInt(killCreditData[1].trim()));
				}
			}
			else {
				String[] killCreditData = killCreditString.split("-");
				killCredit = new HashMap<>(1);
				killCredit.put(Integer.parseInt(killCreditData[0].trim()), Integer.parseInt(killCreditData[1].trim()));
			}
		}
		extra = section.get("extra", String.class);
		if(extra != null) {
			if(extra.contains(",")) {
				String[] extraStringArray = extra.split(",");
				for(String extraStr : extraStringArray) {
					String[] extraStrArray = extraStr.split(":");
					String command = extraStrArray[0];
					String data = extraStrArray[1];
					game.handleExtraAction(command, this, data);
				}
			}
			else {
				String[] extraStrArray = extra.split(":");
				String command = extraStrArray[0];
				String data = extraStrArray[1];
				game.handleExtraAction(command, this, data);
			}
		}
	}
	
	public HGAction(HungerGames game, String action, int extraPlayersRequired, int[] dies, Map<Integer, Integer> killCredit, int rarity, String extra) {
		this.game = game;
		this.action = action;
		this.extraPlayersRequired = extraPlayersRequired;
		this.dies = dies;
		this.killCredit = killCredit;
		this.rarity = rarity;
		this.extra = extra;
		if(extra != null) {
			if(extra.contains(",")) {
				String[] extraStringArray = extra.split(",");
				for(String extraStr : extraStringArray) {
					String[] extraStrArray = extraStr.split(":");
					game.handleExtraAction(extraStrArray[0], this, extraStrArray[1]);
				}
			}
			else {
				String[] extraStrArray = extra.split(":");
				game.handleExtraAction(extraStrArray[0], this, extraStrArray[1]);
			}
		}
	}
	
	public void save(ConfigSection section) {
		String key = section.getKeys(false).size() + ".";
		section.set(key + "action", action);
		if(extraPlayersRequired != 0) section.set(key + "extra-players-required", extraPlayersRequired);
		if(rarity != 100) section.set(key + "rarity", rarity);
		if(dies != null && dies.length > 0) {
			StringBuilder builder = new StringBuilder();
			for(int i = 0; i < dies.length; i++) {
				builder.append(dies[i]);
				if(i != dies.length - 1) builder.append(",");
			}
			section.set(key + "dies", builder.toString());
		}
		if(killCredit != null && killCredit.size() > 0) {
			StringBuilder builder = new StringBuilder();
			for(Map.Entry<Integer, Integer> mapEntry : killCredit.entrySet()) {
				builder.append(mapEntry.getKey()).append("-").append(mapEntry.getValue()).append(",");
			}
			String killCredit = builder.toString();
			if(killCredit.endsWith(",")) killCredit = killCredit.substring(0, killCredit.length() - 1);
			section.set(key + "kill-credit", killCredit);
		}
		if(extra != null) section.set(key + "extra", extra);
	}
	
	public int getExtraPlayersRequired() {
		return extraPlayersRequired;
	}
	
	@Override
	public int getWeight() {
		return rarity;
	}
	
	public void play(IChannel channel, HGPlayer player, List<HGPlayer> players) {
		if(players.size() < extraPlayersRequired) throw new IllegalArgumentException("Not enough players for this entry (got " + players.size() + ", expected " + extraPlayersRequired + ")");
		String message = action;
		message = message.replace("${0}", player.tag());
		for(int i = 0; i < players.size(); i++) {
			HGPlayer p = players.get(i);
			int index = i + 1;
			message = message.replace("${" + index + "}", p.tag());
			if(killCredit != null) {
				Integer killsCredited = killCredit.get(index);
				if(killsCredited != null) p.incrementKills(killsCredited);
			}
			if(dies != null && Arrays.containsInt(dies, index)) game.kill(p);
		}
		if(consumers != null) consumers.forEach(c -> c.accept(player));
		channel.sendMessage(message);
	}
	
	public void addPredicate(Predicate<HGPlayer> predicate) {
		if(predicates == null) predicates = new HashSet<>();
		predicates.add(predicate);
	}
	
	public void addConsumer(Consumer<HGPlayer> consumer) {
		if(consumers == null) consumers = new HashSet<>();
		consumers.add(consumer);
	}
	
	public boolean test(HGPlayer player) {
		if(predicates == null || predicates.isEmpty()) return true;
		for(Predicate<HGPlayer> predicate : predicates) {
			if(!predicate.test(player)) return false;
		}
		return true;
	}
	
}
