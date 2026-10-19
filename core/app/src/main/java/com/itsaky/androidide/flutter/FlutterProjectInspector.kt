/*
 *  This file is part of FlutterIDE.
 *
 *  FlutterIDE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  FlutterIDE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 */

package com.itsaky.androidide.flutter

import java.io.File

/**
 * Lightweight, filesystem-only inspection of a Flutter project.
 *
 * This class deliberately does not invoke Flutter or Dart commands. It is safe to
 * use during project browsing on low-memory devices and can be unit-tested without
 * an Android runtime.
 */
object FlutterProjectInspector {

  enum class Status {
    NOT_A_DIRECTORY,
    NOT_A_FLUTTER_PROJECT,
    INCOMPLETE,
    READY
  }

  data class ProjectInfo(
    val root: File,
    val status: Status,
    val hasPubspec: Boolean,
    val hasMainDart: Boolean,
    val hasAndroidHost: Boolean,
    val hasTests: Boolean,
    val hasAssetsDirectory: Boolean,
    val issues: List<String>
  )

  /**
   * Inspect a directory using Flutter's conventional project markers.
   *
   * A valid pubspec.yaml is the primary marker. A project with a pubspec but no
   * lib/main.dart is reported as incomplete rather than rejected, since packages
   * and plugin-only Flutter repositories may not contain an application entrypoint.
   */
  @JvmStatic
  fun inspect(directory: File): ProjectInfo {
    val root = directory.absoluteFile.normalizeFile()
    if (!directory.isDirectory) {
      return ProjectInfo(
        root = root,
        status = Status.NOT_A_DIRECTORY,
        hasPubspec = false,
        hasMainDart = false,
        hasAndroidHost = false,
        hasTests = false,
        hasAssetsDirectory = false,
        issues = listOf("Selected path is not an existing directory.")
      )
    }

    val hasPubspec = File(directory, "pubspec.yaml").isFile
    val hasMainDart = File(directory, "lib/main.dart").isFile
    val hasAndroidHost = File(directory, "android").isDirectory
    val hasTests = File(directory, "test").isDirectory
    val hasAssetsDirectory = File(directory, "assets").isDirectory

    val issues = buildList {
      if (!hasPubspec) {
        add("Missing pubspec.yaml; this directory is not recognized as a Flutter project.")
      }
      if (hasPubspec && !hasMainDart) {
        add("lib/main.dart is missing; this may be a package/plugin or an incomplete app.")
      }
      if (hasPubspec && !hasAndroidHost) {
        add("android/ directory is missing; Android builds may require platform setup.")
      }
      if (hasPubspec && !hasTests) {
        add("test/ directory is missing; add tests when appropriate.")
      }
    }

    val status = when {
      !hasPubspec -> Status.NOT_A_FLUTTER_PROJECT
      hasMainDart -> Status.READY
      else -> Status.INCOMPLETE
    }

    return ProjectInfo(
      root = root,
      status = status,
      hasPubspec = hasPubspec,
      hasMainDart = hasMainDart,
      hasAndroidHost = hasAndroidHost,
      hasTests = hasTests,
      hasAssetsDirectory = hasAssetsDirectory,
      issues = issues
    )
  }

  private fun File.normalizeFile(): File = try {
    canonicalFile
  } catch (_: Exception) {
    absoluteFile
  }
}
