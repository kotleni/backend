package org.tomadoro.backend.usecases.timers

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.tomadoro.backend.domain.TimerName
import org.tomadoro.backend.providers.MockedCurrentTimeProvider
import org.tomadoro.backend.repositories.MockedSessionsRepository
import org.tomadoro.backend.repositories.MockedTimersRepository
import org.tomadoro.backend.repositories.TimersRepository
import org.tomadoro.backend.repositories.UsersRepository
import kotlin.test.BeforeTest

class StartTimerUseCaseTest {
    private val repository = MockedTimersRepository()
    private val useCase = StartTimerUseCase(repository, MockedCurrentTimeProvider, MockedSessionsRepository())

    @BeforeTest
    fun before() {
        runBlocking {
            repository.createTimer(
                TimerName("test1"),
                TimersRepository.Settings.Default, UsersRepository.UserId(0),
                MockedCurrentTimeProvider.provide()
            )
        }
    }

    @Test
    fun testSuccess() = runBlocking {
        val timerId = TimersRepository.TimerId(0)
        val result = useCase(
            UsersRepository.UserId(0),
            timerId
        )
        assert(result is StartTimerUseCase.Result.Success)
    }

    @Test
    fun testNoAccess() = runBlocking {
        val result = useCase(UsersRepository.UserId(2), TimersRepository.TimerId(0))
        assert(result is StartTimerUseCase.Result.NoAccess)
    }

    @Test
    fun testNotFound() = runBlocking {
        val result = useCase(
            UsersRepository.UserId(2), TimersRepository.TimerId(5)
        )
        assert(result is StartTimerUseCase.Result.NoAccess)
    }
}