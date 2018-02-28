package com.kabryxis.auriel.command;

import com.kabryxis.auriel.user.UserData;
import com.kabryxis.kabutils.cache.Cache;
import com.kabryxis.kabutils.command.CommandIssuer;

import sx.blah.discord.handle.obj.IMessage;

public class UserIssuer implements CommandIssuer {
	
	private IMessage msg;
	
	public void reuse(IMessage msg) {
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
	
	@Override
	public void cache() {
		msg = null;
		Cache.cache(this);
	}
}
