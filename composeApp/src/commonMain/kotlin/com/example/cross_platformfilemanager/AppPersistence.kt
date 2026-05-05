package com.example.cross_platformfilemanager

interface AppSnapshotStore {
    suspend fun load(): AppSnapshot?
    suspend fun save(snapshot: AppSnapshot)
}

5expect fun createAppSnapshotStore(): AppSnapshotStore?

