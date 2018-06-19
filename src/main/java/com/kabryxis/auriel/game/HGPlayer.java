package com.kabryxis.auriel.game;

import com.kabryxis.auriel.Auriel;
import com.kabryxis.kabutils.data.Data;
import com.kabryxis.kabutils.data.file.FileEndingFilter;
import com.kabryxis.kabutils.data.file.yaml.Config;
import sx.blah.discord.handle.obj.IUser;

import java.io.File;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class HGPlayer implements Predicate<Object> {
	
	private static final Map<IUser, HGPlayer> players = new HashMap<>();
	
	public static HGPlayer getPlayer(HungerGames game, IUser user) {
		return players.computeIfAbsent(user, u -> new HGPlayer(game, u));
	}
	
	public static Set<HGPlayer> getAllPlayersWithDistricts() {
		return players.values().stream().filter(HGPlayer::isInDistrict).collect(Collectors.toSet());
	}
	
	public static void loadExistingPlayers(Auriel auriel) {
		for(File file : Objects.requireNonNull(new File("games" + File.separator + "hg" + File.separator + "player").listFiles(new FileEndingFilter(".yml")))) {
			getPlayer(auriel.getGame(), auriel.getClient().getUserByID(Long.parseLong(file.getName().split("\\.")[0])));
		}
	}
	
	public static void saveAll() {
		Data.queue(() -> players.values().forEach(player -> player.data.save()));
	}
	
	private final Set<String> inventory = new HashSet<>();
	
	private final HungerGames game;
	private final IUser user;
	private final Config data;
	
	private HGDistrict district;
	private boolean wantsTag;
	
	private HGPlayer districtTeammate;
	private int kills;
	
	private HGPlayer(HungerGames game, IUser user) {
		this.game = game;
		this.user = user;
		this.data = new Config(new File("games" + File.separator + "hg" + File.separator + "player", user.getLongID() + ".yml"));
		data.load(config -> {
			Integer district = config.get("district", Integer.class);
			if(district != null) this.district = game.getDistrict(district);
			Boolean wantsTag = config.get("wants-tag", Boolean.class);
			if(wantsTag != null) this.wantsTag = wantsTag;
		});
	}
	
	public IUser getUser() {
		return user;
	}
	
	public void setDistrict(HGDistrict district) {
		if(this.district != district) {
			this.district = district;
			data.set("district", district.getNumber());
			data.save();
		}
	}
	
	public boolean isInDistrict() {
		return district != null;
	}
	
	public void leaveDistrict() {
		if(isInDistrict()) {
			district.removePlayer(this);
			district = null;
			data.remove("district");
		}
	}
	
	public void wantsTag(boolean wantsTag) {
		if(this.wantsTag != wantsTag) {
			this.wantsTag = wantsTag;
			data.set("wants-tag", wantsTag);
			data.save();
		}
	}
	
	public void incrementKills(int kills) {
		this.kills += kills;
	}
	
	public void setKills(int kills) {
		this.kills = kills;
	}
	
	public void reset() {
		inventory.clear();
		setKills(0);
	}
	
	public void setDistrictTeammate(HGPlayer player) {
		this.districtTeammate = player;
	}
	
	public boolean hasItem(String item) {
		return inventory.contains(item);
	}
	
	public void addItem(String item) {
		if(!hasItem(item)) inventory.add(item);
	}
	
	public void removeItem(String item) {
		inventory.remove(item);
	}
	
	public boolean isTeammate(HGPlayer player) {
		return districtTeammate.getUser() == player.getUser();
	}
	
	public String tag() {
		return wantsTag ? user.mention() : user.getName();
	}
	
	@Override
	public boolean test(Object o) {
		if(o instanceof HGAction) {
			HGAction action = (HGAction)o;
			if(game.getNeedsAction() < action.getExtraPlayersRequired()) return false;
			boolean b = action.test(this);
			//System.out.println("test: " + b);
			return b;
		}
		return false;
	}
}
