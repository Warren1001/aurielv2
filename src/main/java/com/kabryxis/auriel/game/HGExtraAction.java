package com.kabryxis.auriel.game;

public interface HGExtraAction {
	
	void load(HGEntry entry, String extra);
	
	void handle(HGPlayer player, String extra);
	
}
