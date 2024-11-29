package com.chandra.practice.identifylanguageandtranslatetextwithmlkit.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.chandra.practice.identifylanguageandtranslatetextwithmlkit.databinding.FragmentMainBinding


class MainFragment : Fragment() {
    private lateinit var mainBinding : FragmentMainBinding
    override fun onCreateView(
        inflater : LayoutInflater , container : ViewGroup? ,
        savedInstanceState : Bundle? ,
                             ) : View {
        mainBinding = FragmentMainBinding.inflate(layoutInflater)
        return mainBinding.root
    }

}