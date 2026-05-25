/**
 * 项目级设置脚本，负责声明模块名、插件仓库和依赖仓库。
 */
rootProject.name = "Taggo"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    // 插件解析阶段允许使用的仓库。
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // 普通依赖解析使用的仓库，和上面的插件仓库职责不同。
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// 当前工程只有一个应用模块，所有跨平台代码都在 `composeApp` 下组织。
include(":composeApp")
