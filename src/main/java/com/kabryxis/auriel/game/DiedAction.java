package com.kabryxis.auriel.game;

public class DiedAction implements HGExtraAction {
	
	@Override
	public void load(HGEntry entry, String extra) {}
	
	@Override
	public void handle(HGPlayer player, String extra) {
		player.getGame().kill(player);
	}
	
}
