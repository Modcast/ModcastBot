package com.amadornes.modcast.bot.database

import com.amadornes.modcast.bot.Configuration
import sorm._

/**
  * Created by rewbycraft on 11/8/16.
  */
object DB extends Instance(
	entities = Set(
		Entity[EpisodeRole](),
		Entity[EpisodeGuest](),
		Entity[StreamKey](),
		Entity[UserPermission](),
		Entity[UserMCAccount]()
	),
	url = Configuration.config.getString("db.url"),
	user = Configuration.config.getString("db.username"),
	password = Configuration.config.getString("db.password"),
	initMode = InitMode.Create
)

case class EpisodeRole(episode: Int, guild: String, role: String)
case class EpisodeGuest(episode: Int, guild: String, user: String)
case class StreamKey(user: String, key: String, name: String)
case class UserPermission(user: String, permission: Int)
case class UserMCAccount(user: String, account: String)
