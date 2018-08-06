package com.kabryxis.auriel.game;

import com.kabryxis.kabutils.data.file.yaml.ConfigSection;
import com.kabryxis.kabutils.random.Randoms;
import com.kabryxis.kabutils.random.weighted.Weighted;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HGEntry implements Weighted {
	
	private final Pattern pattern = Pattern.compile("\\$\\{(.*?)}");
	private final Map<Integer, Set<String>> extraCommands = new HashMap<>();
	private final Set<Predicate<HGPlayer>> predicates = new HashSet<>();
	
	private final HungerGames game;
	private final String action;
	private final HGEntryType type;
	private final int requiredExtraPlayers;
	private final int weight;
	
	public HGEntry(HungerGames game, String action, HGEntryType type, int weight) {
		this.game = game;
		this.action = action;
		this.type = type;
		int requiredExtraPlayers = 0;
		Matcher matcher = pattern.matcher(action);
		while(matcher.find()) {
			String s = matcher.group(1);
			if(s.contains(":")) {
				String[] args = s.split(":");
				String extra = args[1];
				int n = Integer.parseInt(args[0]);
				if(n > requiredExtraPlayers) requiredExtraPlayers = n;
				Set<String> extraSet = extraCommands.computeIfAbsent(n, o -> new HashSet<>());
				if(extra.contains(",")) Collections.addAll(extraSet, extra.split(","));
				else extraSet.add(extra);
				for(String command : extraSet) {
					if(command.contains("~")) {
						String[] commandArgs = command.split("~");
						game.loadExtraAction(commandArgs[0], this, commandArgs[1]);
					}
					else game.loadExtraAction(command, this, "");
				}
			}
			else {
				int n = Integer.parseInt(s);
				if(n > requiredExtraPlayers) requiredExtraPlayers = n;
			}
		}
		this.requiredExtraPlayers = requiredExtraPlayers;
		this.weight = weight;
	}
	
	public void save(ConfigSection section) {
		section.set("action", action);
		section.set("type", type.name());
		section.set("weight", weight);
	}
	
	public int getRequiredExtraPlayers() {
		return requiredExtraPlayers;
	}
	
	@Override
	public int getWeight() {
		return weight;
	}
	
	public HGEntryType getType() {
		return type;
	}
	
	public void addPredicate(Predicate<HGPlayer> predicate) {
		predicates.add(predicate);
	}
	
	public String getMessage(HGPlayer player, List<HGPlayer> randomList) {
		String message = action;
		for(int i = 0; i < requiredExtraPlayers + 1; i++) {
			message = message.replaceAll("\\$\\{" + i + ".*?}", performActions(i == 0 ? player : Randoms.getRandomAndRemove(randomList), extraCommands.get(i)));
		}
		return message;
	}
	
	public String performActions(HGPlayer player, Set<String> commands) {
		if(commands != null) {
			for(String command : commands) {
				if(command.contains("~")) {
					String[] commandArgs = command.split("~");
					game.handleExtraAction(commandArgs[0], player, commandArgs[1]);
				}
				else game.handleExtraAction(command, player, "");
			}
		}
		return player.tag();
	}
	
	public boolean test(HGPlayer player) {
		for(Predicate<HGPlayer> predicate : predicates) {
			if(!predicate.test(player)) return false;
		}
		return true;
	}
	
}
