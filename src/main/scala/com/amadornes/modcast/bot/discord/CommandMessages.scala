package com.amadornes.modcast.bot.discord

import com.amadornes.modcast.bot.database.Permission
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.handle.obj.{IChannel, IUser}

/**
  * Created by rewbycraft on 11/2/16.
  */
object CommandMessages {
	case class Startup(client: IDiscordClient)
	case class Register(name: String, level: Permission)
	case class HandleCommand(channel: IChannel, user: IUser, command: String, arguments: Array[String]) {
		def respond(message: String) = {
			if (channel.isPrivate)
				channel.sendMessage(message)
			else
				channel.sendMessage(f"${user.mention(true)} $message")
		}
	}
	
	case class DisplayHelp(channel: IChannel, command: String)
}
