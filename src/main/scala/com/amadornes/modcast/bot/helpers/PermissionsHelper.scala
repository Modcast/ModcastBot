package com.amadornes.modcast.bot.helpers

import com.amadornes.modcast.bot.database.{DB, Permission, UserPermission}
import sx.blah.discord.handle.obj.IUser

/**
  * Created by rewbycraft on 11/8/16.
  */
object PermissionsHelper {
	/**
	  * Update the user permission level
	  *
	  * @param user       user
	  * @param permission new level
	  */
	def setUserPermissionLevel(user: IUser, permission: Permission): Unit = {
		DB.save(
			DB.query[UserPermission]
				.whereEqual("user", user.getID).fetchOne()
				.getOrElse(
					UserPermission(user = user.getID, permission = Permission.NONE.getLevel)
				)
				.copy(permission = permission.getLevel)
		)
	}
	
	/**
	  * Get the user permission level
	  *
	  * @param user User
	  * @return Permission level
	  */
	def getUserPermissionLevel(user: IUser): Permission = Permission.withLevel(DB.query[UserPermission]
		.whereEqual("user", user.getID).fetchOne()
		.map(_.permission).getOrElse(Permission.NONE.getLevel))
	
	/**
	  * Same as setUserPermissionLevel but this will not downgrade your permissions.
	  *
	  * @param user       user
	  * @param permission new level
	  */
	def upgradeUserPermissionLevel(user: IUser, permission: Permission): Unit =
		if (getUserPermissionLevel(user).getLevel < permission.getLevel)
			setUserPermissionLevel(user, permission)
}
