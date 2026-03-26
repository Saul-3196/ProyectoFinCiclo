package com.example.proyectofinciclo.ui

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.proyectofinciclo.R
import com.example.proyectofinciclo.databinding.FragmentDetalleRutaBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import java.text.SimpleDateFormat
import java.util.*

class DetalleRutaFragment : Fragment(R.layout.fragment_detalle_ruta), OnMapReadyCallback {

    private lateinit var binding: FragmentDetalleRutaBinding
    private var googleMap: GoogleMap? = null
    private val TAMANO_ICONO = 80

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDetalleRutaBinding.bind(view)

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapa_detalle) as CustomSupportMapFragment?
        mapFragment?.setListener(object : CustomSupportMapFragment.OnTouchListener {
            override fun onTouch() { binding.root.requestDisallowInterceptTouchEvent(true) }
        })
        mapFragment?.getMapAsync(this)
        actualizarBannerActividad()

        binding.tvVolverDetalle.setOnClickListener { findNavController().popBackStack() }

        // Recuperación de datos de argumentos
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
        val nombreOrganizador = arguments?.getString("creador") ?: "Usuario"


        val textoBici = when(idBici){ 1 -> "Carretera"; 2 -> "MTB"; 3 -> "Gravel"; 4 -> "E-Bike"; else -> "Bici" }
        val esVigente = comprobarFecha(fechaString)

        binding.btnUnirseRuta.apply {
            if (!esVigente) { isEnabled = false; text = "Ruta finalizada"; setBackgroundColor(Color.GRAY) }
            else if (idCreadorRuta == requireContext().getSharedPreferences("preferences", Context.MODE_PRIVATE).getInt("id_usuario", 0)) {
                isEnabled = false; text = "Eres el organizador"; setBackgroundColor(Color.parseColor("#2196F3"))
            } else { setOnClickListener { text = "¡Te has unido a la ruta!"; isEnabled = false; setBackgroundColor(Color.parseColor("#4CAF50")) } }
        }

        binding.apply {
            tvDetalleTitulo.text = titulo; tvDetalleKm.text = "📏 $distancia km"; tvDetalleDesnivel.text = "⛰️ $desnivelRecibido m"
            tvDetalleDificultad.text = "📊 $dificultad"; asignarColorDificultad(tvDetalleDificultad, dificultad)
            tvDetalleLocalidad.text = "📍 $localidad"; tvDetalleFecha.text = "📅 $fechaString"; tvDetalleHora.text = "⏰ $hora"
            tvDetallePunto.text = "🏁 $puntoEncuentro"; tvDetalleDescripcion.text = descripcion; tvDetalleTipo.text = "🚴 $textoBici"
            tvDetalleCreador.text = "Por: $nombreOrganizador"
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.clear()

        // LIMPIEZA EXTREMA: Quitamos espacios en blanco y comillas que puedan romper el código
        val mapaCodificado = arguments?.getString("mapa")?.trim()?.replace("\"", "") ?: ""

        if (mapaCodificado.isNotEmpty()) {
            try {
                val puntos = PolyUtil.decode(mapaCodificado)
                if (puntos.isNotEmpty()) {
                    // Dibujamos con bordes redondeados para suavizar las uniones
                    googleMap?.addPolyline(PolylineOptions()
                        .addAll(puntos)
                        .color(Color.parseColor("#FF9800"))
                        .width(14f)
                        .jointType(JointType.ROUND)
                        .startCap(RoundCap())
                        .endCap(RoundCap()))

                    // Marcadores en los extremos reales del trazado
                    googleMap?.addMarker(MarkerOptions().position(puntos.first())
                        .icon(getCircularBitmapDescriptor(requireContext(), R.drawable.bandera_salida, TAMANO_ICONO))
                        .anchor(0.5f, 0.5f))

                    googleMap?.addMarker(MarkerOptions().position(puntos.last())
                        .icon(getCircularBitmapDescriptor(requireContext(), R.drawable.bandera_llegada, TAMANO_ICONO))
                        .anchor(0.5f, 0.5f))

                    // --- ZOOM DINÁMICO ---
                    val builder = LatLngBounds.Builder()
                    puntos.forEach { builder.include(it) }

                    // Usamos el callback de carga para que el zoom sea perfecto
                    googleMap?.setOnMapLoadedCallback {
                        try {
                            googleMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 150))
                        } catch (e: Exception) {
                            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(puntos.first(), 15f))
                        }
                    }
                }
            } catch (e: Exception) {
                // CHIVATO DE ERROR: Si entra aquí, el String llegó cortado desde el HomeFragment
                android.util.Log.e("MAPA_DETALLE", "Error decodificando la Polyline: ${e.message}")
                Toast.makeText(requireContext(), "Error visualizando el trazado completo", Toast.LENGTH_LONG).show()

                // Fallback de emergencia
                val lat = arguments?.getDouble("latitud") ?: 0.0
                val lon = arguments?.getDouble("longitud") ?: 0.0

                if (lat != 0.0 && lon != 0.0) {
                    googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), 15f))
                }
            }
        } else {
            Toast.makeText(requireContext(), "No se recibió información del trazado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCircularBitmapDescriptor(context: Context, resId: Int, size: Int): BitmapDescriptor? {
        val drawable = ContextCompat.getDrawable(context, resId) ?: return null
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap); drawable.setBounds(0, 0, canvas.width, canvas.height); drawable.draw(canvas)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val outCanvas = Canvas(output); val paint = Paint().apply { isAntiAlias = true }
        val rect = Rect(0, 0, size, size); val rectF = RectF(rect)
        outCanvas.drawOval(rectF, paint); paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        outCanvas.drawBitmap(bitmap, null, rect, paint); paint.xfermode = null; paint.style = Paint.Style.STROKE
        paint.color = Color.WHITE; paint.strokeWidth = 4f; outCanvas.drawOval(rectF, paint)
        return BitmapDescriptorFactory.fromBitmap(output)
    }

    private fun asignarColorDificultad(textView: TextView, dificultad: String) {
        val color = when (dificultad.lowercase().trim()) {
            "baja" -> Color.parseColor("#4CAF50"); "media" -> Color.parseColor("#FFEB3B")
            "alta" -> Color.parseColor("#FF9800"); "muy alta" -> Color.parseColor("#F44336")
            "extrema" -> Color.parseColor("#4A148C"); else -> Color.GRAY
        }
        textView.setTextColor(color)
    }

    private fun actualizarBannerActividad() {
        val idRol = requireContext().getSharedPreferences("preferences", Context.MODE_PRIVATE).getInt("id_rol", 2)
        activity?.findViewById<TextView>(R.id.tvModoAdmin)?.visibility = if (idRol == 1) View.VISIBLE else View.GONE
    }

    private fun comprobarFecha(fechaRuta: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateRuta = sdf.parse(fechaRuta)
            val hoy = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.time
            dateRuta != null && !dateRuta.before(hoy)
        } catch (e: Exception) { true }
    }
}