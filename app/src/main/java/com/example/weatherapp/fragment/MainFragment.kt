package com.example.weatherapp.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationRequest
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weatherapp.MainViewModel
import com.example.weatherapp.R
import com.example.weatherapp.adapters.VpAdapter
import com.example.weatherapp.adapters.WeatherModel
import com.example.weatherapp.databinding.FragmentMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.tabs.TabLayoutMediator
import com.squareup.picasso.Picasso
import org.json.JSONObject
import kotlin.math.max

const val API_KEY = "59d408d64b214d60857154301220710"

class MainFragment : Fragment() {
    private lateinit var fLocationClient: FusedLocationProviderClient
    private val fList = listOf(
        HoursFragment.newInstance(),
        DaysFragment.newInstance()

    )
    private val tList = listOf(
        "HOURS",
        "DAYS"
    )

    private lateinit var pLauncher: ActivityResultLauncher<String>
    lateinit var binding: FragmentMainBinding
    private val model: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermission()
        init()
        getLocation()
        updateCurrentCard()
    }

    private fun init() = with(binding) {
        fLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val adapter = VpAdapter(activity as FragmentActivity, fList)
        vP.adapter = adapter
        TabLayoutMediator(tabLayout, vP) { tab, pos ->
            tab.text = tList[pos]
        }.attach()
        ibSync.setOnClickListener {
            tabLayout.selectTab(tabLayout.getTabAt(0))
            getLocation()
        }
    }

    private fun getLocation() {
        val ct = CancellationTokenSource()
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fLocationClient.getCurrentLocation(
            com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY,
            ct.token
        ).addOnCompleteListener {
            requestWeatherData("${it.result.latitude},${it.result.longitude}")
        }
    }

    // Выводим данные на вверхнюю карту с информацией
    private fun updateCurrentCard() = with(binding) {
        model.liveDataCurrent.observe(viewLifecycleOwner) {
            val maxMinTemp = "${it.maxTemp}°C/${it.minTemp}°C"
            tvData.text = it.time
            tvCity.text = it.city
            tvCurrentTemp.text = it.currentTemp.ifEmpty { maxMinTemp }
            tvCondition.text = it.condition
            tvMaxMin.text = if (it.currentTemp.isEmpty()) "" else maxMinTemp
            Picasso.get().load("https:" + it.imageUrl).into(imWeather)


        }
    }

    // показываем true or false on request location
    private fun permissionListener() {
        pLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            Toast.makeText(activity, "Permission is $it", Toast.LENGTH_LONG).show()
        }
    }

    // проверяем есть ли разрешение на местоположение, если нет выводим пользователю окно с вариантами для получения разрешения, если есть ничего не делаем
    private fun checkPermission() {
        if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissionListener()
            pLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }// дописать если нет выводить определенный город
    }

    //Получение данных через API
    private fun requestWeatherData(city: String) {
        val url =
            "https://api.weatherapi.com/v1/forecast.json?key=$API_KEY&q=$city&days=8&aqi=no&alerts=no"

        val queue = Volley.newRequestQueue(context)
        val request = StringRequest(
            Request.Method.GET,
            url,
            { result ->
                parseWeatherData(result)
                //Log.d("MyLog", "Result: $result")


            }, { error ->
                Log.d("MyLog", "Error:$error")

            }
        )
        queue.add(request)
    }

    private fun parseWeatherData(result: String) {
        val mainObject = JSONObject(result)
        val list = parseDays(mainObject)
        parseCurrentData(mainObject, list[0])

    }

    //Получение данных через API, погоду по дням
    private fun parseDays(mainObject: JSONObject): List<WeatherModel> {
        val list = ArrayList<WeatherModel>()
        val daysArray = mainObject.getJSONObject("forecast").getJSONArray("forecastday")
        val name = mainObject.getJSONObject("location").getString("name")

        for (i in 0 until daysArray.length()) {
            val day = daysArray[i] as JSONObject

            val item = WeatherModel(
                city = name,
                time = day.getString("date"),
                condition = day.getJSONObject("day").getJSONObject("condition").getString("text"),
                currentTemp = "",
                maxTemp = day.getJSONObject("day").getString("maxtemp_c").toFloat().toInt()
                    .toString(),
                minTemp = day.getJSONObject("day").getString("mintemp_c").toFloat().toInt()
                    .toString(),
                imageUrl = day.getJSONObject("day").getJSONObject("condition").getString("icon"),
                hours = day.getJSONArray("hour").toString()

            )
            list.add(item)
        }
        model.liveDataList.value = list
        return list
    }

    //Получение данных через API, погоду на выбранный день (или нынешний день)
    private fun parseCurrentData(mainObject: JSONObject, weatherItem: WeatherModel) {
        val item = WeatherModel(
            city = mainObject.getJSONObject("location").getString("name"),
            time = mainObject.getJSONObject("current").getString("last_updated"),
            condition = mainObject.getJSONObject("current").getJSONObject("condition")
                .getString("text"),
            currentTemp = mainObject.getJSONObject("current").getString("temp_c"),
            imageUrl = mainObject.getJSONObject("current").getJSONObject("condition")
                .getString("icon"),
            maxTemp = weatherItem.maxTemp,
            minTemp = weatherItem.minTemp,
            hours = weatherItem.hours
        )
        model.liveDataCurrent.value = item
        /* Log.d("MyLog", "maxTemp: ${item.maxTemp} ")
         Log.d("MyLog", "minTemp: ${item.minTemp} ")
         Log.d("MyLog", "hours: ${item.hours} ")*/

    }


    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}