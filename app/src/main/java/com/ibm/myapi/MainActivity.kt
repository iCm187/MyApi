package com.ibm.myapi

import android.os.Bundle
import android.webkit.WebSettings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.ibm.myapi.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val idKey = "ID_KEY"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setViewItems()
    }

    fun setViewItems(){
        binding.btnRecherche.setOnClickListener{ search() }
        if (getIdFromSharedPreferences() != 0) {
            binding.editText.setText(getIdFromSharedPreferences().toString())
        }
    }

    private fun search() {
        if (binding.editText.text.toString().isEmpty()) {
            val alertDialog: AlertDialog = AlertDialog.Builder(this@MainActivity).create()
            alertDialog.setMessage("Veuillez saisir un entier")
            alertDialog.setButton(
                AlertDialog.BUTTON_NEUTRAL, "OK"
            ) { dialog, _ -> dialog.dismiss() }
            alertDialog.show()
            return
        }
        callService()
    }

    private fun callService() {
        val service: AlbumApi.AlbumService =
            AlbumApi().getClient().create(AlbumApi.AlbumService::class.java)
        val call: Call<List<Photo>> =
            service.getPhotos(Integer.valueOf(binding.editText.text.toString()))
        call.enqueue(object : Callback<List<Photo>> {
            override fun onResponse(call: Call<List<Photo>>, response: Response<List<Photo>>) {
                updateView(response)
                writeIdToSharePreferences()
            }
            override fun onFailure(call: Call<List<Photo>>, t: Throwable) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun updateView(response: Response<List<Photo>>) {
        response.body().let {
            if (!it.isNullOrEmpty()) {
                binding.title2.text = it[0].title
                //API https://via.placeholder.com requires user agent while dealing with images so we need a little addition before querying images
                val glideUrl = GlideUrl(
                    it[0].thumbnailUrl, LazyHeaders.Builder()
                        .addHeader(
                            "User-Agent",
                            WebSettings.getDefaultUserAgent(applicationContext)
                        )
                        .build()
                )
                Glide.with(applicationContext).load(glideUrl)
                    .into(binding.imageView)
            }else {
                Toast.makeText(this,"Aucun résultat", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun writeIdToSharePreferences() {
        getPreferences(MODE_PRIVATE)
            .edit()
            .putInt(idKey, Integer.valueOf(binding.editText.getText().toString()))
            .apply()
    }

    private fun getIdFromSharedPreferences(): Int {
        return getPreferences(MODE_PRIVATE).getInt(idKey, 0)
    }
}