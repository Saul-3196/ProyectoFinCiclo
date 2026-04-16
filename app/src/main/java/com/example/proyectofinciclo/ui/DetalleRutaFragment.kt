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
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
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

    // Variables de estado para el botón
    private var idRutaActual: Int = 0
    private var idUsuarioActual: Int = 0
    private var usuarioEstaUnido: Boolean = false

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

        // Preferencias del usuario logueado
        val sharedPrefs = requireContext().getSharedPreferences("preferences", Context.MODE_PRIVATE)
        idUsuarioActual = sharedPrefs.getInt("id_usuario", 0)

        // Recuperación de datos del Bundle
        idRutaActual = arguments?.getInt("id_ruta") ?: 0
        usuarioEstaUnido = arguments?.getBoolean("estaUnido") ?: false
        val numParticipantes = arguments?.getInt("num_participantes") ?: 0
        val nombresParticipantes = arguments?.getString("nombres_participantes") ?: ""
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

        binding.apply {
            tvDetalleTitulo.text = titulo
            tvDetalleKm.text = "📏 $distancia km"
            tvDetalleDesnivel.text = "⛰️ $desnivelRecibido m"
            tvDetalleDificultad.text = "📊 $dificultad"
            asignarColorDificultad(tvDetalleDificultad, dificultad)
            tvDetalleLocalidad.text = "📍 $localidad"
            tvDetalleFecha.text = "📅 $fechaString"
            tvDetalleHora.text = "⏰ $hora"
            tvDetallePunto.text = "🫂 $puntoEncuentro"
            tvDetalleDescripcion.text = descripcion
            tvDetalleTipo.text = "🚴 $textoBici"
            tvDetalleCreador.text = "Por: $nombreOrganizador"

            if (numParticipantes > 0) {
                tvListaParticipantes.text = "👥 Apuntados ($numParticipantes): $nombresParticipantes"
            } else {
                tvListaParticipantes.text = "👥 Todavía no hay nadie apuntado"
            }
        }

        // Lógica del botón de unirse o abandonar
        configurarBotonUnirse(esVigente, idCreadorRuta)
    }

    private fun configurarBotonUnirse(esVigente: Boolean, idCreadorRuta: Int) {
        val btn = binding.btnUnirseRuta

        if (!esVigente) {
            btn.isEnabled = false
            btn.text = "Ruta finalizada"
            btn.setBackgroundColor(Color.GRAY)
        } else if (idCreadorRuta == idUsuarioActual) {
            btn.isEnabled = false
            btn.text = "Eres el organizador"
            btn.setBackgroundColor(Color.parseColor("#2196F3"))
        } else {
            // El usuario puede interactuar
            btn.isEnabled = true
            actualizarAspectoBoton()

            btn.setOnClickListener {
                btn.isEnabled = false
                if (usuarioEstaUnido) {
                    peticionAbandonarRuta()
                } else {
                    peticionUnirseRuta()
                }
            }
        }
    }

    private fun actualizarAspectoBoton() {
        if (usuarioEstaUnido) {
            binding.btnUnirseRuta.text = "Abandonar ruta"
            binding.btnUnirseRuta.setBackgroundColor(Color.parseColor("#F44336")) // Rojo
        } else {
            binding.btnUnirseRuta.text = "Unirse a esta ruta"
            binding.btnUnirseRuta.setBackgroundColor(Color.parseColor("#4CAF50")) // Verde
        }
    }

    private fun peticionUnirseRuta() {
        val url = "http://192.168.56.1/cycling_together_api/unirse_ruta.php"
        val request = object : StringRequest(Method.POST, url, { response ->
            binding.btnUnirseRuta.isEnabled = true
            if (response.trim() == "success") {
                usuarioEstaUnido = true
                actualizarAspectoBoton()
                Toast.makeText(requireContext(), "¡Te has unido a la ruta!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Error al unirse", Toast.LENGTH_SHORT).show()
            }
        }, {
            binding.btnUnirseRuta.isEnabled = true
            Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show()
        }) {
            override fun getParams(): Map<String, String> {
                return mapOf(
                    "id_ruta" to idRutaActual.toString(),
                    "id_usuario" to idUsuarioActual.toString()
                )
            }
        }
        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun peticionAbandonarRuta() {
        val url = "http://192.168.56.1/cycling_together_api/abandonar_ruta.php"
        val request = object : StringRequest(Method.POST, url, { response ->
            binding.btnUnirseRuta.isEnabled = true
            if (response.trim() == "success") {
                usuarioEstaUnido = false
                actualizarAspectoBoton()
                Toast.makeText(requireContext(), "Has abandonado la ruta", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Error al abandonar", Toast.LENGTH_SHORT).show()
            }
        }, {
            binding.btnUnirseRuta.isEnabled = true
            Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show()
        }) {
            override fun getParams(): Map<String, String> {
                return mapOf(
                    "id_ruta" to idRutaActual.toString(),
                    "id_usuario" to idUsuarioActual.toString()
                )
            }
        }
        Volley.newRequestQueue(requireContext()).add(request)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.clear()

        val mapaCodificado = arguments?.getString("mapa")?.trim()?.replace("\"", "") ?: ""

        if (mapaCodificado.isNotEmpty()) {
            try {
                val puntos = PolyUtil.decode(mapaCodificado)
                if (puntos.isNotEmpty()) {
                    googleMap?.addPolyline(PolylineOptions()
                        .addAll(puntos)
                        .color(Color.parseColor("#FF9800"))
                        .width(14f)
                        .jointType(JointType.ROUND)
                        .startCap(RoundCap())
                        .endCap(RoundCap()))

                    googleMap?.addMarker(MarkerOptions().position(puntos.first())
                        .icon(getCircularBitmapDescriptor(requireContext(), R.drawable.bandera_salida, TAMANO_ICONO))
                        .anchor(0.5f, 0.5f))

                    googleMap?.addMarker(MarkerOptions().position(puntos.last())
                        .icon(getCircularBitmapDescriptor(requireContext(), R.drawable.bandera_llegada, TAMANO_ICONO))
                        .anchor(0.5f, 0.5f))

                    val builder = LatLngBounds.Builder()
                    puntos.forEach { builder.include(it) }

                    googleMap?.setOnMapLoadedCallback {
                        try {
                            googleMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 150))
                        } catch (e: Exception) {
                            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(puntos.first(), 15f))
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MAPA_DETALLE", "Error decodificando la Polyline: ${e.message}")
                val lat = arguments?.getDouble("latitud") ?: 0.0
                val lon = arguments?.getDouble("longitud") ?: 0.0
                if (lat != 0.0 && lon != 0.0) {
                    googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), 15f))
                }
            }
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
            "baja" -> Color.parseColor("#4CAF50"); "media" -> Color.parseColor("#FFC107")
            "alta" -> Color.parseColor("#F54040"); "muy alta" -> Color.parseColor("#961208")
            "extrema" -> Color.parseColor("#490669"); else -> Color.GRAY
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