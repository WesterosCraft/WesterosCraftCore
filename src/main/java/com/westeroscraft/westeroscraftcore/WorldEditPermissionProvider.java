package com.westeroscraft.westeroscraftcore;

import com.sk89q.worldedit.forge.ForgePermissionsProvider;

import net.luckperms.api.cacheddata.CachedPermissionData;
import net.minecraft.server.level.ServerPlayer;

public class WorldEditPermissionProvider implements ForgePermissionsProvider {
    public WorldEditPermissionProvider() {
    }

    @Override
    public boolean hasPermission(ServerPlayer player, String permission) {
		CachedPermissionData perms = WesterosCraftCore.getLuckPermsAPI().getPlayerAdapter(ServerPlayer.class).getPermissionData(player);
		boolean rslt = perms.checkPermission(permission).asBoolean();
    	//WesterosCraftCore.log.info("hasPermission(" + player.getDisplayName().getString() + ", " + permission + ")=" + rslt);
		return rslt;
    }

    @Override
    public void registerPermission(String permission) {
    	//WesterosCraftCore.log.info("registerPermission(" + permission + ")");
    }    		
}
