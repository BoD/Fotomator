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
package org.jraf.android.fotomator.app.slack

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import dagger.hilt.android.AndroidEntryPoint
import org.jraf.android.fotomator.R
import org.jraf.android.fotomator.databinding.SlackAuthActivityBinding
import org.jraf.android.util.log.Log
import org.jraf.android.util.string.StringUtil

@AndroidEntryPoint
class SlackAuthActivity : AppCompatActivity() {
    private val viewModel: SlackAuthViewModel by viewModels()
    private lateinit var binding: SlackAuthActivityBinding
    private var fromNewIntent: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.slack_auth_activity)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.toast.observe(this) { resId: Int? ->
            if (resId != null) Toast.makeText(this, resId, Toast.LENGTH_LONG).show()
        }

        viewModel.finish.observe(this) {
            finish()
        }

        Log.d("intent=${StringUtil.toString(intent)}")
        viewModel.checkForToken()
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
}