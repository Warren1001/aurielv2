package com.kabryxis.auriel.game;

import com.kabryxis.kabutils.data.file.yaml.Config;
import sx.blah.discord.handle.obj.IUser;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class HGPlayer implements Predicate<Object> {
	
	private final Set<String> inventory = new HashSet<>();
	
	private final HungerGames game;
	private final IUser user;
	private final Config data;
	
	private HGDistrict district;
	private boolean wantsTag;
	
	private HGPlayer districtTeammate;
	private int kills;
	
	HGPlayer(HungerGames game, IUser user) {
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
	
	public HungerGames getGame() {
		return game;
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
		return wantsTag ? user.mention() : "**" + user.getName() + "**";
	}
	
	@Override
	public boolean test(Object o) {
		if(o instanceof HGEntry) {
			HGEntry entry = (HGEntry)o;
			if(game.getNeedsAction() < entry.getRequiredExtraPlayers()) return false;
			return entry.test(this);
		}
		return false;
	}
	
	public Config getData() {
		return data;
	}
	
}
