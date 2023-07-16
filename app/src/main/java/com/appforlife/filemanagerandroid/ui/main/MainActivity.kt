package com.appforlife.filemanagerandroid.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.appforlife.filemanagerandroid.base.SimpleActivity
import com.appforlife.filemanagerandroid.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : SimpleActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}