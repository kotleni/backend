package io.timemates.backend.auth.usecases

import com.timemates.backend.time.SystemTimeProvider
import com.timemates.backend.time.UnixTime
import com.timemates.random.SecureRandomProvider
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.mockk
import io.timemates.backend.authorization.repositories.AuthorizationsRepository
import io.timemates.backend.authorization.types.Authorization
import io.timemates.backend.authorization.types.metadata.ClientMetadata
import io.timemates.backend.authorization.types.metadata.value.ClientIpAddress
import io.timemates.backend.authorization.types.metadata.value.ClientName
import io.timemates.backend.authorization.types.metadata.value.ClientVersion
import io.timemates.backend.authorization.types.value.AccessHash
import io.timemates.backend.authorization.types.value.RefreshHash
import io.timemates.backend.authorization.usecases.GetUserIdByAccessTokenUseCase
import io.timemates.backend.testing.validation.createOrAssert
import io.timemates.backend.users.types.value.UserId
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.platform.commons.annotation.Testable
import kotlin.test.assertEquals

@Testable
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GetUserIdByAccessTokenUseCaseTest {

    private lateinit var useCase: GetUserIdByAccessTokenUseCase
    private val timeProvider = SystemTimeProvider()
    private val randomProvider = SecureRandomProvider()

    private val authorizationsRepository = mockk<AuthorizationsRepository>()

    @BeforeAll
    fun before() {
        MockKAnnotations.init(this)
        useCase = GetUserIdByAccessTokenUseCase(
            authorizations = authorizationsRepository,
            time = timeProvider
        )
    }

    @Test
    fun `get user id by valid access hash should pass`() = runBlocking {
        // GIVEN
        val accessHash = AccessHash.createOrAssert(randomProvider.randomHash(AccessHash.SIZE))
        val refreshHash = RefreshHash.createOrAssert(randomProvider.randomHash(RefreshHash.SIZE))
        val time = UnixTime.createOrAssert(123232335)
        val userId = UserId.createOrAssert(1235)
        val authorization = Authorization(
            userId = userId,
            accessHash = accessHash,
            refreshAccessHash = refreshHash,
            scopes = listOf(),
            createdAt = UnixTime.createOrAssert(12323232),
            expiresAt = time,
            clientMetadata = ClientMetadata(
                clientName = ClientName.createOrAssert("Xiaomi"),
                clientVersion = ClientVersion.createOrAssert(1.0),
                clientIpAddress = ClientIpAddress.createOrAssert("127.0.0.1"),
            )
        )
        coEvery { authorizationsRepository.get(any(), any()) }.returns(authorization)
        // WHEN
        val result = useCase.execute(accessHash)
        // THEN
        assertEquals(GetUserIdByAccessTokenUseCase.Result.Success(userId), result)
    }

    @Test
    fun `get user id by invalid access hash should return NotFound`() = runBlocking {
        // GIVEN
        val accessHash = AccessHash.createOrAssert(randomProvider.randomHash(AccessHash.SIZE))
        coEvery { authorizationsRepository.get(any(), any()) }.returns(null)
        // WHEN
        val result = useCase.execute(accessHash)
        // THEN
        assertEquals(GetUserIdByAccessTokenUseCase.Result.NotFound, result)
    }
}