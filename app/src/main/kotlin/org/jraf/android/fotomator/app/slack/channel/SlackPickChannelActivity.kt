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
package org.jraf.android.fotomator.app.slack.channel

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
import org.jraf.android.fotomator.R
import org.jraf.android.fotomator.util.observeNonNull
import org.jraf.android.util.log.Log

@AndroidEntryPoint
class SlackPickChannelActivity : AppCompatActivity() {
    private val viewModel: SlackPickChannelViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar!!.subtitle = getString(R.string.slack_pick_channel_subtitle)

        setContent {
            val layoutState by viewModel.layoutState.observeAsState(SlackPickChannelLayoutState.Loading)
            SlackPickChannelLayout(
                state = layoutState,
                onChannelClick = { channel ->
                    Log.d("channel=$channel")
                    setResult(RESULT_OK, Intent().putExtra(EXTRA_CHANNEL_NAME, channel.name))
                    finish()
                }
            )
        }

        viewModel.toast.observeNonNull(this) { resId ->
            Toast.makeText(this, resId, Toast.LENGTH_LONG).show()
        }

        viewModel.finishWithError.observe(this) {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    companion object {
        private const val EXTRA_CHANNEL_NAME = "EXTRA_CHANNEL_NAME"

        val CONTRACT = object : ActivityResultContract<Unit, String?>() {
            override fun createIntent(context: Context, input: Unit?) = Intent(context, SlackPickChannelActivity::class.java)
            override fun parseResult(resultCode: Int, intent: Intent?) = intent?.getStringExtra(EXTRA_CHANNEL_NAME)
        }
    }
}