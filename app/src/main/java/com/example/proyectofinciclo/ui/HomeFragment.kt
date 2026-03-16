package com.example.proyectofinciclo.ui

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.proyectofinciclo.R
import com.example.proyectofinciclo.adapter.RutaAdapter
import com.example.proyectofinciclo.databinding.FragmentHomeBinding
import com.example.proyectofinciclo.model.Ruta
import org.json.JSONArray

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: RutaAdapter

    // Guardamos la lista completa para poder filtrar sin perder datos
    private var listaOriginalRutas = mutableListOf<Ruta>()
    private var idRolActual: Int = 2
    private var idUsuarioLogueadoActual: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        // Configuración inicial del RecyclerView
        binding.rvRutasTablon.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRutasTablon.setHasFixedSize(true)

        // Configuramos el buscador
        setupBuscador()

        obtenerRutasDeServidor()
    }

    private fun setupBuscador() {
        binding.etFiltroLocalidad.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val texto = s.toString().lowercase().trim()
                filtrarRutas(texto)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filtrarRutas(texto: String) {
        val listaFiltrada = if (texto.isEmpty()) {
            listaOriginalRutas
        } else {
            listaOriginalRutas.filter { it.localidad.lowercase().contains(texto) }
        }

        // El adaptador debe tener la función updateRutas para refrescar la vista
        if (::adapter.isInitialized) {
            adapter.updateRutas(listaFiltrada)
        }
    }

    private fun obtenerRutasDeServidor() {
        val url = "http://192.168.56.1/cycling_together_api/listar_rutas.php"

        val sharedPrefs = requireContext().getSharedPreferences("preferences", Context.MODE_PRIVATE)
        idRolActual = sharedPrefs.getInt("id_rol", 2)
        idUsuarioLogueadoActual = sharedPrefs.getInt("id_usuario", 0)

        val peticion = StringRequest(Request.Method.GET, url,
            { respuesta ->
                try {
                    val jsonArray = JSONArray(respuesta)
                    listaOriginalRutas.clear()

                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)

                        val ruta = Ruta(
                            id_ruta = obj.getInt("id_ruta"),
                            titulo = obj.getString("titulo"),
                            distancia = obj.getDouble("distancia"),
                            desnivel = obj.getInt("desnivel"),
                            dificultad = obj.getString("dificultad"),
                            localidad = obj.getString("localidad"),
                            id_bici = obj.getInt("id_bici"),
                            fecha = obj.getString("fecha"),
                            hora = obj.getString("hora"),
                            puntoEncuentro = obj.getString("punto_encuentro"),
                            descripcion = obj.getString("descripcion"),
                            id_creador = obj.getInt("id_creador"),
                            nombre_creador = obj.optString("nombre_creador", "Usuario"),
                            mapa_trazado = obj.optString("mapa_trazado", null)
                        )
                        listaOriginalRutas.add(ruta)
                    }

                    // Inicializamos el adaptador con la lista completa
                    adapter = RutaAdapter(listaOriginalRutas, idRolActual, idUsuarioLogueadoActual) { ruta ->
                        irADetalle(ruta)
                    }
                    binding.rvRutasTablon.adapter = adapter

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Error al cargar las rutas", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                Toast.makeText(requireContext(), "Error de red: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )

        Volley.newRequestQueue(requireContext()).add(peticion)
    }
    private fun irADetalle(ruta: Ruta) {
        val bundle = Bundle().apply {
            putString("titulo", ruta.titulo)
            putDouble("distancia", ruta.distancia)
            putString("dificultad", ruta.dificultad)
            putInt("desnivel", ruta.desnivel)
            putString("localidad", ruta.localidad)
            putString("fecha", ruta.fecha)
            putString("hora", ruta.hora)
            putString("puntoEncuentro", ruta.puntoEncuentro)
            putString("descripción", ruta.descripcion)
            putInt("id_bici", ruta.id_bici)
            putInt("id_creador", ruta.id_creador)
            putString("mapa", ruta.mapa_trazado)
            putString("creador", ruta.nombre_creador)
            putBoolean("esMia", ruta.id_creador == idUsuarioLogueadoActual)
        }
        findNavController().navigate(R.id.detalleRutaFragment, bundle)
    }
}