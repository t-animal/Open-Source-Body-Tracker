package de.t_animal.opensourcebodytracker.core.photos

import java.io.File
import java.nio.file.Files
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class PhotoStorageContractTest {
    @Test
    fun fileProviderCachePath_matchesPhotoStorageContract() {
        val xmlFile = listOf(
            File("app/src/main/res/xml/file_paths.xml"),
            File("src/main/res/xml/file_paths.xml"),
        ).firstOrNull { candidate -> candidate.exists() }
        if (xmlFile == null) {
            fail("Unable to locate file_paths.xml for contract validation")
            return
        }

        val xmlContent = Files.readString(xmlFile.toPath())
        assertTrue(
            "file_paths.xml cache-path must match PhotoStorageContract.TEMP_CAPTURE_FILE_PROVIDER_PATH",
            xmlContent.contains(
                "path=\"${PhotoStorageContract.TEMP_CAPTURE_FILE_PROVIDER_PATH}\"",
            ),
        )
    }
}
