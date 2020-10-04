package org.jraf.android.fotomator.app.slack

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.jraf.android.fotomator.R
import org.jraf.android.fotomator.databinding.SlackAuthActivityBinding
import org.jraf.android.util.log.Log
import org.jraf.android.util.string.StringUtil

class SlackAuthActivity : AppCompatActivity() {
    private val viewModel: SlackAuthViewModel by viewModels()
    private lateinit var binding: SlackAuthActivityBinding

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
        val redirectUri = intent.data
        if (redirectUri == null) {
            Log.e("redirectUri is null, this should never happen")
            finish()
            return
        }
        viewModel.handleRedirectUri(redirectUri)
    }
}