package com.amadornes.modcast.bot.discord.commands

import com.amadornes.modcast.bot.database.Permission
import com.amadornes.modcast.bot.discord.{AbstractCommand, TCommandOptions}
import com.amadornes.modcast.bot.helpers.PermissionsHelper

/**
  * Created by rewbycraft on 11/9/16.
  */
class WhoamiCommand extends AbstractCommand[WhoamiCommand.Options] {
	override def handle: Receive = {
		case c@Execute(channel, user, _, _) =>
			c.respond(f"You are ${user.getDisplayName(channel.getGuild)}. (Username: ${user.getName}) (ID: ${user.getID}) (Guild: ${if (channel.getGuild != null) channel.getGuild.getID else null}) (Channel: ${channel.getID}) (Permission: ${PermissionsHelper.getUserPermissionLevel(user).toString})")
		
	}
	
	override def name: Array[String] = Array("whoami")
	
	override def parser(command: String): OptionParser = new OptionParser(command) {
		note("Displays some info about you.")
	}
	
	override def permissionLevel: Permission = Permission.NONE
}

object WhoamiCommand {
	
	case class Options() extends TCommandOptions
	
}
