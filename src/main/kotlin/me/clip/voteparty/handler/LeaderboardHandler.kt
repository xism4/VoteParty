package me.clip.voteparty.handler

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import me.clip.voteparty.base.Addon
import me.clip.voteparty.base.State
import me.clip.voteparty.leaderboard.Leaderboard
import me.clip.voteparty.leaderboard.LeaderboardType
import me.clip.voteparty.plugin.VotePartyPlugin
import java.util.concurrent.TimeUnit

class LeaderboardHandler(override val plugin: VotePartyPlugin) : Addon, State
{
	private val cache: LoadingCache<LeaderboardType, Leaderboard> = CacheBuilder.newBuilder().refreshAfterWrite(2, TimeUnit.MINUTES)
			.build(object : CacheLoader<LeaderboardType, Leaderboard>()
			       {
				       override fun load(key: LeaderboardType): Leaderboard
				       {
					       return Leaderboard(key, plugin.voteParty?.usersHandler?.getVotesWithinRange(key.duration) ?: emptyList())
				       }
			       })
	
	override fun load()
	{
		LeaderboardType.values().forEach {
			cache.put(it, Leaderboard(it, plugin.voteParty?.usersHandler?.getVotesWithinRange(it.duration) ?: emptyList()))
		}
	}
	
	override fun kill()
	{
		cache.invalidateAll()
	}
	
	fun getLeaderboard(type: LeaderboardType): Leaderboard
	{
		return cache.get(type)
	}
}