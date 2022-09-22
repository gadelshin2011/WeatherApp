package com.example.weatherapp.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import com.example.weatherapp.R
import com.example.weatherapp.adapters.WeatherAdapter
import com.example.weatherapp.adapters.WeatherModel
import com.example.weatherapp.databinding.FragmentHoursBinding
import com.example.weatherapp.databinding.FragmentMainBinding

class HoursFragment : Fragment() {
    private lateinit var binding: FragmentHoursBinding
    private lateinit var adapter: WeatherAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHoursBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRcView()
    }
  // заполняем RCView
    private fun initRcView() = with(binding) {
        rcView.layoutManager = LinearLayoutManager(activity)
        adapter = WeatherAdapter()
        rcView.adapter = adapter

        val list = listOf(
            WeatherModel(
                city = "",
                time = "12:00",
                condition = "Sunny",
                currentTemp = "25°C",
                maxTemp = "",
                minTemp = "",
                imageUrl = "",
                hours = ""
            ),
            WeatherModel(
                city = "",
                time = "13:00",
                condition = "Sunny",
                currentTemp = "26°C",
                maxTemp = "",
                minTemp = "",
                imageUrl = "",
                hours = ""
            ),
            WeatherModel(
                city = "",
                time = "14:00",
                condition = "Sunny",
                currentTemp = "29°C",
                maxTemp = "",
                minTemp = "",
                imageUrl = "",
                hours = ""
            ),
            WeatherModel(
                city = "",
                time = "15:00",
                condition = "Sunny",
                currentTemp = "34°C",
                maxTemp = "",
                minTemp = "",
                imageUrl = "",
                hours = ""
            )
        )

        adapter.submitList(list)
    }

    companion object {
        @JvmStatic
        fun newInstance() = HoursFragment()

    }
}