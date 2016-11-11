package com.amadornes.modcast.bot.discord.commands

import com.amadornes.modcast.bot.database.{DB, Permission, UserPermission}
import com.amadornes.modcast.bot.discord.{AbstractCommand, TCommandOptions}
import com.amadornes.modcast.bot.helpers.{GuestHelper, PermissionsHelper}
import sx.blah.discord.handle.obj.IUser

/**
  * Created by rewbycraft on 11/10/16.
  */
class UserCommand extends AbstractCommand[UserCommand.Options] {
	override def handle: Receive = {
		case c@Execute(channel, user, _, arguments) =>
			arguments.command match {
				case "list" =>
					user.getOrCreatePMChannel().sendMessage("Known users:\n" +
						DB.query[UserPermission].fetch().map(p => s"${discordClient.getUserByID(p.user)}\t- ${Permission.withLevel(p.permission)}\n").mkString
					)
					if (!channel.isPrivate)
						c.respond("I've sent you a private message!")
				
				case "set" =>
					PermissionsHelper.setUserPermissionLevel(arguments.user, arguments.permission)
					
					if (GuestHelper.isGuest(arguments.user, channel.getGuild) && arguments.permission.getLevel < Permission.GUEST.getLevel)
						GuestHelper.delAllEpisodes(arguments.user, channel.getGuild)
					
					c.respond("Okay!")
			}
	}
	
	override def name: Array[String] = Array("user")
	
	override def parser(command: String): OptionParser = new OptionParser(command) {
		note("Manipulate the user database.")
		
		cmd("list").text("List known users").action((_, c) => c.copy(command = "list"))
		
		cmd("set").text("Set user permissions").action((_, c) => c.copy(command = "set")).children(
			opt[IUser]('u', "user").text("User to modify").action((v, c) => c.copy(user = v)),
			opt[Permission]('p', "permission").text("New permission level").action((v, c) => c.copy(permission = v))
		)
	}
	
	override def permissionLevel: Permission = Permission.ADMIN
}

object UserCommand {
	
	case class Options(command: String, user: IUser, permission: Permission) extends TCommandOptions {
		def this() = this(null, null, null)
	}
	
}
