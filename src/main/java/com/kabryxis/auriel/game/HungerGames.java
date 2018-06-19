package com.kabryxis.auriel.game;

import com.kabryxis.auriel.Auriel;
import com.kabryxis.kabutils.concurrent.Threads;
import com.kabryxis.kabutils.data.file.yaml.Config;
import com.kabryxis.kabutils.data.file.yaml.ConfigSection;
import com.kabryxis.kabutils.random.Randoms;
import com.kabryxis.kabutils.random.weighted.conditional.ConditionalWeightedRandomArrayList;
import sx.blah.discord.handle.obj.IChannel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HungerGames {
	
	private final HGDistrict[] districts = { new HGDistrict(1), new HGDistrict(2), new HGDistrict(3), new HGDistrict(4),
			new HGDistrict(5), new HGDistrict(6), new HGDistrict(7), new HGDistrict(8),
			new HGDistrict(9), new HGDistrict(10), new HGDistrict(11), new HGDistrict(12) };
	private final Map<String, ExtraAction> extras = new HashMap<>();
	
	private final Auriel auriel;
	private final Config data;
	
	private final ConditionalWeightedRandomArrayList<HGAction> startEntries = new ConditionalWeightedRandomArrayList<>(1);
	private final ConditionalWeightedRandomArrayList<HGAction> standardEntries = new ConditionalWeightedRandomArrayList<>(5);
	private final ConditionalWeightedRandomArrayList<HGAction> feastEntries = new ConditionalWeightedRandomArrayList<>(1);
	
	private List<HGPlayer> alive = new ArrayList<>(24);
	private List<HGPlayer> needsAction = new ArrayList<>(24);
	private int day = 0;
	
	public HungerGames(Auriel auriel) {
		this.auriel = auriel;
		this.data = new Config(new File("games" + File.separator + "hg", "actions.yml"));
		data.load(config -> {
			ConfigSection startSection = config.getChild("start");
			if(startSection != null) startSection.getChildren().forEach(section -> startEntries.add(new HGAction(this, section)));
			ConfigSection standardSection = config.getChild("standard");
			if(standardSection != null) standardSection.getChildren().forEach(section -> standardEntries.add(new HGAction(this, section)));
			ConfigSection feastSection = config.getChild("feast");
			if(feastSection != null) feastSection.getChildren().forEach(section -> feastEntries.add(new HGAction(this, section)));
		});
	}
	
	public int getNeedsAction() {
		return needsAction.size();
	}
	
	public void kill(HGPlayer player) {
		alive.remove(player);
	}
	
	public HGDistrict getDistrict(int district) {
		return districts[district - 1];
	}
	
	public void saveData() {
		data.save();
	}
	
	public void registerExtraAction(String key, ExtraAction action) {
		extras.put(key, action);
	}
	
	public void handleExtraAction(String key, HGAction action, String extra) {
		ExtraAction extraAction = extras.get(key);
		if(extraAction != null) extraAction.handle(action, extra);
	}
	
	public void addStartEntry(HGAction entry) {
		startEntries.add(entry);
		entry.save(data.getChild("start", true));
	}
	
	public void addStandardEntry(HGAction entry) {
		standardEntries.add(entry);
		entry.save(data.getChild("standard", true));
	}
	
	public void addFeastEntry(HGAction entry) {
		feastEntries.add(entry);
		entry.save(data.getChild("feast", true));
	}
	
	private boolean stop = false;
	private int checkInterval = 10;
	
	public void start(IChannel channel) {
		alive.addAll(HGPlayer.getAllPlayersWithDistricts());
		Threads.start(() -> {
			while(alive.size() > 1 && !stop) {
				day++;
				if(day == 1) nextDay(channel, startEntries);
				else if(day % 4 == 0) {
					for(int i = 0; i < 10 * checkInterval; i++) {
						if(stop) break;
						Threads.sleep(1000 / checkInterval);
					}
					if(stop) break;
					nextDay(channel, feastEntries);
				}
				else {
					for(int i = 0; i < 10 * checkInterval; i++) {
						if(stop) break;
						Threads.sleep(1000 / checkInterval);
					}
					if(stop) break;
					nextDay(channel, standardEntries);
				}
			}
			if(stop) {
				channel.sendMessage("Stopped.");
				reset();
				return;
			}
			Threads.sleep(750);
			channel.sendMessage(alive.get(0).getUser().mention() + " wins!");
			reset();
		}, "Game thread - Hunger Games");
	}
	
	public void stop() {
		stop = true;
	}
	
	public void nextDay(IChannel channel, ConditionalWeightedRandomArrayList<HGAction> entries) {
		needsAction.clear();
		needsAction.addAll(alive);
		channel.sendMessage("**Day " + day + "**");
		while(needsAction.size() > 0) {
			Threads.sleep(750);
			HGPlayer player = Randoms.getRandomAndRemove(needsAction);
			HGAction entry = entries.random(player);
			entry.play(channel, player, getRandomUsers(needsAction, entry.getExtraPlayersRequired()));
		}
	}
	
	public void reset() {
		stop = false;
		alive.clear();
		needsAction.clear();
		day = 0;
		HGPlayer.getAllPlayersWithDistricts().forEach(HGPlayer::reset);
	}
	
	private List<HGPlayer> getRandomUsers(List<HGPlayer> users, int amount) {
		List<HGPlayer> randomUsers = new ArrayList<>(amount);
		while(amount > 0) {
			randomUsers.add(Randoms.getRandomAndRemove(users));
			amount--;
		}
		return randomUsers;
	}
	
}
