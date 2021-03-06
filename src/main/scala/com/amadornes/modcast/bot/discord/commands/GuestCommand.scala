package com.amadornes.modcast.bot.discord.commands

import com.amadornes.modcast.bot.Configuration
import com.amadornes.modcast.bot.database._
import com.amadornes.modcast.bot.discord.{AbstractCommand, TCommandOptions}
import com.amadornes.modcast.bot.helpers.{GuestHelper, PermissionsHelper, StreamKeyHelper}
import sx.blah.discord.handle.obj.IUser

/**
  * Created by rewbycraft on 11/2/16.
  */
class GuestCommand extends AbstractCommand[GuestCommand.Options] {
	override def handle: Receive = {
		case c@Execute(channel, _, _, arguments: GuestCommand.Options) =>
			arguments.command match {
				case "add" =>
					if (DB.query[EpisodeGuest]
						.whereEqual("guild", channel.getGuild.getID)
						.whereEqual("user", arguments.user.getID)
						.whereEqual("episode", arguments.episode).exists())
						c.respond("User is already featuring in this episode!")
					else {
						
						GuestHelper.addGuest(arguments.user, arguments.episode, channel.getGuild)
						
						sendWelcomeMessage(arguments.user, arguments.episode)
						
						c.respond("User added to episode.")
					}
				
				case "del" =>
					if (!DB.query[EpisodeGuest]
						.whereEqual("guild", channel.getGuild.getID)
						.whereEqual("user", arguments.user.getID)
						.whereEqual("episode", arguments.episode).exists())
						c.respond("User is not in this episode!")
					else {
						GuestHelper.delGuest(arguments.user, arguments.episode, channel.getGuild)
						c.respond("User removed from episode.")
					}
				
				case "purge" =>
					GuestHelper.delAllEpisodes(arguments.user, channel.getGuild)
					
					c.respond("User removed from all episodes.")
				
				case "list" =>
					var query = DB.query[EpisodeGuest].whereEqual("guild", channel.getGuild.getID)
					if (arguments.episode != -1)
						query = query.whereEqual("episode", arguments.episode)
					
					val guests = query.fetch().toList
					var message = "Guest list:\n"
					
					for (episode: Int <- guests.map(_.episode).sorted) {
						message += s" - Episode $episode:\n"
						for (guest: EpisodeGuest <- guests.filter(_.episode == episode))
							message += s"    - ${channel.getClient.getUserByID(guest.user).getName}\n"
					}
					
					c.user.getOrCreatePMChannel().sendMessage(message)
					
					if (!channel.isPrivate)
						c.respond("I have sent you a private message.")
			}
		
	}
	
	override def name: Array[String] = Array("guest")
	
	override def parser(command: String): OptionParser = new OptionParser(command) {
		cmd("add").action((_, c) => c.copy(command = "add")).text("Add user to episode").children(
			opt[Int]('e', "episode").action((x, c) => c.copy(episode = x)).text("Episode to add user to."),
			opt[IUser]('u', "user").action((u, c) => c.copy(user = u)).text("User to add.")
		)
		
		cmd("delete").abbr("del").abbr("remove").abbr("rem").action((_, c) => c.copy(command = "del")).text("Remove user from episode").children(
			opt[Int]('e', "episode").action((x, c) => c.copy(episode = x)).text("Episode to remove user from."),
			opt[IUser]('u', "user").action((u, c) => c.copy(user = u)).text("User to remove.")
		)
		
		cmd("purge").action((_, c) => c.copy(command = "purge")).text("Remove user from all episodes").children(
			opt[IUser]('u', "user").action((u, c) => c.copy(user = u)).text("User to purge.")
		)
		
		cmd("list").action((_, c) => c.copy(command = "list")).text("List guests in episode").children(
			opt[Int]('e', "episode").action((x, c) => c.copy(episode = x)).text("Only display guests in this episode").optional()
		)
	}
	
	override def permissionLevel: Permission = Permission.ADMIN
	
	private def sendWelcomeMessage(user: IUser, episode: Int): Unit = {
		user.getOrCreatePMChannel().sendMessage(
			f"""
			   |Hello and welcome, to ${Configuration.showName}!
			   |
			   |I am the friendly neighbourhood bot.
			   |Thank you for participating in episode $episode!
			   |
			   |You have been given ${PermissionsHelper.getUserPermissionLevel(user)} permissions.
			   |
			   |As part of our live show, we provide our guests with the ability to stream their view as part of our multi-camera setup.
			   |If you have the capability to do so, we would love to have your view on the stream!
			   |Your streaming key is: ${StreamKeyHelper.getUserStreamKey(user)}
			   |You can use the 'streaminstructions' command to get information on how to stream to our server.
			   |You can use the 'streamkey' command to get your key again or 'streamkey -reset' to reset your key.
			   |You can use the 'whitelist' command to whitelist yourself on the server.
			   |""".stripMargin)
	}
}

object GuestCommand {
	
	case class Options(command: String, episode: Int, user: IUser) extends TCommandOptions {
		def this() = this(null, -1, null)
	}
	
}
