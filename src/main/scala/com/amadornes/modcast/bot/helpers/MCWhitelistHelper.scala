package com.amadornes.modcast.bot.helpers

import java.net.URLEncoder

import com.amadornes.modcast.bot.Actors
import com.amadornes.modcast.bot.database.{DB, UserMCAccount}
import com.amadornes.modcast.bot.servers.MCWhitelistServer
import com.google.gson.{Gson, JsonObject}
import sx.blah.discord.handle.obj.IUser

import scalaj.http.Http

/**
  * Created by rewbycraft on 11/9/16.
  */
object MCWhitelistHelper {
	def associateMCAccountWithUser(user: IUser, account: String): Unit = {
		val id = getMCAccountUUID(account)
		if (DB.query[UserMCAccount].whereEqual("user", user.getID).exists())
			deassociateMCAccountWithUser(user)
		Actors.servers.MCWhitelistServer ! MCWhitelistServer.WhitelistUser(id)
		DB.save(UserMCAccount(user.getID, id))
	}
	
	def deassociateMCAccountWithUser(user: IUser): Unit = {
		val account = DB.query[UserMCAccount].whereEqual("user", user.getID).fetchOne()
		if (account.isDefined) {
			Actors.servers.MCWhitelistServer ! MCWhitelistServer.UnWhitelistUser(account.get.account)
			DB.delete(account.get)
		}
	}
	
	def getMCAccountUUID(name: String): String = {
		val http = Http("http://mcapi.ca/profile/" + URLEncoder.encode(name)).asString
		if (http.code != 200)
			throw new IllegalArgumentException()
		new Gson().fromJson(http.body, classOf[JsonObject]).get("uuid").getAsString
	}
}
