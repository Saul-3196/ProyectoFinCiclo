package com.example.proyectofinciclo.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.proyectofinciclo.R
import com.example.proyectofinciclo.databinding.FragmentDetalleRutaBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.text.SimpleDateFormat
import java.util.*

class DetalleRutaFragment : Fragment(R.layout.fragment_detalle_ruta), OnMapReadyCallback {

    private lateinit var binding: FragmentDetalleRutaBinding
    private var googleMap: GoogleMap? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDetalleRutaBinding.bind(view)

        // --- CAMBIO CLAVE: Usamos CustomSupportMapFragment ---
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapa_detalle) as CustomSupportMapFragment?

        mapFragment?.setListener(object : CustomSupportMapFragment.OnTouchListener {
            override fun onTouch() {
                // Al tocar el mapa, bloqueamos el NestedScrollView
                binding.root.requestDisallowInterceptTouchEvent(true)
            }
        })
        mapFragment?.getMapAsync(this)
        // ---------------------------------------------------

        actualizarBannerActividad()

        binding.tvVolverDetalle.setOnClickListener {
            findNavController().popBackStack()
        }

        // Recuperamos los datos de los argumentos
        val titulo = arguments?.getString("titulo") ?: ""
        val distancia = arguments?.getDouble("distancia") ?: 0.0
        val dificultad = arguments?.getString("dificultad") ?: ""
        val desnivelRecibido = arguments?.getInt("desnivel") ?: 0
        val localidad = arguments?.getString("localidad") ?: ""
        val fechaString = arguments?.getString("fecha") ?: ""
        val hora = arguments?.getString("hora") ?: ""
        val puntoEncuentro = arguments?.getString("puntoEncuentro") ?: ""
        val descripcion = arguments?.getString("descripción") ?: ""
        val idBici = arguments?.getInt("id_bici") ?: 0
        val idCreadorRuta = arguments?.getInt("id_creador") ?: 0
        val nombreOrganizador = arguments?.getString("creador") ?: "Usuario Desconocido"

        val sharedPrefs = requireContext().getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val idUsuarioLogueado = sharedPrefs.getInt("id_usuario", 0)

        val textoBici = when(idBici){
            1 -> "Carretera"
            2 -> "MTB"
            3 -> "Gravel"
            4 -> "E-Bike"
            else -> "Desconocido"
        }

        val esVigente = comprobarFecha(fechaString)

        binding.btnUnirseRuta.apply {
            when {
                !esVigente -> {
                    isEnabled = false
                    text = "Ruta finalizada"
                    setBackgroundColor(android.graphics.Color.GRAY)
                }
                idCreadorRuta == idUsuarioLogueado -> {
                    isEnabled = false
                    text = "Eres el organizador"
                    setBackgroundColor(android.graphics.Color.parseColor("#2196F3"))
                }
                else -> {
                    isEnabled = true
                    text = "Unirse a esta ruta"
                    setBackgroundColor(android.graphics.Color.parseColor("#FF9800"))
                    setOnClickListener {
                        text = "¡Te has unido!"
                        setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"))
                        isEnabled = false
                        Toast.makeText(requireContext(), "Inscripción realizada", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        binding.apply {
            tvDetalleTitulo.text = titulo
            tvDetalleKm.text = "📏 $distancia km"
            tvDetalleDesnivel.text = "⛰️ Desnivel: $desnivelRecibido m"
            tvDetalleDificultad.text = "📊 $dificultad"
            asignarColorDificultad(tvDetalleDificultad, dificultad)
            tvDetalleLocalidad.text = "📍 $localidad"
            tvDetalleFecha.text = "📅 $fechaString"
            tvDetalleHora.text = "⏰ $hora"
            tvDetallePunto.text = "🏁 Punto de encuentro: $puntoEncuentro"
            tvDetalleDescripcion.text = descripcion
            tvDetalleTipo.text = "🚴 $textoBici"
            tvDetalleCreador.text = "Organizado por: $nombreOrganizador"
        }
    }

    private fun asignarColorDificultad(textView: TextView, dificultad: String) {
        val color = when (dificultad.lowercase().trim()) {
            "baja" -> android.graphics.Color.parseColor("#4CAF50")
            "media" -> android.graphics.Color.parseColor("#FFEB3B")
            "alta" -> android.graphics.Color.parseColor("#FF9800")
            "muy alta" -> android.graphics.Color.parseColor("#F44336")
            "extrema" -> android.graphics.Color.parseColor("#4A148C")
            else -> android.graphics.Color.GRAY
        }
        textView.setTextColor(color)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        googleMap?.uiSettings?.apply {
            isZoomControlsEnabled = true
            isScrollGesturesEnabled = true
            isMapToolbarEnabled = true
        }

        //  Recuperamos coordenadas y el punto de encuentro real
        var lat = arguments?.getDouble("latitud") ?: 0.0
        var lon = arguments?.getDouble("longitud") ?: 0.0
        val tituloRuta = arguments?.getString("titulo") ?: "Ruta"
        val puntoEncuentroReal = arguments?.getString("puntoEncuentro") ?: "Punto de encuentro"

        //  Marcador y movimiento de cámara
        val posicionRuta = LatLng(lat, lon)

        googleMap?.clear() // Limpiamos marcadores previos para evitar duplicados

        googleMap?.addMarker(
            MarkerOptions()
                .position(posicionRuta)
                .title(puntoEncuentroReal) // Aquí saldrá "Oficina de Turismo de Luz"
                .snippet(tituloRuta)       // Subtítulo con el nombre de la ruta
        )

        // Centramos el mapa
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(posicionRuta, 15f))
    }

    private fun actualizarBannerActividad() {
        val sharedPrefs = requireContext().getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val idRol = sharedPrefs.getInt("id_rol", 2)
        val tvBanner = activity?.findViewById<TextView>(R.id.tvModoAdmin)
        if (idRol == 1) {
            tvBanner?.visibility = View.VISIBLE
        } else {
            tvBanner?.visibility = View.GONE
        }
    }

    private fun comprobarFecha(fechaRuta: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val dateRuta = sdf.parse(fechaRuta)
            val hoy = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            dateRuta != null && !dateRuta.before(hoy)
        } catch (e: Exception) {
            true
        }
    }
}