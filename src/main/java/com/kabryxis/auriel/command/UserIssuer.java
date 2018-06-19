package com.kabryxis.auriel.command;

import com.kabryxis.auriel.user.UserData;
import com.kabryxis.kabutils.command.CommandIssuer;

import sx.blah.discord.handle.obj.IMessage;

public class UserIssuer implements CommandIssuer {
	
	private final IMessage msg;
	
	public UserIssuer(IMessage msg) {
		this.msg = msg;
	}
	
	public IMessage getMsg() {
		return msg;
	}
	
	public boolean hasPermission(String permission) {
		return UserData.getData(msg.getAuthor()).hasPermission(permission);
	}
	
	public void sendMessage(String message) {
		msg.reply(message);
	}
	
}
