package com.amadornes.modcast.bot.discord.commands

import com.amadornes.modcast.bot.Configuration
import com.amadornes.modcast.bot.database.Permission
import com.amadornes.modcast.bot.discord.{AbstractCommand, TCommandOptions}
import com.amadornes.modcast.bot.helpers.StreamKeyHelper
import sx.blah.discord.handle.obj.IUser

/**
  * Created by rewbycraft on 11/9/16.
  */
class StreamCommand extends AbstractCommand[StreamCommand.Options] {
	override def handle: Receive = {
		case c@Execute(channel, user, "streamkey", arguments) =>
			val key = {
				if (arguments.reset)
					StreamKeyHelper.resetUserStreamKey(user)
				else
					StreamKeyHelper.getUserStreamKey(user)
			}
			
			user.getOrCreatePMChannel().sendMessage(s"Your stream key is: $key")
			
			if (!channel.isPrivate)
				c.respond("I have sent you a private message.")

		case c@Execute(channel, user, "streaminstructions", _) =>
			sendInstructions(user)
			if (!channel.isPrivate)
				c.respond("I have sent you a private message.")
	}
	
	override def name: Array[String] = Array("streamkey", "streaminstructions")
	
	override def parser(command: String): OptionParser = command match {
		case "streamkey" => new OptionParser(command) {
			note("Get or reset your stream key")
			opt[Unit]('r', "reset").text("Reset your stream key.").action((_, c) => c.copy(reset = true))
		}
		
		case "streaminstructions" => new OptionParser(command) {
			note("Get instructions on how to stream to the private server.")
		}
	}
	
	override def permissionLevel: Permission = Permission.GUEST
	
	private def sendInstructions(user: IUser): Unit = {
		val message = f"""
			   |Streaming to ${Configuration.showName} is very simple!
			   |
			   |These instructions pertain to OBS Studio.
			   |For an optimal experience, please set the following settings:
			   |Go to File->Settings
			   |Then, under "Stream", change the stream type to "Custom Streaming Server".
			   |Set the URL to rtmp://${Configuration.config.getString("rtmp.host")}/${Configuration.config.getString("rtmp.ingestapp")}
			   |And set your stream key to your unique key, which can be found with the "streamkey" command.
			   |Now go to "Output"
			   |Set "Output Mode" to advanced. If applicable, enable advanced encoder settings.
			   |You will want to set the bitrate to at least 1800, but preferably higher if your network can support it.
			   |(You can use https://obsproject.com/help/estimator with the "medium motion" setting and a resolution of 1280x720)
			   |Also set your "Rate Control" to CBR.
			   |And set the "Keyframe Interval" to 2.
			   |Here is an example of what it should look like at the end (this is using the software x264 encoder, use NVENC if you have it): https://ss.roelf.org/screenshot-13-02-30.png
			   |Make sure you are set to stream at least 1280x720 under the "Video" tab (the given settings here work well for this resolution).
			   |You can also stream in 1080p if you want, but you may need to bump up the bitrate for that.
			   |Now for your scene.
			   |Let's assume you are running the game in windowed mode.
			   |You will want an empty scene (this is the default if you've never used OBS before).
			   |You can then add a game capture source or window capture source of the game.
			   |Audio doesn't matter as we do not use your audio feed.
			   |Now, OBS might do the right thing by default which is to scale your capture to fill as much of the "canvas" as it can without breaking the aspect ratio.
			   |This is good. It does not matter that the game does not fill the canvas in one of the directions (usually the vertical axis).
			   |If OBS does not do this, please do so manually by clicking on the image and dragging the corner.
			   |By default OBS will not break the aspect ratio. THIS IS GOOD
			   |Do NOT select "Fill screen" and break the aspect ratio.
			   |
			   |At this point, you should be good to go and you can click start to get going!
			 """.stripMargin
		for (chunk <- message.split("\n").grouped(20))
			user.getOrCreatePMChannel().sendMessage(chunk.mkString("\n"))
	}
}

object StreamCommand {
	
	case class Options(reset: Boolean = false) extends TCommandOptions {
		def this() = this(false)
	}
	
}
