package com.amadornes.modcast.bot.discord.commands

import com.amadornes.modcast.bot.database.Permission
import com.amadornes.modcast.bot.discord.{AbstractCommand, TCommandOptions}

/**
  * Created by rewbycraft on 11/10/16.
  */
class UserCommand extends AbstractCommand[UserCommand.Options] {
	override def handle: Receive = {
		case c@Execute(user, channel, _, arguments) =>
			//TODO
	}
	
	override def name: Array[String] = Array("user")
	
	override def parser(command: String): OptionParser = new OptionParser(command) {
		//TODO
	}
	
	override def permissionLevel: Permission = Permission.ADMIN
}

object UserCommand {
	case class Options() extends TCommandOptions
}
