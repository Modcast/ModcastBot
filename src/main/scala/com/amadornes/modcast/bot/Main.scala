package com.amadornes.modcast.bot

import com.amadornes.modcast.bot.database.{DB, Permission, UserPermission}
import com.amadornes.modcast.bot.discord.DiscordMessages
import com.amadornes.modcast.bot.servers.http.RTMPControlServer
import grizzled.slf4j.Logging

import scala.collection.JavaConversions._

/**
  * Created by rewbycraft on 11/2/16.
  */
object Main extends App with Logging {
	info("Bot is now loading!")
	
	info("Starting discord...")
	Actors.discord.actor ! DiscordMessages.Startup()
	info("Forcing admins...")
	Configuration.config.getStringList("users.forcedAdmins").foreach(admin =>
		DB.save(DB.query[UserPermission].whereEqual("user", admin).fetchOne().getOrElse(UserPermission(user = admin, permission = Permission.NONE.getLevel)).copy(permission = Permission.ADMIN.getLevel))
	)
	info("Starting RTMP control...")
	RTMPControlServer.start()
	
	info("Booping MCWhitelistServer...")
	Actors.servers.MCWhitelistServer ! "boop"

	info("We are online!")
}
