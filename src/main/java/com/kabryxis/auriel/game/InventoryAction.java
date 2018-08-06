package com.kabryxis.auriel.game;

public class InventoryAction implements HGExtraAction {
	
	@Override
	public void load(HGEntry entry, String extra) {
		String[] args = extra.split("-");
		//System.out.println(args[0]);
		if(args[0].equalsIgnoreCase("requires")) {
			entry.addPredicate(player -> {
				boolean b = player.hasItem(args[1]);
				//System.out.println(player.getUser().getName() + " has " + args[1] + ": " + b);
				return b;
			});
		}
	}
	
	@Override
	public void handle(HGPlayer player, String extra) {
		String[] args = extra.split("-");
		String a = args[0];
		String item = args[1];
		if(a.equalsIgnoreCase("add")) {
			player.addItem(item);
			//System.out.println("giving " + player.getUser().getName() + " a " + item);
		}
		else if(a.equalsIgnoreCase("remove")) {
			player.removeItem(item);
			//System.out.println("taking " + item + " from " + player.getUser().getName());
		}
	}
	
}
