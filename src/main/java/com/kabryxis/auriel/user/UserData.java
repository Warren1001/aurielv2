package com.kabryxis.auriel.user;

import com.kabryxis.kabutils.data.file.yaml.Config;
import sx.blah.discord.handle.obj.IUser;

import java.io.File;
import java.util.*;

public class UserData {
	
	private static final Map<IUser, UserData> DATA = new HashMap<>();
	
	public static UserData getData(IUser user) {
		return DATA.computeIfAbsent(user, UserData::new);
	}
	
	public static void saveAll() {
		DATA.values().forEach(UserData::save);
	}
	
	private final IUser user;
	private final Config data;
	
	private List<String> grantedPermissions, negatedPermissions;
	
	private boolean dataModified = false;
	
	private UserData(IUser user) {
		this.user = user;
		this.data = new Config("users" + File.separator + user.getStringID() + ".yml");
		reloadData();
	}
	
	public void reloadData() {
		data.loadSync();
		grantedPermissions = new ArrayList<>();
		negatedPermissions = new ArrayList<>();
		for(String permission : data.getList("permissions", String.class)) {
			if(permission.startsWith("-")) negatedPermissions.add(permission.substring(1, permission.length()));
			else grantedPermissions.add(permission);
		}
	}
	
	public void save() {
		if(dataModified) {
			List<String> list = new ArrayList<>();
			list.addAll(grantedPermissions);
			list.addAll(negatedPermissions);
			data.set("permissions", list);
			data.save();
		}
	}
	
	public boolean hasPermission(String permission) {
		return !negatedPermissions.contains(permission) && (grantedPermissions.contains(permission) || hasWildcardPermission(permission));
	}
	
	private boolean hasWildcardPermission(String permission) {
		for(int index = permission.length() - 1; index >= 0; index--) {
			if(permission.charAt(index) == '.' && grantedPermissions.contains(permission.substring(0, index + 1) + "*")) return true;
		}
		return false;
	}
	
	public void givePermission(String permission) {
		grantedPermissions.add(permission);
		negatedPermissions.remove(permission);
		dataModified = true;
	}
	
}
