package com.example.mylocation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.mylocation.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlin.random.Random

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var myLocation: Location? = null
    private val FINE_PERMISSION_CODE = 1

    // 🅿️ Daftar lokasi parkir tetap di Yogyakarta
    private val lokasiParkir = listOf(
        Triple("Parkir Malioboro", LatLng(-7.7956, 110.3695), "Masih Ada"),
        Triple("Parkir Abu Bakar Ali", LatLng(-7.7895, 110.3670), "Penuh"),
        Triple("Parkir Ngabean", LatLng(-7.8032, 110.3602), "Masih Ada"),
        Triple("Parkir Senopati", LatLng(-7.7970, 110.3710), "Masih Ada"),
        Triple("Parkir Kridosono", LatLng(-7.7848, 110.3772), "Penuh"),
        Triple("Parkir XT Square", LatLng(-7.8145, 110.3840), "Masih Ada"),
        Triple("Parkir Gembira Loka", LatLng(-7.7999, 110.3983), "Masih Ada"),
        Triple("Parkir Taman Pintar", LatLng(-7.8009, 110.3690), "Penuh"),
        Triple("Parkir Stasiun Tugu", LatLng(-7.7891, 110.3633), "Masih Ada"),
        Triple("Parkir Alun-Alun Utara", LatLng(-7.8039, 110.3649), "Penuh")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        getMyLocation()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Tombol arahkan ke Malioboro
        binding.btnArahkan.setOnClickListener {
            val gmmIntentUri = Uri.parse("geo:-7.7956,110.3695?q=Tempat+Parkir+Malioboro")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")

            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            } else {
                Toast.makeText(this, "Google Maps tidak ditemukan di perangkat", Toast.LENGTH_SHORT).show()
            }
        }

        // Zoom control
        binding.btnZoomIn.setOnClickListener { mMap.animateCamera(CameraUpdateFactory.zoomIn()) }
        binding.btnZoomOut.setOnClickListener { mMap.animateCamera(CameraUpdateFactory.zoomOut()) }
    }

    private fun getMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                FINE_PERMISSION_CODE
            )
            return
        }
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
            myLocation = it
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // 🔹 Tampilkan semua lokasi parkir tetap
        for (lokasi in lokasiParkir) {
            val warna = if (lokasi.third == "Masih Ada")
                BitmapDescriptorFactory.HUE_GREEN else BitmapDescriptorFactory.HUE_RED

            val marker = mMap.addMarker(
                MarkerOptions()
                    .position(lokasi.second)
                    .title(lokasi.first)
                    .snippet("Status: ${lokasi.third}")
                    .icon(BitmapDescriptorFactory.defaultMarker(warna))
            )
            marker?.tag = lokasi
        }

        // Fokus ke Yogyakarta
        val yogyakarta = LatLng(-7.7956, 110.3695)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(yogyakarta, 14f))

        // 🔹 Klik marker → halaman detail parkir
        mMap.setOnMarkerClickListener { marker ->
            val data = marker.tag as? Triple<String, LatLng, String>
            data?.let {
                val intent = Intent(this, DetailParkirActivity::class.java)
                intent.putExtra("nama", it.first)
                intent.putExtra("koordinat", "${it.second.latitude}, ${it.second.longitude}")
                intent.putExtra("status", it.third)
                startActivity(intent)
            }
            true
        }

        // 🔹 Klik lokasi bebas di peta → tampilkan info parkir dinamis
        mMap.setOnMapClickListener { latLng ->
            // Tambahkan marker sementara
            mMap.clear()
            for (lokasi in lokasiParkir) {
                val warna = if (lokasi.third == "Masih Ada")
                    BitmapDescriptorFactory.HUE_GREEN else BitmapDescriptorFactory.HUE_RED
                mMap.addMarker(
                    MarkerOptions()
                        .position(lokasi.second)
                        .title(lokasi.first)
                        .snippet("Status: ${lokasi.third}")
                        .icon(BitmapDescriptorFactory.defaultMarker(warna))
                )
            }

            mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Parkir Manual")
                    .snippet("Lokasi sekitar")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            )

            // Simulasi data dinamis
            val status = if (Random.nextBoolean()) "Masih Ada" else "Penuh"
            val tarif = if (Random.nextBoolean()) "Rp 3.000 / jam" else "Rp 5.000 / jam"
            val jam = "07.00 - 22.00"

            val intent = Intent(this, DetailParkirActivity::class.java)
            intent.putExtra("nama", "Parkir Sekitar")
            intent.putExtra("koordinat", "${latLng.latitude}, ${latLng.longitude}")
            intent.putExtra("status", status)
            intent.putExtra("tarif", tarif)
            intent.putExtra("jam", jam)
            startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getMyLocation()
            } else {
                Toast.makeText(this, "Izin lokasi tidak diberikan", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
