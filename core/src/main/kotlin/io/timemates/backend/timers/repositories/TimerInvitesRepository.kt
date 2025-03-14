package io.timemates.backend.timers.repositories

import com.timemates.backend.time.UnixTime
import io.timemates.backend.common.markers.Repository
import io.timemates.backend.common.types.value.Count
import io.timemates.backend.pagination.Page
import io.timemates.backend.pagination.PageToken
import io.timemates.backend.timers.types.Invite
import io.timemates.backend.timers.types.value.InviteCode
import io.timemates.backend.timers.types.value.TimerId
import io.timemates.backend.users.types.value.UserId

interface TimerInvitesRepository : Repository {
    suspend fun getInvites(timerId: TimerId, nextPageToken: PageToken?): Page<Invite>
    suspend fun removeInvite(timerId: TimerId, code: InviteCode)
    suspend fun getInvite(code: InviteCode): Invite?

    suspend fun getInvitesCount(timerId: TimerId, after: UnixTime): Count

    suspend fun createInvite(
        timerId: TimerId,
        userId: UserId,
        code: InviteCode,
        creationTime: UnixTime,
        limit: Count,
    )
}