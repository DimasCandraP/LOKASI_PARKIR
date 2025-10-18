package com.example.mylocation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mylocation.databinding.ActivityDetailParkirBinding

class DetailParkirActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailParkirBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailParkirBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val nama = intent.getStringExtra("nama")
        val koordinat = intent.getStringExtra("koordinat")
        val status = intent.getStringExtra("status")

        binding.txtNamaLokasi.text = nama
        binding.txtKoordinat.text = "Koordinat: $koordinat"
        binding.txtStatus.text = "Status: $status"
        binding.txtTarif.text = "Tarif: Rp 3.000 / jam"
        binding.txtJam.text = "Jam Operasional: 07.00 - 22.00"
    }
}
