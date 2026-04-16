package com.example.proyectofinciclo.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.proyectofinciclo.R
import com.example.proyectofinciclo.databinding.FragmentCrearRutaBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import org.json.JSONObject
import java.util.*

class CrearRutaFragment : Fragment(R.layout.fragment_crear_ruta), OnMapReadyCallback {

    private lateinit var binding: FragmentCrearRutaBinding
    private lateinit var googleMap: GoogleMap
    private val GOOGLE_API_KEY = "AIzaSyDJjlhS1n4TCkAKvdr4DjGM6ofGpI3lLT0"
    private val COLOR_ASFALTO = Color.parseColor("#FF9800")
    private val COLOR_TIERRA = Color.parseColor("#8B4513")
    private val TAMANO_ICONO_CIRCULAR = 80
    private val TAMANO_FLECHA = 40
    private val puntosRuta = mutableListOf<LatLng>()
    private val tramosPolyline = mutableListOf<Polyline>()
    private val puntosTrazadoCompleto = mutableListOf<LatLng>()
    private val distanciasTramos = mutableListOf<Double>()
    private val desnivelesTramos = mutableListOf<Double>()
    private val marcadoresKm = mutableListOf<Marker>()
    private val marcadoresFlechas = mutableListOf<Marker>()
    private var marcadorInicio: Marker? = null
    private var marcadorFin: Marker? = null
    private var distanciaTotalAcumulada = 0.0
    private var desnivelTotalAcumulado = 0.0
    private val opcionesBici = mapOf("Carretera" to 1, "MTB" to 2, "Gravel" to 3, "E-Bike" to 4)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCrearRutaBinding.bind(view)
        actualizarBannerActividad()
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as CustomSupportMapFragment
        mapFragment.getMapAsync(this)
        mapFragment.setListener(object : CustomSupportMapFragment.OnTouchListener {
            override fun onTouch() { binding.root.requestDisallowInterceptTouchEvent(true) }
        })
        binding.tvVolverCrear.setOnClickListener { findNavController().popBackStack() }
        binding.btnDeshacer.setOnClickListener { deshacerUltimoPunto() }
        binding.btnPublicarRuta.setOnClickListener { validarYPublicar() }
        binding.btnCambiarMapa.setOnClickListener { cambiarTipoMapa() }
        configurarDesplegables()
        configurarPickers()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        val gijon = LatLng(43.5322, -5.6611)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gijon, 12f))
        googleMap.setOnMapClickListener { latLng ->
            puntosRuta.add(latLng)
            actualizarMarcadoresExtremos()
            if (puntosRuta.size > 1) {
                obtenerTrazadoYElevacion(puntosRuta[puntosRuta.size - 2], latLng)
            }
        }
    }
    private fun obtenerTrazadoYElevacion(origen: LatLng, destino: LatLng) {
        val url = "https://maps.googleapis.com/maps/api/directions/json?origin=${origen.latitude},${origen.longitude}&destination=${destino.latitude},${destino.longitude}&mode=driving&avoid=highways|tolls&key=$GOOGLE_API_KEY"
        Volley.newRequestQueue(requireContext()).add(StringRequest(Request.Method.GET, url, { respuesta ->
            val json = JSONObject(respuesta)
            if (json.getString("status") == "OK") {
                val route = json.getJSONArray("routes").getJSONObject(0)
                val leg = route.getJSONArray("legs").getJSONObject(0)
                val km = leg.getJSONObject("distance").getDouble("value") / 1000.0
                val polylineTramo = route.getJSONObject("overview_polyline").getString("points")
                val puntosDecodificados = PolyUtil.decode(polylineTramo)
                val color = if (binding.autoCompleteTipo.text.toString() in listOf("MTB", "Gravel")) COLOR_TIERRA else COLOR_ASFALTO
                distanciasTramos.add(km); distanciaTotalAcumulada += km
                puntosTrazadoCompleto.addAll(puntosDecodificados)
                tramosPolyline.add(googleMap.addPolyline(PolylineOptions().addAll(puntosDecodificados).color(color).width(14f).jointType(JointType.ROUND)))

                recalcularHitosKilometricos()
                dibujarFlechasDireccion()
                actualizarMarcadoresExtremos()
                obtenerDesnivelAlgoritmoFiltro(puntosDecodificados)
                binding.etCrearDistancia.setText(String.format("%.2f", distanciaTotalAcumulada))
            }
        }, {}))
    }

    private fun obtenerDesnivelAlgoritmoFiltro(puntos: List<LatLng>) {
        if (puntos.size < 2) return
        val url = "https://maps.googleapis.com/maps/api/elevation/json?locations=${puntos.joinToString("|") { "${it.latitude},${it.longitude}" }}&key=$GOOGLE_API_KEY"
        Volley.newRequestQueue(requireContext()).add(StringRequest(Request.Method.GET, url, { respuesta ->
            val json = JSONObject(respuesta)
            if (json.getString("status") == "OK") {
                val results = json.getJSONArray("results")
                val raw = List(results.length()) { results.getJSONObject(it).getDouble("elevation") }
                val smooth = mutableListOf<Double>()
                for (i in raw.indices) {
                    val window = raw.subList((i-2).coerceAtLeast(0), (i+2).coerceAtMost(raw.size-1) + 1)
                    smooth.add(window.average())
                }
                var gain = 0.0; var last = smooth[0]
                for (i in 1 until smooth.size) {
                    val diff = smooth[i] - last
                    if (Math.abs(diff) >= 2.5) { if (diff > 0) gain += diff; last = smooth[i] }
                }
                desnivelesTramos.add(gain); desnivelTotalAcumulado += gain
                binding.etCrearDesnivel.setText(desnivelTotalAcumulado.toInt().toString())
            }
        }, {}))
    }

    private fun deshacerUltimoPunto() {
        if (puntosRuta.isNotEmpty()) {
            puntosRuta.removeAt(puntosRuta.size - 1)
            if (puntosRuta.isEmpty()) {
                marcadorInicio?.remove(); marcadorFin?.remove(); distanciaTotalAcumulada = 0.0; desnivelTotalAcumulado = 0.0
                puntosTrazadoCompleto.clear()
                marcadoresKm.forEach { it.remove() }; marcadoresKm.clear()
                marcadoresFlechas.forEach { it.remove() }; marcadoresFlechas.clear()
            } else if (tramosPolyline.isNotEmpty()) {
                tramosPolyline.removeAt(tramosPolyline.size - 1).remove()
                distanciasTramos.removeAt(distanciasTramos.size - 1)
                if (desnivelesTramos.isNotEmpty()) desnivelTotalAcumulado -= desnivelesTramos.removeAt(desnivelesTramos.size - 1)
                puntosTrazadoCompleto.clear()
                tramosPolyline.forEach { puntosTrazadoCompleto.addAll(it.points) }

                recalcularHitosKilometricos()
                dibujarFlechasDireccion()
            }
            actualizarMarcadoresExtremos()
            binding.etCrearDistancia.setText(String.format("%.2f", if(distanciaTotalAcumulada < 0) 0.0 else distanciaTotalAcumulada))
            binding.etCrearDesnivel.setText(if(desnivelTotalAcumulado < 0) "0" else desnivelTotalAcumulado.toInt().toString())
        }
    }

    private fun validarYPublicar() {
        val titulo = binding.etCrearTitulo.text.toString().trim()
        val localidad = binding.etCrearLocalidad.text.toString().trim()
        val fecha = binding.etCrearFecha.text.toString().trim()
        val hora = binding.etCrearHora.text.toString().trim()


        if (titulo.isEmpty() || localidad.isEmpty() || puntosRuta.isEmpty() || fecha.isEmpty() || hora.isEmpty()) {
            Toast.makeText(requireContext(), "Faltan datos", Toast.LENGTH_SHORT).show(); return
        }

        // Generamos una polyline del recorrido
        val puntosSimplificados = PolyUtil.simplify(puntosTrazadoCompleto, 5.0)
        val polylineFinal = PolyUtil.encode(puntosSimplificados)

        val url = "http://192.168.56.1/cycling_together_api/guardar_rutas.php"
        val prefs = requireContext().getSharedPreferences("preferences", Context.MODE_PRIVATE)

        val request = object : StringRequest(Method.POST, url, { respuesta ->
            if (respuesta.contains("success")) {
                Toast.makeText(requireContext(), "Ruta publicada", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.homeFragment)
            }
        }, { Toast.makeText(requireContext(), "Error de red", Toast.LENGTH_SHORT).show() }
        ) {
            override fun getParams(): Map<String, String> {
                val p = HashMap<String, String>()
                p["titulo"] = titulo; p["localidad"] = localidad
                p["distancia"] = distanciaTotalAcumulada.toString()
                p["desnivel"] = desnivelTotalAcumulado.toInt().toString()
                p["dificultad"] = binding.autoCompleteDificultad.text.toString()
                p["id_bici"] = (opcionesBici[binding.autoCompleteTipo.text.toString()] ?: 1).toString()
                p["fecha"] = binding.etCrearFecha.text.toString(); p["hora"] = binding.etCrearHora.text.toString()
                p["punto_encuentro"] = binding.etCrearPunto.text.toString()
                p["descripcion"] = binding.etCrearDescripcion.text.toString()
                p["id_creador"] = prefs.getInt("id_usuario", 0).toString()
                p["latitud"] = String.format(Locale.US, "%.8f", puntosRuta.first().latitude)
                p["longitud"] = String.format(Locale.US, "%.8f", puntosRuta.first().longitude)
                p["mapa_trazado"] = polylineFinal
                return p
            }
        }
        Volley.newRequestQueue(requireContext()).add(request)
    }

    // Actualización de los marcadores de inicio y fin
    private fun actualizarMarcadoresExtremos() {
        marcadorInicio?.remove(); marcadorFin?.remove()
        if (puntosRuta.isEmpty()) return
        marcadorInicio = googleMap.addMarker(MarkerOptions().position(puntosRuta.first())
            .icon(getCircularBitmapDescriptor(requireContext(), R.drawable.bandera_salida, TAMANO_ICONO_CIRCULAR))
            .anchor(0.5f, 0.5f).zIndex(15f))
        if (puntosRuta.size > 1) {
            marcadorFin = googleMap.addMarker(MarkerOptions().position(puntosRuta.last())
                .icon(getCircularBitmapDescriptor(requireContext(), R.drawable.bandera_llegada, TAMANO_ICONO_CIRCULAR))
                .anchor(0.5f, 0.5f).zIndex(15f))
        }
    }

    private fun dibujarFlechasDireccion() {
        marcadoresFlechas.forEach { it.remove() }; marcadoresFlechas.clear()
        val puntosCompletos = mutableListOf<LatLng>()
        tramosPolyline.forEach { puntosCompletos.addAll(it.points) }
        if (puntosCompletos.size < 10) return
        var distAcum = 0.0; var sigFlecha = 3.0
        for (i in 0 until puntosCompletos.size - 1) {
            val p1 = puntosCompletos[i]; val p2 = puntosCompletos[i+1]
            val res = FloatArray(1)
            android.location.Location.distanceBetween(p1.latitude, p1.longitude, p2.latitude, p2.longitude, res)
            distAcum += (res[0] / 1000.0)
            if (distAcum >= sigFlecha) {
                val rotacion = calcularBearing(p1, p2)
                val m = googleMap.addMarker(MarkerOptions().position(p2)
                    .icon(getBitmapDescriptorResized(requireContext(), R.drawable.ic_flecha_direccion, TAMANO_FLECHA, TAMANO_FLECHA))
                    .rotation(rotacion).flat(true).anchor(0.5f, 0.5f).zIndex(5f))
                m?.let { marcadoresFlechas.add(it) }
                sigFlecha += 3.0
            }
        }
    }

    private fun calcularBearing(inicio: LatLng, fin: LatLng): Float {
        val lat1 = Math.toRadians(inicio.latitude); val lon1 = Math.toRadians(inicio.longitude)
        val lat2 = Math.toRadians(fin.latitude); val lon2 = Math.toRadians(fin.longitude)
        val dLon = lon2 - lon1
        val y = Math.sin(dLon) * Math.cos(lat2)
        val x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon)
        return ((Math.toDegrees(Math.atan2(y, x)) + 360) % 360).toFloat()
    }

    private fun recalcularHitosKilometricos() {
        marcadoresKm.forEach { it.remove() }; marcadoresKm.clear()
        val puntosCompletos = mutableListOf<LatLng>()
        tramosPolyline.forEach { puntosCompletos.addAll(it.points) }
        if (puntosCompletos.size < 2) return
        var acumuladoKm = 0.0; var hitoObjetivo = 5.0
        for (i in 0 until puntosCompletos.size - 1) {
            val res = FloatArray(1)
            android.location.Location.distanceBetween(puntosCompletos[i].latitude, puntosCompletos[i].longitude, puntosCompletos[i+1].latitude, puntosCompletos[i+1].longitude, res)
            acumuladoKm += (res[0] / 1000.0)
            if (acumuladoKm >= hitoObjetivo) {
                val bitmap = createHitoKilometricoBitmap("KM ${hitoObjetivo.toInt()}")
                marcadoresKm.add(googleMap.addMarker(MarkerOptions().position(puntosCompletos[i+1]).icon(BitmapDescriptorFactory.fromBitmap(bitmap)).anchor(0.5f, 0.5f).zIndex(10f))!!)
                hitoObjetivo += 5.0
            }
        }
    }

    private fun createHitoKilometricoBitmap(text: String): Bitmap {
        val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 30f; color = Color.WHITE; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
        val paintStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 30f; color = Color.BLACK; style = Paint.Style.STROKE; strokeWidth = 4f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
        val paintCircle = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; style = Paint.Style.FILL }
        val paintCircleStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK; style = Paint.Style.STROKE; strokeWidth = 3f }
        val textWidth = paintText.measureText(text)
        val width = textWidth.toInt() + 20; val height = 70
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val centerX = width / 2f; val centerY = height - 10f
        canvas.drawCircle(centerX, centerY, 6f, paintCircle)
        canvas.drawCircle(centerX, centerY, 6f, paintCircleStroke)
        canvas.drawText(text, (width - textWidth) / 2f, centerY - 15f, paintStroke)
        canvas.drawText(text, (width - textWidth) / 2f, centerY - 15f, paintText)
        return bitmap
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

    private fun getBitmapDescriptorResized(context: Context, resId: Int, w: Int, h: Int): BitmapDescriptor? {
        val d = ContextCompat.getDrawable(context, resId) ?: return null
        val b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(b); d.setBounds(0, 0, w, h); d.draw(c)
        return BitmapDescriptorFactory.fromBitmap(b)
    }

    private fun cambiarTipoMapa() {
        when (googleMap.mapType) {
            GoogleMap.MAP_TYPE_NORMAL -> googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            GoogleMap.MAP_TYPE_HYBRID -> googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
            GoogleMap.MAP_TYPE_TERRAIN -> googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            else -> googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        }
    }

    private fun actualizarBannerActividad() {
        val idRol = requireContext().getSharedPreferences("preferences", Context.MODE_PRIVATE).getInt("id_rol", 2)
        activity?.findViewById<TextView>(R.id.tvModoAdmin)?.visibility = if (idRol == 1) View.VISIBLE else View.GONE
    }

    private fun configurarDesplegables() {
        binding.autoCompleteDificultad.setAdapter(ArrayAdapter(requireContext(), R.layout.list_item, listOf("baja", "media", "alta", "muy alta", "extrema")))
        binding.autoCompleteTipo.setAdapter(ArrayAdapter(requireContext(), R.layout.list_item, opcionesBici.keys.toList()))
    }

    private fun configurarPickers() {
        binding.etCrearFecha.setOnClickListener { val c = Calendar.getInstance(); DatePickerDialog(requireContext(), { _, y, m, d -> binding.etCrearFecha.setText(String.format("%d-%02d-%02d", y, m + 1, d)) }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show() }
        binding.etCrearHora.setOnClickListener { val c = Calendar.getInstance(); TimePickerDialog(requireContext(), { _, h, min -> binding.etCrearHora.setText(String.format("%02d:%02d:00", h, min)) }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show() }
    }
}