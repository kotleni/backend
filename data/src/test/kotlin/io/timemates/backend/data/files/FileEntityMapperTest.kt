package io.timemates.backend.data.files

import com.timemates.random.SecureRandomProvider
import io.timemates.backend.data.files.datasource.FileEntityMapper
import io.timemates.backend.data.files.datasource.PostgresqlFilesDataSource
import io.timemates.backend.files.types.File
import io.timemates.backend.files.types.value.FileId
import io.timemates.backend.testing.validation.createOrAssert
import kotlin.test.Test
import kotlin.test.assertEquals

class FileEntityMapperTest {
    private val random = SecureRandomProvider()
    private val mapper = FileEntityMapper()

    @Test
    fun `toDomainMapper should map to domain file correctly`() {
        // GIVEN
        val fileId = random.randomHash(64)
        val fileName = "Default Name"
        val fileType = PostgresqlFilesDataSource.FileType.IMAGE
        val filePath = "C\\123"
        val fileCreationType = 4444L

        val expectedFile = File.Image(
            FileId.createOrAssert(fileId)
        )

        // WHEN
        val actualFile = mapper.toDomainFile(
            PostgresqlFilesDataSource.File(
                fileId,
                fileName,
                fileType, filePath,
                fileCreationType,
            )
        )

        // THEN
        assertEquals(
            expected = expectedFile,
            actual = actualFile
        )
    }
}