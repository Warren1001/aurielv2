package com.kabryxis.auriel.user;

import com.kabryxis.kabutils.data.file.yaml.Config;
import sx.blah.discord.handle.obj.IUser;

import java.io.File;
import java.util.*;

public class UserData {
	
	private static final Map<IUser, UserData> userDatas = new HashMap<>();
	
	public static UserData getData(IUser user) {
		return userDatas.computeIfAbsent(user, UserData::new);
	}
	
	public static void saveAll() {
		userDatas.values().forEach(UserData::save);
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
		List<String> permissionsList = data.getList("permissions", String.class);
		if(permissionsList != null) {
			for(String permission : permissionsList) {
				if(permission.startsWith("-")) negatedPermissions.add(permission.substring(1, permission.length()));
				else grantedPermissions.add(permission);
			}
		}
	}
	
	public void save() {
		if(dataModified) {
			List<String> list = new ArrayList<>();
			list.addAll(grantedPermissions);
			list.addAll(negatedPermissions);
			data.set("permissions", list);
			data.save();
			dataModified = false;
		}
	}
	
	public boolean hasPermission(String permission) {
		return permission.isEmpty() || (!negatedPermissions.contains(permission) && !hasWildcardPermission(negatedPermissions, permission)
				&& (grantedPermissions.contains(permission) || hasWildcardPermission(grantedPermissions, permission)));
	}
	
	private boolean hasWildcardPermission(List<String> permissions, String permission) {
		for(int index = permission.length() - 1; index >= 0; index--) {
			if(permission.charAt(index) == '.' && permissions.contains(permission.substring(0, index + 1) + "*")) return true;
		}
		return false;
	}
	
	public void givePermission(String permission) {
		grantedPermissions.add(permission);
		negatedPermissions.remove(permission);
		dataModified = true;
	}
	
}
