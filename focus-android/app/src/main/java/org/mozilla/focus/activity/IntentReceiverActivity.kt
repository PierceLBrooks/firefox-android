/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.activity

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Browser
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import mozilla.components.feature.intent.ext.sanitize
import mozilla.components.feature.search.widget.BaseVoiceSearchActivity
import mozilla.components.support.utils.toSafeIntent
import org.mozilla.focus.ext.components
import org.mozilla.focus.session.IntentBroadcastReceiver
import org.mozilla.focus.session.IntentProcessor
import org.mozilla.focus.utils.SupportUtils

/**
 * This activity receives VIEW intents and either forwards them to MainActivity or CustomTabActivity.
 */
class IntentReceiverActivity : Activity() {
    private val intentProcessor by lazy {
        IntentProcessor(this, components.tabsUseCases, components.customTabsUseCases)
    }

    private var intentBroadcastReceiver: IntentBroadcastReceiver? = null
    private var headersAction: String? = null
    private var headersPermission: String? = null
    private var result: IntentProcessor.Result? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent.sanitize().toSafeIntent()

        if (intent.dataString.equals(SupportUtils.OPEN_WITH_DEFAULT_BROWSER_URL)) {
            dispatchNormalIntent()
            return
        }

        var result = intentProcessor.handleIntent(this, intent, savedInstanceState)
        this.result = result
        if (result is IntentProcessor.Result.CustomTab) {
            if (!result.equals(dispatchCustomTabsIntent(result.id, result))) {
                this.result = null
            }
        } else {
            if (!result?.equals(dispatchNormalIntent(result))!!) {
                this.result = null
            }
        }

        if (this.result != null) {
            finish()
        } else {
            this.result = result
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (intentBroadcastReceiver != null) {
            unregisterReceiver(intentBroadcastReceiver)
            intentBroadcastReceiver = null
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != 0) {
            return
        }
        if (permissions.size == grantResults.size) {
            for (idx in permissions.indices) {
                if (permissions[idx] == headersPermission) {
                    if (grantResults[idx] == PackageManager.PERMISSION_GRANTED) {
                        dispatchHeadersIntent()
                    } else {
                        result = intentProcessor.handleIntent(this, intent.sanitize().toSafeIntent(), null)
                        if (result is IntentProcessor.Result.CustomTab) {
                            if (!result?.equals(dispatchCustomTabsIntent((result as IntentProcessor.Result.CustomTab).id, result))!!) {
                                result = null
                            }
                        } else {
                            if (!result?.equals(dispatchNormalIntent(result))!!) {
                                result = null
                            }
                        }
                        finish()
                    }
                    break
                }
            }
        }
    }

    fun dispatchCustomTabsIntent(tabId: String, result: IntentProcessor.Result? = null, headers: Bundle? = null): IntentProcessor.Result? {
        val intent = Intent(intent)
        if (headers == null && dispatchHeadersIntent(intent)) {
            return null
        }

        Log.i("CUSTOM_TAB", "EXTRA_HEADERS = " + headers);

        intent.setClassName(applicationContext, CustomTabActivity::class.java.name)

        // We are adding a generated custom tab ID to the intent here. CustomTabActivity will
        // use this ID to later decide what session to display once it is created.
        intent.putExtra(CustomTabActivity.CUSTOM_TAB_ID, tabId)
        if (headers != null) {
            intent.putExtra(Browser.EXTRA_HEADERS, headers)
        }

        startActivity(intent)

        return result
    }

    private fun dispatchNormalIntent(result: IntentProcessor.Result? = null): IntentProcessor.Result? {
        val intent = Intent(intent)
        intent.setClassName(applicationContext, MainActivity::class.java.name)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        intent.putExtra(SEARCH_WIDGET_EXTRA, intent.getBooleanExtra(SEARCH_WIDGET_EXTRA, false))
        intent.putExtra(
            BaseVoiceSearchActivity.SPEECH_PROCESSING,
            intent.getStringExtra(BaseVoiceSearchActivity.SPEECH_PROCESSING),
        )
        startActivity(intent)
        return result
    }

    private fun dispatchHeadersIntent(intent: Intent? = null): Boolean {
        var headersAction = ""
        var headersPermission = ""
        if (intent != null) {
            headersAction += intent.getStringExtra(Browser.EXTRA_HEADERS + ".action")
            headersPermission += intent.getStringExtra(Browser.EXTRA_HEADERS + ".permission")
        } else {
            headersAction += this.headersAction
            headersPermission += this.headersPermission
        }
        if (headersAction.equals("" + null)) {
            headersAction = ""
        }
        if (headersPermission.equals("" + null)) {
            headersPermission = ""
        }
        if (headersAction.isNotEmpty() && headersPermission.isNotEmpty()) {
            this.headersAction = headersAction
            this.headersPermission = headersPermission
            if (ContextCompat.checkSelfPermission(this, headersPermission) == PackageManager.PERMISSION_GRANTED) {
                val broadcastIntent = Intent(headersAction)
                val intentFilter = IntentFilter()
                intentFilter.addAction(Browser.EXTRA_HEADERS + ".response")
                intentFilter.addCategory(Intent.CATEGORY_DEFAULT)
                broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT)
                if (intentBroadcastReceiver == null) {
                    Log.i("CUSTOM_TAB", "ACTION = " + headersAction)
                    Log.i("CUSTOM_TAB", "PERMISSION = " + headersPermission)
                    intentBroadcastReceiver = IntentBroadcastReceiver(this, Browser.EXTRA_HEADERS + ".response", result as IntentProcessor.Result.CustomTab)
                    ContextCompat.registerReceiver(this, intentBroadcastReceiver, intentFilter, ContextCompat.RECEIVER_EXPORTED)
                }
                sendBroadcast(broadcastIntent, headersPermission)
            } else {
                ActivityCompat.requestPermissions(this, Array<String>(1) { _: Int -> headersPermission }, 0)
            }
            return true
        }
        return false
    }

    companion object {
        const val SEARCH_WIDGET_EXTRA = "search_widget_extra"
    }
}
