package io.timemates.backend.application.dependencies

import io.timemates.backend.data.files.datasource.FileEntityMapper
import io.timemates.backend.data.files.datasource.LocalFilesDataSource
import io.timemates.backend.data.files.datasource.PostgresqlFilesDataSource
import io.timemates.backend.files.repositories.FilesRepository
import io.timemates.backend.files.usecases.GetImageUseCase
import io.timemates.backend.files.usecases.UploadFileUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.net.URI
import java.nio.file.Path
import io.timemates.backend.data.files.FilesRepository as LocalFilesRepository

val filesPathName = named("files.path")

val FilesModule = module {
    single {
        LocalFilesDataSource(Path.of(get<URI>(filesPathName)))
    }
    singleOf(::PostgresqlFilesDataSource)
    singleOf(::FileEntityMapper)
    single<FilesRepository> {
        LocalFilesRepository(get(), get(), get())
    }

    // Use cases
    singleOf(::GetImageUseCase)
    singleOf(::UploadFileUseCase)
}