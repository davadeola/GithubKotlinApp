package com.sriyank.javatokotlindemo.extensions

import android.content.Context
import android.util.Log

import okhttp3.ResponseBody
import com.google.gson.GsonBuilder
import com.sriyank.javatokotlindemo.models.ErrorResponse
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.sriyank.javatokotlindemo.adapters.DisplayAdapter
import com.sriyank.javatokotlindemo.models.Repository
import kotlinx.android.synthetic.main.activity_display.*
import java.io.IOException



fun Context.showErrorMessage(errorBody: ResponseBody){
    val gson = GsonBuilder().create()

    try {
       val  errorResponse = gson.fromJson(errorBody.string(), ErrorResponse::class.java)
         toast( errorResponse.message!!)
    } catch (e: IOException) {
        Log.i("Exception ", e.toString())
    }
}

fun Context.toast(msg: String){
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}



fun RecyclerView.setUpRecyclerView(items: List<Repository>): DisplayAdapter {
    val displayAdapter = DisplayAdapter(context, items)
    adapter = displayAdapter
    return displayAdapter

}

