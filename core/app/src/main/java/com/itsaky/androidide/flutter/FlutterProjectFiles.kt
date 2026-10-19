/*
 *  This file is part of FlutterIDE.
 *
 *  FlutterIDE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */

package com.itsaky.androidide.flutter

import java.io.File

/**
 * Small, filesystem-only helpers for Dart source files in a Flutter project.
 *
 * These operations intentionally do not start Flutter/Dart processes, making
 * file browsing and editing usable on low-memory devices.
 */
object FlutterProjectFiles {

  data class DartFile(
    val relativePath: String,
    val sizeBytes: Long,
    val lastModified: Long
  )

  private val ignoredDirectories = setOf(
    ".dart_tool",
    ".git",
    ".idea",
    "build",
    ".flutter-plugins",
    ".flutter-plugins-dependencies"
  )

  /**
   * Lists Dart source files, excluding generated/build metadata directories.
   * Throws [IllegalArgumentException] when [projectRoot] is not a Flutter root.
   */
  @JvmStatic
  fun listDartFiles(projectRoot: File): List<DartFile> {
    val root = requireFlutterRoot(projectRoot)
    return root.walkTopDown()
      .onEnter { directory ->
        directory == root || directory.name !in ignoredDirectories
      }
      .filter { it.isFile && it.extension == "dart" && isInside(root, it) }
      .map {
        DartFile(
          relativePath = root.toPath().relativize(it.canonicalFile.toPath())
            .toString().replace(File.separatorChar, '/'),
          sizeBytes = it.length(),
          lastModified = it.lastModified()
        )
      }
      .sortedBy { it.relativePath }
      .toList()
  }

  /** Reads an existing Dart file contained by the project root. */
  @JvmStatic
  fun readDartFile(projectRoot: File, dartFile: File): String {
    val root = requireFlutterRoot(projectRoot)
    val target = requireDartPath(root, dartFile)
    require(target.isFile) { "Dart file does not exist: ${target.name}" }
    return target.readText(Charsets.UTF_8)
  }

  /**
   * Saves a Dart file inside the project. Parent directories are created as
   * needed, but paths that escape the project or are not .dart files are rejected.
   */
  @JvmStatic
  fun writeDartFile(projectRoot: File, dartFile: File, content: String) {
    val root = requireFlutterRoot(projectRoot)
    val target = requireDartPath(root, dartFile)
    val parent = target.parentFile
      ?: throw IllegalArgumentException("Dart file must be inside the project.")
    require(isInside(root, parent, allowRoot = true)) {
      "Dart file must be inside the Flutter project."
    }
    if (!parent.exists()) {
      check(parent.mkdirs() || parent.isDirectory) {
        "Could not create Dart file directory."
      }
    }
    check(parent.isDirectory) { "Dart file parent is not a directory." }
    target.writeText(content, Charsets.UTF_8)
  }

  private fun requireFlutterRoot(projectRoot: File): File {
    val root = projectRoot.canonicalFile
    require(root.isDirectory) { "Flutter project root must be an existing directory." }
    require(File(root, "pubspec.yaml").isFile) {
      "Selected directory does not contain pubspec.yaml."
    }
    return root
  }

  private fun requireDartPath(root: File, dartFile: File): File {
    require(dartFile.extension == "dart") { "Only .dart files are supported." }
    val target = if (dartFile.isAbsolute) dartFile.canonicalFile
    else File(root, dartFile.path).canonicalFile
    require(isInside(root, target)) {
      "Dart file must be inside the Flutter project."
    }
    return target
  }

  private fun isInside(root: File, target: File, allowRoot: Boolean = false): Boolean {
    val rootPath = root.canonicalFile.toPath()
    val targetPath = target.canonicalFile.toPath()
    return (allowRoot && targetPath == rootPath) || targetPath.startsWith(rootPath) &&
      targetPath != rootPath
  }
}
