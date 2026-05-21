package com.example.proyectofinciclo.ui

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
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
import java.util.Locale

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: RutaAdapter

    private var listaOriginalRutas = mutableListOf<Ruta>()
    private var idRolActual: Int = 2
    private var idUsuarioLogueadoActual: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        binding.rvRutasTablon.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRutasTablon.setHasFixedSize(true)

        val sharedPrefs = requireContext().getSharedPreferences("preferences", Context.MODE_PRIVATE)
        idRolActual = sharedPrefs.getInt("id_rol", 2)
        idUsuarioLogueadoActual = sharedPrefs.getInt("id_usuario", 0)

        activity?.findViewById<TextView>(R.id.tvModoAdmin)?.visibility =
            if (idRolActual == 1) View.VISIBLE else View.GONE

        adapter = RutaAdapter(listaOriginalRutas, idRolActual, idUsuarioLogueadoActual, { ruta ->
            irADetalle(ruta)
        }, { ruta ->
            mostrarDialogoBorrar(ruta)
        })

        binding.rvRutasTablon.adapter = adapter

        // Escuchador para el filtro de búsqueda por Localidad
        binding.etFiltroLocalidad.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filtrarRutas(s.toString())
            }
        })

        obtenerRutasDeServidor()
    }

    override fun onResume() {
        super.onResume()
        // Refresca la lista automáticamente al volver a esta pestaña
        obtenerRutasDeServidor()
    }

    private fun obtenerRutasDeServidor() {
        val url = "http://192.168.56.1/cycling_together_api/listar_rutas.php"

        val request = StringRequest(Request.Method.GET, url, { response ->
            try {
                val jsonArray = JSONArray(response)
                listaOriginalRutas.clear()

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val listaIds = obj.optString("lista_ids_participantes", "")
                    val usuarioUnido = listaIds.split(",").contains(idUsuarioLogueadoActual.toString())

                    listaOriginalRutas.add(
                        Ruta(
                            id_ruta = obj.optInt("id_ruta"),
                            id_creador = obj.optInt("id_creador"),
                            titulo = obj.optString("titulo"),
                            localidad = obj.optString("localidad"),
                            distancia = obj.optDouble("distancia"),
                            desnivel = obj.optInt("desnivel"),
                            dificultad = obj.optString("dificultad"),
                            id_bici = obj.optInt("id_bici"),
                            fecha = obj.optString("fecha"),
                            hora = obj.optString("hora"),
                            puntoEncuentro = obj.optString("punto_encuentro"),
                            descripcion = obj.optString("descripcion"),
                            mapa_trazado = obj.optString("mapa_trazado"),
                            nombre_creador = obj.optString("nombre_creador"),
                            latitud = obj.optDouble("latitud", 0.0),
                            longitud = obj.optDouble("longitud", 0.0),
                            num_participantes = obj.optInt("num_participantes", 0),
                            nombres_participantes = obj.optString("nombres_participantes", ""),
                            estaUnido = usuarioUnido
                        )
                    )
                }
                adapter.updateRutas(listaOriginalRutas)
            } catch (e: Exception) {
                listaOriginalRutas.clear()
                adapter.updateRutas(listaOriginalRutas)
            }
        }, {
            Toast.makeText(requireContext(), "Error de red", Toast.LENGTH_SHORT).show()
        })

        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun irADetalle(ruta: Ruta) {
        val bundle = Bundle().apply {
            putInt("id_ruta", ruta.id_ruta ?: 0)
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
            putString("creador", ruta.nombre_creador)
            putDouble("latitud", ruta.latitud)
            putDouble("longitud", ruta.longitud)
            putString("mapa", ruta.mapa_trazado)
            putInt("num_participantes", ruta.num_participantes)
            putString("nombres_participantes", ruta.nombres_participantes)
            putBoolean("estaUnido", ruta.estaUnido)
        }
        findNavController().navigate(R.id.detalleRutaFragment, bundle)
    }

    private fun mostrarDialogoBorrar(ruta: Ruta) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar ruta")
            .setMessage("¿Estás seguro de que deseas eliminar esta ruta?")
            .setPositiveButton("Sí") { _, _ -> ejecutarBorrado(ruta.id_ruta!!) }
            .setNegativeButton("No", null)
            .show()
    }

    private fun ejecutarBorrado(id: Int) {
        val url = "http://192.168.56.1/cycling_together_api/borrar_rutas.php"

        val request = object : StringRequest(Method.POST, url, { response ->
            try {
                val jsonObject = JSONObject(response)
                if (jsonObject.getString("status") == "success") {
                    Toast.makeText(requireContext(), "Ruta eliminada correctamente", Toast.LENGTH_SHORT).show()
                    obtenerRutasDeServidor()
                } else {
                    Toast.makeText(requireContext(), "Error al eliminar la ruta", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error en el servidor", Toast.LENGTH_SHORT).show()
            }
        }, {
            Toast.makeText(requireContext(), "Error de red", Toast.LENGTH_SHORT).show()
        }) {
            override fun getParams(): Map<String, String> {
                return mapOf("id_ruta" to id.toString())
            }
        }
        Volley.newRequestQueue(requireContext()).add(request)
    }

    // Función para filtrar las rutas
    private fun filtrarRutas(texto: String) {
        val textoBusqueda = texto.lowercase(Locale.getDefault())
        val listaFiltrada = listaOriginalRutas.filter { ruta ->
            ruta.localidad.lowercase(Locale.getDefault()).contains(textoBusqueda)
        }
        adapter.updateRutas(listaFiltrada)
    }

}