package com.kabryxis.auriel.game;

public class InventoryAction implements ExtraAction {
	
	@Override
	public void handle(HGAction action, String extra) {
		String[] args = extra.split("-");
		String a = args[0];
		String item = args[1];
		if(a.equalsIgnoreCase("add")) action.addConsumer(player -> {
			System.out.println("giving " + player.getUser().getName() + " " + item);
			player.addItem(item);
		});
		else if(a.equalsIgnoreCase("remove")) action.addConsumer(player -> {
			System.out.println("removing " + player.getUser().getName() + " " + item);
			player.removeItem(item);
		});
		else if(a.equalsIgnoreCase("requires")) action.addPredicate(player -> {
			boolean b = player.hasItem(item);
			System.out.println(player.getUser().getName() + " has " + item + ": " + b);
			return b;
		});
	}
	
}
