/*
 *  This file is part of FlutterIDE.
 *
 *  FlutterIDE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */

package com.itsaky.androidide.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro2
import com.github.appintro.AppIntroPageTransformerType
import com.itsaky.androidide.fragments.onboarding.IdeSetupConfigurationFragment

/**
 * Optional Android SDK/JDK setup for users who need the legacy Android build toolchain.
 * FlutterIDE's welcome flow does not require these tools.
 */
class AndroidToolchainSetupActivity : AppIntro2() {

  private val terminalActivityCallback = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
  ) {
    finish()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setSwipeLock(true)
    setTransformer(AppIntroPageTransformerType.Fade)
    setProgressIndicator()
    showStatusBar(true)
    isIndicatorEnabled = true
    isWizardMode = true
    addSlide(IdeSetupConfigurationFragment.newInstance(this))
  }

  override fun onDonePressed(currentFragment: Fragment?) {
    val setup = currentFragment as? IdeSetupConfigurationFragment ?: run {
      finish()
      return
    }
    val intent = Intent(this, TerminalActivity::class.java)
    if (setup.isAutoInstall()) {
      intent.putExtra(TerminalActivity.EXTRA_ONBOARDING_RUN_IDESETUP, true)
      intent.putExtra(
        TerminalActivity.EXTRA_ONBOARDING_RUN_IDESETUP_ARGS,
        setup.buildIdeSetupArguments()
      )
    }
    terminalActivityCallback.launch(intent)
  }
}
