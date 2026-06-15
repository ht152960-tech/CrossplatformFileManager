package com.example.cross_platformfilemanager

import android.util.AtomicFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private const val SNAPSHOT_FILE_NAME = "app_snapshot.json"

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
            } catch (_: Exception) {
                return@withContext
            }

            val output = try {
                atomicFile.startWrite()
            } catch (_: Exception) {
                return@withContext
            }

            try {
                output.write(encoded.encodeToByteArray())
                atomicFile.finishWrite(output)
            } catch (_: Exception) {
                try {
                    atomicFile.failWrite(output)
                } catch (_: Exception) {
                    // Saving is best-effort; leave the previous atomic snapshot intact.
                }
            }
        }
    }
}
