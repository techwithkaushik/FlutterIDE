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

class FlutterProjectInspectorTest {

  @get:Rule
  val temporaryFolder = TemporaryFolder()

  @Test
  fun `directory without pubspec is not a Flutter project`() {
    val root = temporaryFolder.newFolder("not-flutter")

    val result = FlutterProjectInspector.inspect(root)

    assertThat(result.status)
      .isEqualTo(FlutterProjectInspector.Status.NOT_A_FLUTTER_PROJECT)
    assertThat(result.hasPubspec).isFalse()
    assertThat(result.issues).isNotEmpty()
  }

  @Test
  fun `Flutter app markers are detected`() {
    val root = temporaryFolder.newFolder("flutter-app")
    File(root, "pubspec.yaml").writeText("name: sample_app\n")
    File(root, "lib").mkdirs()
    File(root, "lib/main.dart").writeText("void main() {}\n")
    File(root, "android").mkdirs()
    File(root, "test").mkdirs()
    File(root, "assets").mkdirs()

    val result = FlutterProjectInspector.inspect(root)

    assertThat(result.status).isEqualTo(FlutterProjectInspector.Status.READY)
    assertThat(result.hasPubspec).isTrue()
    assertThat(result.hasMainDart).isTrue()
    assertThat(result.hasAndroidHost).isTrue()
    assertThat(result.hasTests).isTrue()
    assertThat(result.hasAssetsDirectory).isTrue()
    assertThat(result.issues).isEmpty()
  }

  @Test
  fun `pubspec without app entrypoint is reported as incomplete`() {
    val root = temporaryFolder.newFolder("flutter-package")
    File(root, "pubspec.yaml").writeText("name: sample_package\n")

    val result = FlutterProjectInspector.inspect(root)

    assertThat(result.status).isEqualTo(FlutterProjectInspector.Status.INCOMPLETE)
    assertThat(result.hasPubspec).isTrue()
    assertThat(result.hasMainDart).isFalse()
    assertThat(result.issues).contains("lib/main.dart is missing; this may be a package/plugin or an incomplete app.")
  }

  @Test
  fun `nonexistent directory is rejected`() {
    val root = File(temporaryFolder.root, "missing")

    val result = FlutterProjectInspector.inspect(root)

    assertThat(result.status).isEqualTo(FlutterProjectInspector.Status.NOT_A_DIRECTORY)
  }
}
