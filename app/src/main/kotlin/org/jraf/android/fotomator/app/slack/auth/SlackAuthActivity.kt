/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2020-present Benoit 'BoD' Lubek (BoD@JRAF.org)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jraf.android.fotomator.app.slack.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import dagger.hilt.android.AndroidEntryPoint
import org.jraf.android.fotomator.util.observeNonNull
import org.jraf.android.util.log.Log
import org.jraf.android.util.string.StringUtil

@AndroidEntryPoint
class SlackAuthActivity : AppCompatActivity() {
    private val viewModel: SlackAuthViewModel by viewModels()
    private var fromNewIntent: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val isLoading by viewModel.isLoading.observeAsState(false)
            SlackAuthLayout(
                isLoading = isLoading,
                onStartAuthenticationClick = viewModel::startAuthentication,
            )
        }

        viewModel.toast.observeNonNull(this) { resId ->
            Toast.makeText(this, resId, Toast.LENGTH_LONG).show()
        }

        viewModel.finishWithSuccess.observe(this) {
            setResult(RESULT_OK)
            finish()
        }

        Log.d("intent=${StringUtil.toString(intent)}")
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("intent=${StringUtil.toString(intent)}")
        fromNewIntent = true
        val redirectUri = intent.data
        if (redirectUri == null) {
            Log.e("redirectUri is null, this should never happen")
            finish()
            return
        }
        viewModel.handleRedirectUri(redirectUri)
    }

    override fun onStart() {
        super.onStart()
        if (!fromNewIntent) {
            viewModel.setLoading(false)
        }
        fromNewIntent = false
        Log.d()
    }

    companion object {
        val CONTRACT = object : ActivityResultContract<Unit, Boolean>() {
            override fun createIntent(context: Context, input: Unit) = Intent(context, SlackAuthActivity::class.java)
            override fun parseResult(resultCode: Int, intent: Intent?) = resultCode == RESULT_OK
        }
    }
}