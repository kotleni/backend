package io.timemates.api.rsocket.serializable.requests.timers.members

import io.timemates.api.rsocket.serializable.requests.common.RSocketRequest
import io.timemates.api.rsocket.serializable.types.users.SerializableUser
import kotlinx.serialization.Serializable

@Serializable
data class GetMembersListRequest(
    val timerId: Long,
    val pageToken: String? = null,
) : RSocketRequest<GetMembersListRequest.Result> {
    companion object Key : RSocketRequest.Key<GetMembersListRequest>

    override val key: RSocketRequest.Key<*>
        get() = Key

    @Serializable
    data class Result(
        val list: List<SerializableUser>,
        val nextPageToken: String? = null,
    )
}