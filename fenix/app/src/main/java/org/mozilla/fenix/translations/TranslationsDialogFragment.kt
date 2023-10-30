/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.translations

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.mozilla.fenix.R
import org.mozilla.fenix.theme.FirefoxTheme

/**
 * The enum is to know what bottom sheet to open.
 */
enum class TranslationsDialogAccessPoint {
    Translations,
    TranslationsOptions,
}

/**
 * A bottom sheet fragment displaying the Firefox Translation dialog.
 */
class TranslationsDialogFragment : BottomSheetDialogFragment() {

    private var behavior: BottomSheetBehavior<View>? = null
    private val args by navArgs<TranslationsDialogFragmentArgs>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        super.onCreateDialog(savedInstanceState).apply {
            setOnShowListener {
                val bottomSheet =
                    findViewById<View?>(R.id.design_bottom_sheet)
                bottomSheet?.setBackgroundResource(android.R.color.transparent)
                behavior = BottomSheetBehavior.from(bottomSheet)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setContent {
            FirefoxTheme {
                var translationsVisibility by remember {
                    mutableStateOf(args.translationsDialogAccessPoint == TranslationsDialogAccessPoint.Translations)
                }
                if (translationsVisibility) {
                    ShowTranslations(
                        onDismiss = {
                            translationsVisibility = false
                        },
                    )
                } else {
                    ShowTranslationOptions(
                        onDismiss = {
                            translationsVisibility = true
                        },
                    )
                }
            }
        }
    }

    @Composable
    private fun ShowTranslations(onDismiss: () -> Unit) {
        TranslationsDialogBottomSheet(
            onSettingClicked = onDismiss,
        )
    }

    @Composable
    private fun ShowTranslationOptions(onDismiss: () -> Unit) {
        TranslationOptionsDialogBottomSheet(
            translationOptionsList = getTranslationOptionsList(),
            onBackClicked = onDismiss,
            onTranslationSettingsClicked = {
                findNavController().navigate(
                    TranslationsDialogFragmentDirections.actionTranslationsDialogFragmentTranslationSettingsFragment(),
                )
            },
            aboutTranslationClicked = {},
        )
    }
}
