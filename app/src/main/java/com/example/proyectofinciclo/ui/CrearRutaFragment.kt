package com.example.proyectofinciclo.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
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
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONObject
import java.util.*

class CrearRutaFragment : Fragment(R.layout.fragment_crear_ruta), OnMapReadyCallback {

    private lateinit var binding: FragmentCrearRutaBinding
    private lateinit var googleMap: GoogleMap

    // Listado de los diferentes tipos de bicicletas con su ID correspondiente
    private val opcionesBici = mapOf(
        "Carretera" to 1,
        "MTB" to 2,
        "Gravel" to 3,
        "E-Bike" to 4
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCrearRutaBinding.bind(view)

        // Lógica del botón volver
        binding.tvVolverCrear.setOnClickListener {
            findNavController().popBackStack()
        }

        // Se inicializa el mapa
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        configurarDesplegables()
        configurarPickers()

        binding.btnPublicarRuta.setOnClickListener {
            validarYPublicar()
        }
    }

    // Este método es obligatorio para implementar OnMapReadyCallback
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        // Localidad por defecto
        val gijon = LatLng(43.5322, -5.6611)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gijon, 12f))

        // Añadimos un marcador de prueba
        googleMap.addMarker(MarkerOptions().position(gijon).title("Punto de inicio"))
    }

    private fun configurarDesplegables() {
        val dificultades = listOf("baja", "media", "alta", "muy alta", "extrema")
        val adapterDificultad = ArrayAdapter(requireContext(), R.layout.list_item, dificultades)
        binding.autoCompleteDificultad.setAdapter(adapterDificultad)

        val tipos = opcionesBici.keys.toList()
        val adapterTipo = ArrayAdapter(requireContext(), R.layout.list_item, tipos)
        binding.autoCompleteTipo.setAdapter(adapterTipo)
    }

    private fun configurarPickers() {
        binding.etCrearFecha.setOnClickListener {
            val calendario = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                binding.etCrearFecha.setText(String.format("%02d/%02d/%d", d, m + 1, y))
            }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.etCrearHora.setOnClickListener {
            val calendario = Calendar.getInstance()
            TimePickerDialog(requireContext(), { _, h, min ->
                binding.etCrearHora.setText(String.format("%02d:%02d", h, min))
            }, calendario.get(Calendar.HOUR_OF_DAY), calendario.get(Calendar.MINUTE), true).show()
        }
    }

    private fun validarYPublicar() {
        val titulo = binding.etCrearTitulo.text.toString().trim()
        val localidad = binding.etCrearLocalidad.text.toString().trim()
        val distancia = binding.etCrearDistancia.text.toString().trim()
        val desnivel = binding.etCrearDesnivel.text.toString().trim()
        val dificultad = binding.autoCompleteDificultad.text.toString()
        val tipoBicicleta = binding.autoCompleteTipo.text.toString()
        val fecha = binding.etCrearFecha.text.toString()
        val hora = binding.etCrearHora.text.toString()
        val puntoEncuentro = binding.etCrearPunto.text.toString().trim()
        val descripcion = binding.etCrearDescripcion.text.toString().trim()

        // Validaciones de seguridad
        if (titulo.isEmpty()) { binding.etCrearTitulo.error = "Campo obligatorio"; return }
        if (localidad.isEmpty()) { binding.etCrearLocalidad.error = "Campo obligatorio"; return }
        if (distancia.isEmpty()) { binding.etCrearDistancia.error = "Indica los KM"; return }
        if (desnivel.isEmpty()) { binding.etCrearDesnivel.error = "Indica el desnivel"; return }
        if (fecha.isEmpty()) { Toast.makeText(context, "Selecciona una fecha", Toast.LENGTH_SHORT).show(); return }

        val idBiciSeleccionada = opcionesBici[tipoBicicleta] ?: 1
        val prefs = requireContext().getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val idUsuarioLogueado = prefs.getInt("id_usuario", 0)

        // Nos conectamos a nuestro servidor para publicar la ruta
        val url = "http://192.168.56.1/cycling_together_api/guardar_ruta.php"

        val peticion = object : StringRequest(Method.POST, url,
            { respuesta ->
                try {
                    val json = JSONObject(respuesta)
                    if (json.getString("status") == "success") {
                        Toast.makeText(requireContext(), "¡Ruta publicada!", Toast.LENGTH_SHORT).show()
                        volverAlHome()
                    } else {
                        Toast.makeText(requireContext(), "Error: ${json.getString("message")}", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error al procesar respuesta", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(requireContext(), "Error de red: ${error.message}", Toast.LENGTH_LONG).show()
            }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["titulo"] = titulo
                params["localidad"] = localidad
                params["distancia"] = distancia
                params["desnivel"] = desnivel
                params["dificultad"] = dificultad
                params["id_bici"] = idBiciSeleccionada.toString()
                params["fecha"] = fecha
                params["hora"] = hora
                params["punto_encuentro"] = puntoEncuentro
                params["descripcion"] = descripcion
                params["id_creador"] = idUsuarioLogueado.toString()
                return params
            }
        }
        Volley.newRequestQueue(requireContext()).add(peticion)
    }

    private fun volverAlHome() {
        findNavController().navigate(R.id.homeFragment)
    }
}