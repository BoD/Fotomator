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
import android.os.Parcelable
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import org.jetbrains.annotations.Contract
import org.jraf.android.fotomator.app.slack.channel.SlackPickChannelActivity.Contract.Companion.EXTRA_RESULT
import org.jraf.android.fotomator.upload.client.slack.SlackChannel
import org.jraf.android.fotomator.upload.client.slack.SlackConversation
import org.jraf.android.fotomator.util.observeNonNull
import org.jraf.android.util.log.Log

@AndroidEntryPoint
class SlackPickChannelActivity : AppCompatActivity() {
    private val viewModel: SlackPickChannelViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val layoutState by viewModel.layoutState.observeAsState(SlackPickChannelLayoutState.Loading)
            SlackPickChannelLayout(
                state = layoutState,
                onBackClick = ::onBackPressed,
                onChannelClick = { channel ->
                    Log.d("channel=$channel")
                    setResult(
                        RESULT_OK,
                        Intent().putExtra(
                            EXTRA_RESULT,
                            Contract.PickChannelResult(
                                id = channel.id,
                                name = when (channel) {
                                    is SlackChannel -> "#${channel.name}"
                                    is SlackConversation -> channel.description
                                }
                            )
                        )
                    )
                    finish()
                },
                onSearchQueryChange = { query ->
                    Log.d("query=$query")
                    viewModel.updateSearchQuery(query)
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

    class Contract : ActivityResultContract<Unit, Contract.PickChannelResult?>() {
        companion object {
            const val EXTRA_RESULT = "EXTRA_RESULT"
        }

        @Parcelize
        data class PickChannelResult(val id: String, val name: String) : Parcelable

        override fun createIntent(context: Context, input: Unit) = Intent(context, SlackPickChannelActivity::class.java)
        override fun parseResult(resultCode: Int, intent: Intent?): PickChannelResult? = intent?.getParcelableExtra(EXTRA_RESULT)
    }
}