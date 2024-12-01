package com.chandra.practice.identifylanguageandtranslatetextwithmlkit

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.chandra.practice.identifylanguageandtranslatetextwithmlkit.databinding.ActivityMainBinding
import com.chandra.practice.identifylanguageandtranslatetextwithmlkit.databinding.FragmentMainBinding
import com.chandra.practice.identifylanguageandtranslatetextwithmlkit.fragment.MainFragment

class MainActivity : AppCompatActivity() {
    private lateinit var mainBinding : ActivityMainBinding
    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

       supportFragmentManager.beginTransaction().add(R.id.container , MainFragment()).commit()

    }
}