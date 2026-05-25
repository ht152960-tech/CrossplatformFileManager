package com.example.cross_platformfilemanager

/**
 * 一次文件打开事件。
 *
 * 这是推荐算法最基础的行为样本：
 * 当前打开了哪个文件、打开时间是什么、它之前刚打开的是哪个文件。
 * 时间规律和后继关系都会从这个事件出发累计。
 */
data class FileOpenLog(
    val fileId: String,
    val openedAtMillis: Long,
    val previousFileId: String? = null,
)
