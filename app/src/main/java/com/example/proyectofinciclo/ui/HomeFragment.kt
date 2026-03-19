package com.example.proyectofinciclo.ui

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import org.json.JSONObject

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: RutaAdapter

    private var listaOriginalRutas = mutableListOf<Ruta>()
    private var idRolActual: Int = 2
    private var idUsuarioLogueadoActual: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)


        // Configuración del RecyclerView
        binding.rvRutasTablon.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRutasTablon.setHasFixedSize(true)

        // Recupero el ID del usuario logueado y su rol
        val sharedPrefs = requireContext().getSharedPreferences("preferences", Context.MODE_PRIVATE)
        idRolActual = sharedPrefs.getInt("id_rol", 2) // Por defecto usuario normal (2)
        idUsuarioLogueadoActual = sharedPrefs.getInt("id_usuario", 0)

        // Visibilidad del banner para el rol de ciclista
        actualizarInterfazSegunRol()

        // Configuración de obtener rutas.
        setupBuscador()
        obtenerRutasDeServidor()
    }

    private fun actualizarInterfazSegunRol() {
        // Buscamos el Text View del Banner en alguna de nuestras Activities
        val tvBanner = activity?.findViewById<android.widget.TextView>(R.id.tvModoAdmin)

        if (idRolActual == 1) {
            tvBanner?.visibility = View.VISIBLE
        } else {
            tvBanner?.visibility = View.GONE
        }
    }

    private fun setupBuscador() {
        binding.etFiltroLocalidad.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarRutas(s.toString().lowercase().trim())
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
        if (::adapter.isInitialized) {
            adapter.updateRutas(listaFiltrada)
        }
    }

    private fun obtenerRutasDeServidor() {
        val url = "http://192.168.56.1/cycling_together_api/listar_rutas.php"

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
                            mapa_trazado = obj.optString("mapa_trazado", null),
                            latitud = obj.optDouble("latitud", 0.0),
                            longitud = obj.optDouble("longitud", 0.0)
                        )
                        listaOriginalRutas.add(ruta)
                    }

                    // Creamos el adapter pasando los 5 parámetros requeridos
                    adapter = RutaAdapter(
                        listaOriginalRutas,
                        idRolActual,
                        idUsuarioLogueadoActual,
                        { ruta -> irADetalle(ruta) },
                        { ruta -> mostrarDialogoBorrar(ruta) }
                    )
                    binding.rvRutasTablon.adapter = adapter

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            { error ->
                Toast.makeText(requireContext(), "Error al conectar con el servidor", Toast.LENGTH_SHORT).show()
            }
        )
        Volley.newRequestQueue(requireContext()).add(peticion)
    }

    private fun mostrarDialogoBorrar(ruta: Ruta) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Ruta")
            .setMessage("¿Estás seguro de que quieres borrar '${ruta.titulo}' permanentemente?")
            .setPositiveButton("Eliminar") { _, _ ->
                // Usamos !! porque id_ruta en el modelo puede ser opcional
                ejecutarBorradoEnServidor(ruta.id_ruta!!)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun ejecutarBorradoEnServidor(idRuta: Int) {
        val url = "http://192.168.56.1/cycling_together_api/borrar_rutas.php"

        val request = object : StringRequest(Method.POST, url,
            { respuesta ->
                try {
                    val json = JSONObject(respuesta)
                    if (json.getString("status") == "success") {
                        Toast.makeText(context, "Ruta eliminada correctamente", Toast.LENGTH_SHORT).show()
                        obtenerRutasDeServidor() // Recarga la lista automáticamente
                    } else {
                        Toast.makeText(context, "Error: ${json.getString("message")}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) { e.printStackTrace() }
            },
            { Toast.makeText(context, "Error de conexión al borrar", Toast.LENGTH_SHORT).show() }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["id_ruta"] = idRuta.toString()
                return params
            }
        }
        Volley.newRequestQueue(requireContext()).add(request)
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
            putDouble("longitud", ruta.longitud)
            putDouble("latitud", ruta.latitud)
            putString("mapa", ruta.mapa_trazado)
        }
        findNavController().navigate(R.id.detalleRutaFragment, bundle)
    }
}