package io.timemates.backend.timers.repositories

import com.timemates.backend.time.UnixTime
import io.timemates.backend.common.markers.Repository
import io.timemates.backend.common.types.value.Count
import io.timemates.backend.pagination.Page
import io.timemates.backend.pagination.PageToken
import io.timemates.backend.timers.types.TimerSettings
import io.timemates.backend.timers.types.value.InviteCode
import io.timemates.backend.timers.types.value.TimerDescription
import io.timemates.backend.timers.types.value.TimerId
import io.timemates.backend.timers.types.value.TimerName
import io.timemates.backend.users.types.value.UserId

interface TimersRepository : Repository {
    suspend fun createTimer(
        name: TimerName,
        description: TimerDescription,
        settings: TimerSettings,
        ownerId: UserId,
        creationTime: UnixTime,
    ): TimerId

    suspend fun getTimerInformation(timerId: TimerId): TimerInformation?
    suspend fun removeTimer(timerId: TimerId)

    suspend fun getOwnedTimersCount(
        ownerId: UserId,
        after: UnixTime,
    ): Int

    suspend fun getTimerSettings(timerId: TimerId): TimerSettings?
    suspend fun setTimerSettings(timerId: TimerId, settings: TimerSettings.Patch)
    suspend fun addMember(
        userId: UserId,
        timerId: TimerId,
        joinTime: UnixTime,
        inviteCode: InviteCode,
    )

    suspend fun removeMember(userId: UserId, timerId: TimerId)

    suspend fun getMembers(
        timerId: TimerId,
        pageToken: PageToken?,
    ): Page<UserId>

    suspend fun getMembersCountOfInvite(timerId: TimerId, inviteCode: InviteCode): Count

    suspend fun isMemberOf(userId: UserId, timerId: TimerId): Boolean

    /**
     * Gets all timers where [userId] is participating.
     */
    suspend fun getTimersInformation(
        userId: UserId,
        pageToken: PageToken?,
    ): Page<TimerInformation>

    suspend fun setTimerInformation(timerId: TimerId, information: TimerInformation.Patch)

    data class TimerInformation(
        val id: TimerId,
        val name: TimerName,
        val description: TimerDescription,
        val ownerId: UserId,
        val settings: TimerSettings,
        val membersCount: Count,
    ) {
        data class Patch(
            val name: TimerName? = null,
            val description: TimerDescription? = null,
        )
    }
}