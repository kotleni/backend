package io.timemates.backend.auth.usecases

import com.timemates.backend.time.TimeProvider
import com.timemates.backend.time.UnixTime
import com.timemates.random.RandomProvider
import com.timemates.random.SecureRandomProvider
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.spyk
import io.timemates.backend.authorization.repositories.VerificationsRepository
import io.timemates.backend.authorization.types.metadata.ClientMetadata
import io.timemates.backend.authorization.types.metadata.value.ClientIpAddress
import io.timemates.backend.authorization.types.metadata.value.ClientName
import io.timemates.backend.authorization.types.metadata.value.ClientVersion
import io.timemates.backend.authorization.usecases.AuthByEmailUseCase
import io.timemates.backend.common.repositories.EmailsRepository
import io.timemates.backend.common.types.value.Count
import io.timemates.backend.testing.validation.createOrAssert
import io.timemates.backend.users.types.value.EmailAddress
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.platform.commons.annotation.Testable
import kotlin.test.assertEquals

@Testable
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class AuthByEmailUseCaseTest {
    @MockK
    lateinit var verificationsRepository: VerificationsRepository

    @MockK
    lateinit var timeProvider: TimeProvider

    @MockK
    lateinit var emailsRepository: EmailsRepository

    private val randomProvider: RandomProvider = SecureRandomProvider()

    private lateinit var useCase: AuthByEmailUseCase

    private val email = EmailAddress.createOrAssert("test@email.com")

    @BeforeAll
    fun before() {
        useCase = AuthByEmailUseCase(
            emailsRepository, verificationsRepository, timeProvider, randomProvider
        )
    }

    //@Test
    fun `test success email sending`(): Unit = runBlocking {
        // GIVEN
        val clientMetadata = spyk<ClientMetadata>()
        val time = UnixTime.createOrAssert(System.currentTimeMillis())
        coEvery { verificationsRepository.getNumberOfAttempts(any(), any()) } returns Count.createOrAssert(0)
        coEvery { verificationsRepository.getNumberOfSessions(any(), any()) } returns Count.createOrAssert(0)
        every { timeProvider.provide() } returns time
        coEvery { emailsRepository.send(any(), any()) } returns true
        coJustRun { verificationsRepository.save(any(), any(), any(), any(), any(), any()) }

        // WHEN
        val result = useCase.execute(email, clientMetadata)

        // THEN
//        assertEquals(
//            expected = AuthByEmailUseCase.Result.Success(
//                time + 10.minutes,
//                Attempts.createOrAssert(3)
//            ),
//            actual = result
//        )
    }

    // @Test
    fun `test sessions number exceed`(): Unit = runBlocking {
        // GIVEN
        val clientMetadata = ClientMetadata(
            clientName = ClientName.createOrAssert("name"),
            clientVersion = ClientVersion.createOrAssert(1.0),
            clientIpAddress = ClientIpAddress.createOrAssert("ip_address"),
        )
        coEvery { verificationsRepository.getNumberOfAttempts(any(), any()) } returns Count.createOrAssert(0)
        coEvery { verificationsRepository.getNumberOfSessions(any(), any()) } returns Count.createOrAssert(3)
        every { timeProvider.provide() } returns UnixTime.createOrAssert(System.currentTimeMillis())
        coJustRun { verificationsRepository.save(any(), any(), any(), any(), any(), any()) }

        // WHEN
        val result = useCase.execute(email, clientMetadata)

        // THEN
        assertEquals(
            expected = AuthByEmailUseCase.Result.AttemptsExceed,
            actual = result
        )
    }

    // @Test
    fun `test attempts number exceed`(): Unit = runBlocking {
        // GIVEN
        val clientMetadata = spyk<ClientMetadata>()
        coEvery { verificationsRepository.getNumberOfAttempts(any(), any()) } returns Count.createOrAssert(9)
        every { timeProvider.provide() } returns UnixTime.createOrAssert(System.currentTimeMillis())
        coJustRun { verificationsRepository.save(any(), any(), any(), any(), any(), any()) }

        // WHEN
        val result = useCase.execute(email, clientMetadata)

        // THEN
        assertEquals(
            expected = AuthByEmailUseCase.Result.AttemptsExceed,
            actual = result,
        )
    }
}