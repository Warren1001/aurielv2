package com.kabryxis.auriel.command;

import com.kabryxis.auriel.Auriel;
import com.kabryxis.auriel.user.UserData;
import com.kabryxis.kabutils.command.Com;
import com.kabryxis.kabutils.command.CommandIssuer;

public class CommandListener {
	
	private final Auriel auriel;
	
	public CommandListener(Auriel auriel) {
		this.auriel = auriel;
	}
	// TODO test if making an issuer vague (setting to CommandIssuer specifically) enables several types of CommandIssuers to be used (such as ConsoleIssuer and UserIssuer) in the same method
	@Com(aliases = { "stop" }, permission = "auriel.master")
	public boolean onStop(CommandIssuer issuer, String alias, String[] args) {
		issuer.sendMessage("Bye bye :)");
		auriel.close();
		return true;
	}
	
	@Com(aliases = { "hi" })
	public boolean onHi(UserIssuer issuer, String alias, String[] args) {
		issuer.sendMessage("hi");
		return true;
	}
	
	@Com(aliases = { "perm" })
	public boolean onPerm(UserIssuer issuer, String alias, String[] args) {
		UserData userData = UserData.getData(issuer.getMsg().getAuthor());
		userData.givePermission("auriel.*");
		userData.save();
		return true;
	}
	
}
