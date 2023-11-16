/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.feature.intent.processing

import android.content.Context
import android.content.Intent

/**
 * Processor for Android intents which should trigger session-related actions.
 */
open class IntentProcessor() {
    /**
     * Processes the given [Intent].
     *
     * @param intent The intent to process.
     * @return True if the intent was processed, otherwise false.
     */
    open fun process(intent: Intent): Boolean {
        return false
    }

    /**
     * Processes the given [Intent] with a [Context].
     *
     * @param intent The intent to process.
     * @param context The context in which to process the intent.
     * @return True if the intent was processed in the given context, otherwise false.
     */
    open fun process(intent: Intent, context: Context?): Boolean {
        return process(intent)
    }
}
