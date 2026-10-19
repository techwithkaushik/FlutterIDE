/*
 *  This file is part of FlutterIDE.
 *
 *  FlutterIDE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */

package com.itsaky.androidide.models

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FileExtensionTest {

  @Test
  fun `Dart source files are recognized by the existing file tree model`() {
    assertThat(FileExtension.Factory.forExtension("dart"))
      .isEqualTo(FileExtension.DART)
  }

  @Test
  fun `Flutter YAML manifests are recognized by the existing file tree model`() {
    assertThat(FileExtension.Factory.forExtension("yaml"))
      .isEqualTo(FileExtension.YAML)
    assertThat(FileExtension.Factory.forExtension("yml"))
      .isEqualTo(FileExtension.YML)
  }
}
