package com.example.component

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.component.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initCircle()
    }

    private fun initCircle() {
        binding.circle.apply {
            configurationList = Circle.Config(
                initValue = 0,
                maxValue = 100,
                lockMode = false,
                useIcon = false,
                colorSector = Circle.ColorState.defaultColorSector
            )
        }

    }
}