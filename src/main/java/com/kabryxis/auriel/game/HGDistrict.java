package com.kabryxis.auriel.game;

import sx.blah.discord.handle.obj.IMessage;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class HGDistrict {
	
	public static int MAX_DISTRICT_MEMBERS = 2;
	
	private final Set<HGPlayer> districtMembers = new HashSet<>(MAX_DISTRICT_MEMBERS);
	
	private final int district;
	
	public HGDistrict(int district) {
		this.district = district;
	}
	
	public int getNumber() {
		return district;
	}
	
	public void addPlayer(IMessage msg, HGPlayer player) {
		if(!districtMembers.contains(player)) {
			if(districtMembers.size() == MAX_DISTRICT_MEMBERS) {
				msg.reply("District " + district + " is full, choose another or ask " + getPlayerList() + " to join another District.");
				return;
			}
			player.leaveDistrict();
			player.setDistrict(this);
			districtMembers.add(player);
			msg.reply("You've been added to District " + district + ".");
		}
	}
	
	public void removePlayer(HGPlayer player) {
		districtMembers.remove(player);
	}
	
	private String getPlayerList() {
		StringBuilder builder = new StringBuilder();
		if(districtMembers.size() > 2) {
			boolean first = true;
			for(Iterator<HGPlayer> it = districtMembers.iterator(); it.hasNext();) {
				HGPlayer player = it.next();
				if(first) first = false;
				else {
					builder.append(", ");
					if(!it.hasNext()) builder.append(" or ");
				}
				builder.append(player.getUser().mention());
			}
		}
		else {
			for(Iterator<HGPlayer> it = districtMembers.iterator(); it.hasNext();) {
				HGPlayer player = it.next();
				builder.append(player.getUser().mention());
				if(it.hasNext()) builder.append(" or ");
			}
		}
		return builder.toString();
	}
	
}
