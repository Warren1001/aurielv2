package com.kabryxis.auriel.game;

import com.kabryxis.auriel.Auriel;
import com.kabryxis.kabutils.concurrent.Threads;
import com.kabryxis.kabutils.data.Data;
import com.kabryxis.kabutils.data.file.FileEndingFilter;
import com.kabryxis.kabutils.data.file.yaml.Config;
import com.kabryxis.kabutils.random.Randoms;
import com.kabryxis.kabutils.random.weighted.conditional.ConditionalWeightedRandomArrayList;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.io.File;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class HungerGames {
	
	private final Map<IUser, HGPlayer> players = new HashMap<>();
	
	public HGPlayer getPlayer(IUser user) {
		return players.computeIfAbsent(user, u -> new HGPlayer(this, u));
	}
	
	public Set<HGPlayer> getAllPlayersWithDistricts() {
		return players.values().stream().filter(HGPlayer::isInDistrict).collect(Collectors.toSet());
	}
	
	public void savePlayerData() {
		Data.queue(() -> players.values().forEach(player -> player.getData().saveSync()));
	}
	
	private final ConditionalWeightedRandomArrayList<HGEntry> entries = new ConditionalWeightedRandomArrayList<>(1);
	private final Predicate<Object> startPredicate = o -> o instanceof HGEntry && ((HGEntry)o).getType() == HGEntryType.START;
	private final Predicate<Object> standardPredicate = o -> o instanceof HGEntry && ((HGEntry)o).getType() == HGEntryType.STANDARD;
	private final Predicate<Object> feastPredicate = o -> o instanceof HGEntry && ((HGEntry)o).getType() == HGEntryType.FEAST;
	private final HGDistrict[] districts = { new HGDistrict(1), new HGDistrict(2), new HGDistrict(3), new HGDistrict(4),
			new HGDistrict(5), new HGDistrict(6), new HGDistrict(7), new HGDistrict(8),
			new HGDistrict(9), new HGDistrict(10), new HGDistrict(11), new HGDistrict(12) };
	private final Map<String, HGExtraAction> extraActions = new HashMap<>();
	
	private final Auriel auriel;
	private final Config data;
	
	private List<HGPlayer> alive = new ArrayList<>(24);
	private List<HGPlayer> needsAction = new ArrayList<>(24);
	private int day = 0;
	
	private boolean stop = false;
	private int checkInterval = 10;
	
	public HungerGames(Auriel auriel) {
		this.auriel = auriel;
		this.data = new Config(new File("games" + File.separator + "hg", "actions.yml"));
		registerExtraAction("inv", new InventoryAction());
		registerExtraAction("dies", new DiedAction());
		registerExtraAction("kills", new KillAction());
		data.load(config -> config.getChildren().forEach(section -> entries.add(new HGEntry(this, section.get("action", String.class),
				HGEntryType.valueOf(section.get("type", String.class).toUpperCase()), section.get("weight", Integer.class)))));
		for(File file : Objects.requireNonNull(new File("games" + File.separator + "hg" + File.separator + "player").listFiles(new FileEndingFilter(".yml")))) {
			getPlayer(auriel.getClient().getUserByID(Long.parseLong(file.getName().split("\\.")[0])));
		}
	}
	
	public int getNeedsAction() {
		return needsAction.size();
	}
	
	public void kill(HGPlayer player) {
		needsAction.remove(player);
		alive.remove(player);
	}
	
	public HGDistrict getDistrict(int district) {
		return districts[district - 1];
	}
	
	public void saveData() {
		data.save();
	}
	
	public void registerExtraAction(String key, HGExtraAction action) {
		extraActions.put(key, action);
	}
	
	public void handleExtraAction(String key, HGPlayer player, String extra) {
		HGExtraAction extraAction = extraActions.get(key);
		if(extraAction != null) extraAction.handle(player, extra);
	}
	
	public void loadExtraAction(String key, HGEntry entry, String extra) {
		HGExtraAction extraAction = extraActions.get(key);
		if(extraAction != null) extraAction.load(entry, extra);
	}
	
	public void addEntry(HGEntry entry) {
		entries.add(entry);
		entry.save(data.getChild(String.valueOf(data.getKeys(false).size()), true));
	}
	
	public void start(IChannel channel) {
		alive.addAll(getAllPlayersWithDistricts());
		Threads.start(() -> {
			while(alive.size() > 1 && !stop) {
				day++;
				if(day == 1) {
					if(nextDay(channel, startPredicate)) break;
				}
				else if(day % 4 == 0) {
					for(int i = 0; i < 10 * checkInterval; i++) {
						if(stop) break;
						Threads.sleep(1000 / checkInterval);
					}
					if(stop) break;
					if(nextDay(channel, feastPredicate)) break;
				}
				else {
					for(int i = 0; i < 10 * checkInterval; i++) {
						if(stop) break;
						Threads.sleep(1000 / checkInterval);
					}
					if(stop) break;
					if(nextDay(channel, standardPredicate)) break;
				}
			}
			if(stop) {
				channel.sendMessage("Stopped.");
				reset();
				return;
			}
			Threads.sleep(750);
			channel.sendMessage(alive.get(0).tag() + " wins!");
			reset();
		}, "Game thread - Hunger Games");
	}
	
	public void stop() {
		stop = true;
	}
	
	public boolean nextDay(IChannel channel, Predicate<Object> predicate) {
		needsAction.clear();
		needsAction.addAll(alive);
		channel.sendMessage("__**\\_\\_\\_Day " + day + "\\_\\_\\_**__");
		while(getNeedsAction() > 0) {
			if(alive.size() == 1) return true;
			Threads.sleep(1250);
			HGPlayer player = Randoms.getRandomAndRemove(needsAction);
			HGEntry entry = entries.random(player, predicate);
			channel.sendMessage(entry.getMessage(player, needsAction));
			if(entry.getType() == HGEntryType.FEAST) needsAction.add(player);
		}
		return false;
	}
	
	public void reset() {
		stop = false;
		alive.clear();
		needsAction.clear();
		day = 0;
		getAllPlayersWithDistricts().forEach(HGPlayer::reset);
	}
	
}
