package com.amadornes.modcast.bot.helpers

import com.amadornes.modcast.bot.database.{DB, Permission, StreamKey}
import sx.blah.discord.handle.obj.IUser

/**
  * Created by rewbycraft on 11/8/16.
  */
object StreamKeyHelper {
	def getUserStreamKey(user: IUser): String = {
		if (PermissionsHelper.getUserPermissionLevel(user).getLevel < Permission.GUEST.getLevel)
			return null
		
		val key = DB.query[StreamKey].whereEqual("user", user.getID).fetchOne()
		if (key.isDefined)
			return key.get.key
		
		resetUserStreamKey(user)
	}
	
	def resetUserStreamKey(user: IUser): String = {
		if (PermissionsHelper.getUserPermissionLevel(user).getLevel < Permission.GUEST.getLevel)
			return null
		
		val key = DB.query[StreamKey]
			.whereEqual("user", user.getID).fetchOne()
			.getOrElse(StreamKey(user = user.getID, key = null, name = user.getName))
			.copy(key = java.util.UUID.randomUUID().toString)
		
		DB.save(key)
		
		key.key
	}
	
	def removeUserStreamKey(user: IUser): Unit = {
		DB.query[StreamKey].whereEqual("user", user.getID).fetch().foreach(DB.delete(_))
	}

	def getStreamKeyForName(name: String): Option[String] = DB.query[StreamKey].whereEqual("name", name).fetchOne().map(_.key)
}
