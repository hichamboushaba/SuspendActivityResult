package com.hicham.activityresult.custom

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hicham.activityresult.databinding.ActivityResultBinding
import kotlin.random.Random

class ResultActivity : AppCompatActivity() {
    companion object {
        const val RESULT_EXTRA_KEY = "result_extra"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.returnResultButton.setOnClickListener {
            setResult(Activity.RESULT_OK, Intent().apply {
                putExtra(RESULT_EXTRA_KEY, Random.nextInt(0, Int.MAX_VALUE))
            })
            finish()
        }
    }
}