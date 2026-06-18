package com.example.cross_platformfilemanager

import android.util.AtomicFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private const val SNAPSHOT_FILE_NAME = "app_snapshot.json"

private class AndroidSnapshotSaveException(
    cause: Throwable,
) : Exception("Failed to save Android app snapshot.", cause)

actual fun createAppSnapshotStore(): AppSnapshotStore? {
    val context = AndroidContextHolder.applicationContext ?: return null
    return AndroidAppSnapshotStore(
        snapshotFile = File(context.filesDir, SNAPSHOT_FILE_NAME),
    )
}

private class AndroidAppSnapshotStore(
    snapshotFile: File,
) : AppSnapshotStore {
    private val atomicFile = AtomicFile(snapshotFile)

    override suspend fun load(): AppSnapshot? = withContext(Dispatchers.IO) {
        if (!atomicFile.baseFile.exists()) {
            return@withContext null
        }

        try {
            SnapshotCodec.decode(atomicFile.readFully().decodeToString())
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun save(snapshot: AppSnapshot) {
        withContext(Dispatchers.IO) {
            val encoded = try {
                SnapshotCodec.encode(snapshot)
            } catch (error: Exception) {
                throw AndroidSnapshotSaveException(error)
            }

            val output = try {
                atomicFile.startWrite()
            } catch (error: Exception) {
                throw AndroidSnapshotSaveException(error)
            }

            try {
                output.write(encoded.encodeToByteArray())
                atomicFile.finishWrite(output)
            } catch (error: Exception) {
                try {
                    atomicFile.failWrite(output)
                } catch (_: Exception) {
                    // Saving is best-effort; leave the previous atomic snapshot intact.
                }
                throw AndroidSnapshotSaveException(error)
            }
        }
    }
}
