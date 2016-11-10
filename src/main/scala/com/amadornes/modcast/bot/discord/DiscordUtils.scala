package com.amadornes.modcast.bot.discord

import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.handle.obj.{IChannel, IUser}

/**
  * Created by rewbycraft on 11/4/16.
  */
object DiscordUtils {
	private lazy val userMentionRegex = "<@([0-9]+)>".r
	private lazy val channelMentionRegex = "<#([0-9]+)>".r
	private lazy val userRegex = "@(.*)".r
	
	def isUserMention(str: String): Boolean = userMentionRegex.findFirstIn(str).isDefined
	
	def isChannelMention(str: String): Boolean = channelMentionRegex.findFirstIn(str).isDefined
	
	def parseUserMention(str: String)(implicit client: IDiscordClient): IUser =
		if (client != null) {
			val userMentionRegex(id) = str
			client.getUserByID(id)
		} else
			null
	
	def parseChannelMention(str: String)(implicit client: IDiscordClient): IChannel =
		if (client != null) {
			val channelMentionRegex(id) = str
			client.getChannelByID(id)
		} else
			null
}
