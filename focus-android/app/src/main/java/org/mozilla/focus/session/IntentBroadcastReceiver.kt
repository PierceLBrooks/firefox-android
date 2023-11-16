/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.session

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Browser
import android.util.Log
import org.mozilla.focus.activity.IntentReceiverActivity

class IntentBroadcastReceiver(
    private val activity: IntentReceiverActivity,
    private val action: String,
    private val result: IntentProcessor.Result.CustomTab
) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            Log.i("CUSTOM_TAB", "INTENT = " + intent.action)
            if (action.equals(intent.action)) {
                if (result.equals(activity.dispatchCustomTabsIntent(result.id, result, intent.getBundleExtra(Browser.EXTRA_HEADERS)))) {
                    activity.finish()
                }
            }
        }
    }
}
