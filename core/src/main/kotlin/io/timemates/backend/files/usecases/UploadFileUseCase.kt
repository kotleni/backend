package io.timemates.backend.files.usecases

import com.timemates.backend.time.TimeProvider
import io.timemates.backend.validation.createOrThrowInternally
import com.timemates.random.RandomProvider
import io.timemates.backend.common.markers.UseCase
import io.timemates.backend.features.authorization.AuthorizedContext
import io.timemates.backend.files.repositories.FilesRepository
import io.timemates.backend.files.types.FileType
import io.timemates.backend.files.types.FilesScope
import io.timemates.backend.files.types.value.FileId
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow

class UploadFileUseCase(
    private val files: FilesRepository,
    private val randomProvider: RandomProvider,
    private val timeProvider: TimeProvider,
) : UseCase {
    context(AuthorizedContext<FilesScope.Write>)
    suspend fun execute(
        fileType: FileType,
        inputStream: Flow<ByteArray>,
    ): Result {
        val fileId = FileId.createOrThrowInternally(randomProvider.randomHash(FileId.SIZE))

        // saving compressed variants, if exception occurs, we remove all already
        // saved variants
        try {
            coroutineScope {
                files.save(fileId, fileType, inputStream, timeProvider.provide())
            }
        } catch (exception: Exception) {
            files.remove(fileId)
            exception.printStackTrace()
            return Result.Failure
        }

        return Result.Success(fileId)
    }

    sealed class Result {
        data object Failure : Result()

        data class Success(val fileId: FileId) : Result()
    }
}