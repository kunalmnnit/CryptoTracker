package com.kunal.cryptotracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kunal.cryptotracker.Adapter.CoinAdapter
import com.kunal.cryptotracker.Common.Common
import com.kunal.cryptotracker.Interface.ILoadMore
import com.kunal.cryptotracker.Model.CoinModel
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException

class MainActivity : AppCompatActivity(),ILoadMore {
    internal  var items:MutableList<CoinModel> = ArrayList()
    internal lateinit var adapter: CoinAdapter
    internal lateinit var client:OkHttpClient
    internal lateinit var request:Request
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        swipe_to_refresh.post{
            loadFirst10Coin()
        }
        swipe_to_refresh.setOnRefreshListener {
            items.clear()
            loadFirst10Coin()
            setUpAdapter()
        }
        coin_recycler_view.layoutManager = LinearLayoutManager(this)
        setUpAdapter()
    }

    private fun setUpAdapter() {
        adapter = CoinAdapter(coin_recycler_view,this@MainActivity,items)
        coin_recycler_view.adapter = adapter
        adapter.setLoadMore(this)
    }

    override fun onLoadMore() {
        if(items.size<=Common.MAX_COIN_LOAD)
            loadNext10Coin(items.size)
        else
            Toast.makeText(this@MainActivity,"Data max is ${Common.MAX_COIN_LOAD}",Toast.LENGTH_SHORT).show()
    }

    private fun loadNext10Coin(size: Int) {
        client = OkHttpClient()
        request = Request.Builder().header("X-CMC_PRO_API_KEY",Common.API_KEY)
            .url(String.format("https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest?start=%d&limit=10",size)).build()
        client.newCall(request)
            .enqueue(object :Callback
            {
                override fun onFailure(call: Call, e: IOException) {
                    Log.d("ERROR",e.toString())
                }

                override fun onResponse(call: Call, response: Response) {
                   val body = response.body!!.string()
                    val gson = Gson()
                    val newItems = gson.fromJson<List<CoinModel>>(body,object:TypeToken<List<CoinModel>>(){}.type)
                    runOnUiThread {
                        items.addAll(newItems)
                        adapter.setLoaded()
                        adapter.updateData(items)

                    }
                }

            })
    }
    private fun loadFirst10Coin() {
        client = OkHttpClient()
        request = Request.Builder().header("X-CMC_PRO_API_KEY",Common.API_KEY)
            .url(String.format("https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest?start=1&limit=10")).build()
        swipe_to_refresh.isRefreshing=true
        client.newCall(request)
            .enqueue(object :Callback
            {
                override fun onFailure(call: Call, e: IOException) {
                    Log.d("ERROR",e.toString())
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body!!.string()
                    val gson = Gson()
                    items = gson.fromJson(body,object:TypeToken<List<CoinModel>>(){}.type)
                    runOnUiThread {
                        adapter.setLoaded()
                        adapter.updateData(items)

                        swipe_to_refresh.isRefreshing = false
                    }
                }

            })
    }
}