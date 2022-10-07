package com.example.weatherapp.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import com.example.weatherapp.MainViewModel
import com.example.weatherapp.R
import com.example.weatherapp.adapters.WeatherAdapter
import com.example.weatherapp.adapters.WeatherModel
import com.example.weatherapp.databinding.FragmentHoursBinding
import com.example.weatherapp.databinding.FragmentMainBinding
import org.json.JSONArray
import org.json.JSONObject

class HoursFragment : Fragment() {
    private lateinit var binding: FragmentHoursBinding
    private lateinit var adapter: WeatherAdapter
    private val model: MainViewModel by activityViewModels()

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
        updateHoursCard()
    }

    private fun updateHoursCard(){
        model.liveDataCurrent.observe(viewLifecycleOwner){
            adapter.submitList(getHoursList(it))
        }
    }

  // заполняем RCView
    private fun initRcView() = with(binding) {
        rcView.layoutManager = LinearLayoutManager(activity)
        adapter = WeatherAdapter(null)
        rcView.adapter = adapter

       /* val list = listOf(
            WeatherModel(
                city = "",
                time = "12:00",
                condition = "Sunny",
                currentTemp = "25°C",
                maxTemp = "",
                minTemp = "",
                imageUrl = "",
                hours = ""
            )

        )
        adapter.submitList(list)*/
    }

    private fun getHoursList(wItem: WeatherModel):List<WeatherModel>{
        val hoursArray = JSONArray(wItem.hours)
        val list = ArrayList<WeatherModel>()
        for (i in 0 until hoursArray.length()){
            val item = WeatherModel(
                city = "",
                time = (hoursArray[i] as JSONObject).getString("time"),
                condition = (hoursArray[i] as JSONObject).getJSONObject("condition").getString("text"),
                currentTemp = (hoursArray[i] as JSONObject).getString("temp_c"),
                maxTemp = "",
                minTemp = "",
                imageUrl = (hoursArray[i] as JSONObject).getJSONObject("condition").getString("icon"),
                hours = ""

            )
            list.add(item)
        }
        return list
    }

    companion object {
        @JvmStatic
        fun newInstance() = HoursFragment()

    }
}