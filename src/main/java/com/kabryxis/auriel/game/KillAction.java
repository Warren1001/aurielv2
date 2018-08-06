package com.kabryxis.auriel.game;

public class KillAction implements HGExtraAction {
	
	@Override
	public void load(HGEntry entry, String extra) {}
	
	@Override
	public void handle(HGPlayer player, String extra) {
		player.incrementKills(Integer.parseInt(extra));
	}
	
}
