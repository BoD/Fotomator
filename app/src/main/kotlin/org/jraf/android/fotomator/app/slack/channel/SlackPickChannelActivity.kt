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

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import dagger.hilt.android.AndroidEntryPoint
import org.jraf.android.fotomator.R
import org.jraf.android.fotomator.databinding.SlackPickChannelActivityBinding
import org.jraf.android.util.log.Log

@AndroidEntryPoint
class SlackPickChannelActivity : AppCompatActivity() {
    private val viewModel: SlackPickChannelViewModel by viewModels()
    private lateinit var binding: SlackPickChannelActivityBinding

    private val adapter = SlackPickChannelAdapter { channelName ->
        Log.d("channelName=$channelName")
        setResult(RESULT_OK, Intent().putExtra(EXTRA_CHANNEL_NAME, channelName))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.slack_pick_channel_activity)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        binding.rclChannels.adapter = adapter

        viewModel.channelList.observe(this) { channelList ->
            adapter.submitList(channelList.map { SlackChannelUiModel(it) })
        }

        viewModel.toast.observe(this) { resId: Int? ->
            if (resId != null) Toast.makeText(this, resId, Toast.LENGTH_LONG).show()
        }

        viewModel.finishWithError.observe(this) {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    companion object {
        private const val EXTRA_CHANNEL_NAME = "EXTRA_CHANNEL_NAME"

        fun getPickedChannelName(intent: Intent): String? = intent.getStringExtra(EXTRA_CHANNEL_NAME)
    }
}