/*
 *  This file is part of FlutterIDE.
 *
 *  FlutterIDE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */

package com.itsaky.androidide.flutter

import com.google.common.truth.Truth.assertThat
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FlutterProjectFilesTest {

  @get:Rule
  val temporaryFolder = TemporaryFolder()

  private fun flutterProject(name: String = "sample"): File {
    val root = temporaryFolder.newFolder(name)
    File(root, "pubspec.yaml").writeText("name: sample\n")
    File(root, "lib").mkdirs()
    return root
  }

  @Test
  fun `lists Dart files in stable relative-path order`() {
    val root = flutterProject()
    File(root, "lib/z.dart").writeText("void z() {}")
    File(root, "lib/main.dart").writeText("void main() {}")
    File(root, "test").mkdirs()
    File(root, "test/widget_test.dart").writeText("void test() {}")

    val files = FlutterProjectFiles.listDartFiles(root)

    assertThat(files.map { it.relativePath }).containsExactly(
      "lib/main.dart",
      "lib/z.dart",
      "test/widget_test.dart"
    ).inOrder()
    assertThat(files.first().sizeBytes).isGreaterThan(0L)
  }

  @Test
  fun `ignores generated Dart files under build directories`() {
    val root = flutterProject()
    File(root, "lib/main.dart").writeText("void main() {}")
    File(root, "build/generated.dart").apply {
      parentFile.mkdirs()
      writeText("generated")
    }
    File(root, ".dart_tool/package_config.dart").apply {
      parentFile.mkdirs()
      writeText("generated")
    }

    val paths = FlutterProjectFiles.listDartFiles(root).map { it.relativePath }

    assertThat(paths).containsExactly("lib/main.dart")
  }

  @Test
  fun `writes and reads a nested Dart file`() {
    val root = flutterProject()
    val dartFile = File(root, "lib/features/home.dart")
    val source = "void main() { print('FlutterIDE'); }\n"

    FlutterProjectFiles.writeDartFile(root, dartFile, source)

    assertThat(dartFile.isFile).isTrue()
    assertThat(FlutterProjectFiles.readDartFile(root, dartFile)).isEqualTo(source)
  }

  @Test
  fun `rejects paths that escape the project root`() {
    val root = flutterProject()
    val outside = File(temporaryFolder.root, "outside.dart")
    outside.writeText("do not overwrite")

    val error = runCatching {
      FlutterProjectFiles.writeDartFile(root, File(root, "../outside.dart"), "unsafe")
    }.exceptionOrNull()

    assertThat(error).isInstanceOf(IllegalArgumentException::class.java)
    assertThat(outside.readText()).isEqualTo("do not overwrite")
  }

  @Test
  fun `rejects non Dart file paths`() {
    val root = flutterProject()

    val error = runCatching {
      FlutterProjectFiles.writeDartFile(root, File(root, "pubspec.yaml"), "name: changed")
    }.exceptionOrNull()

    assertThat(error).isInstanceOf(IllegalArgumentException::class.java)
    assertThat(File(root, "pubspec.yaml").readText()).isEqualTo("name: sample\n")
  }

  @Test
  fun `requires a Flutter project root`() {
    val nonFlutterRoot = temporaryFolder.newFolder("not-flutter")

    val error = runCatching {
      FlutterProjectFiles.listDartFiles(nonFlutterRoot)
    }.exceptionOrNull()

    assertThat(error).isInstanceOf(IllegalArgumentException::class.java)
  }
}
