package com.amadornes.modcast.bot.helpers

import java.util

import com.amadornes.modcast.bot.database._
import sx.blah.discord.handle.obj.{IGuild, IRole, IUser, Permissions}
import sx.blah.discord.util.RoleBuilder

/**
  * Created by rewbycraft on 11/11/16.
  */
object GuestHelper {
	def isGuest(user: IUser, guild: IGuild): Boolean = DB.query[EpisodeGuest].whereEqual("guild", guild.getID).whereEqual("user", user.getID).exists()
	
	def isGuest(user: IUser, guild: IGuild, episode: Int): Boolean = DB.query[EpisodeGuest].whereEqual("guild", guild.getID).whereEqual("user", user.getID).whereEqual("episode", episode).exists()
	
	def addGuest(user: IUser, episode: Int, guild: IGuild): Unit = {
		val role = getEpisodeRoll(episode, guild)
		user.addRole(role)
		
		DB.save(EpisodeGuest(episode, guild.getID, user.getID))
		
		PermissionsHelper.upgradeUserPermissionLevel(user, Permission.GUEST)
	}
	
	def delGuest(user: IUser, episode: Int, guild: IGuild): Unit = {
		val entry = DB.query[EpisodeGuest]
			.whereEqual("guild", guild.getID)
			.whereEqual("user", user.getID)
			.whereEqual("episode", episode).fetchOne()
		
		if (entry.isEmpty)
			return
		
		DB.delete(entry.get)
		user.removeRole(getEpisodeRoll(episode, guild))
		
		if (!DB.query[EpisodeGuest].whereEqual("user", user.getID).exists()) {
			//User is not in any episode anymore. Let's clean up some stuff.
			
			//If the user is *only* a guest, erase their permissions and stream key.
			if (PermissionsHelper.getUserPermissionLevel(user) == Permission.GUEST) {
				PermissionsHelper.setUserPermissionLevel(user, Permission.NONE)
				StreamKeyHelper.removeUserStreamKey(user)
				MCWhitelistHelper.deassociateMCAccountWithUser(user)
			}
		}
	}
	
	def delAllEpisodes(user: IUser, guild: IGuild): Unit = {
		if (PermissionsHelper.getUserPermissionLevel(user) == Permission.GUEST)
			MCWhitelistHelper.deassociateMCAccountWithUser(user)
		
		for (episode <- DB.query[EpisodeGuest].whereEqual("guild", guild.getID).whereEqual("user", user.getID).fetch().map(_.episode))
			delGuest(user, episode, guild)
	}
	
	private def getEpisodeRoll(episode: Int, guild: IGuild): IRole = {
		val roleObject = DB.query[EpisodeRole].whereEqual("guild", guild.getID).whereEqual("episode", episode).fetchOne()
		if (roleObject.isDefined)
			return guild.getRoleByID(roleObject.get.role)
		
		//Create the roll if it does not exist
		val roleBuilder = new RoleBuilder(guild)
		roleBuilder.withName("Episode " + episode)
		roleBuilder.setHoist(true)
		roleBuilder.setMentionable(true)
		val role = roleBuilder.build()
		
		DB.save(EpisodeRole(episode, guild.getID, role.getID))
		
		val channel = guild.createChannel("episode" + episode)
		
		channel.overrideRolePermissions(guild.getEveryoneRole, util.EnumSet.noneOf(classOf[Permissions]), util.EnumSet.allOf(classOf[Permissions]))
		channel.overrideRolePermissions(role,
			util.EnumSet.of(
				Permissions.READ_MESSAGES,
				Permissions.SEND_MESSAGES,
				Permissions.SEND_TTS_MESSAGES,
				Permissions.EMBED_LINKS,
				Permissions.ATTACH_FILES,
				Permissions.READ_MESSAGE_HISTORY,
				Permissions.MENTION_EVERYONE,
				Permissions.USE_EXTERNAL_EMOJIS
			), util.EnumSet.noneOf(classOf[Permissions]))
		
		DB.save(EpisodeChannel(episode, guild.getID, channel.getID))
		
		role
	}
}
