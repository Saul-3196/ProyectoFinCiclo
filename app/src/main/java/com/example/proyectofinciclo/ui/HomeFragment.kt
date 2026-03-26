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

        actualizarInterfazSegunRol()
        setupBuscador()
        obtenerRutasDeServidor()
    }

    private fun actualizarInterfazSegunRol() {
        val tvBanner = activity?.findViewById<TextView>(R.id.tvModoAdmin)
        tvBanner?.visibility = if (idRolActual == 1) View.VISIBLE else View.GONE
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
        val listaFiltrada = if (texto.isEmpty()) listaOriginalRutas else listaOriginalRutas.filter { it.localidad.lowercase().contains(texto) }
        if (::adapter.isInitialized) adapter.updateRutas(listaFiltrada)
    }

    private fun obtenerRutasDeServidor() {
        val url = "http://192.168.56.1/cycling_together_api/listar_rutas.php"
        Volley.newRequestQueue(requireContext()).add(StringRequest(Request.Method.GET, url, { respuesta ->
            try {
                val jsonArray = JSONArray(respuesta)
                listaOriginalRutas.clear()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    listaOriginalRutas.add(Ruta(
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
                        mapa_trazado = obj.optString("mapa_trazado", ""),
                        latitud = obj.optDouble("latitud", 0.0),
                        longitud = obj.optDouble("longitud", 0.0)
                    ))
                }
                adapter = RutaAdapter(listaOriginalRutas, idRolActual, idUsuarioLogueadoActual, { irADetalle(it) }, { mostrarDialogoBorrar(it) })
                binding.rvRutasTablon.adapter = adapter
            } catch (e: Exception) { e.printStackTrace() }
        }, { Toast.makeText(requireContext(), "Error de red", Toast.LENGTH_SHORT).show() }))
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
            putString("creador", ruta.nombre_creador)
            putDouble("latitud", ruta.latitud)
            putDouble("longitud", ruta.longitud)
            putString("mapa", ruta.mapa_trazado) // CLAVE CORRECTA
        }
        findNavController().navigate(R.id.detalleRutaFragment, bundle)
    }

    private fun mostrarDialogoBorrar(ruta: Ruta) {
        AlertDialog.Builder(requireContext()).setTitle("Eliminar").setMessage("¿Borrar ruta?")
            .setPositiveButton("Sí") { _, _ -> ejecutarBorrado(ruta.id_ruta!!) }.setNegativeButton("No", null).show()
    }

    private fun ejecutarBorrado(id: Int) {
        val url = "http://192.168.56.1/cycling_together_api/borrar_rutas.php"
        Volley.newRequestQueue(requireContext()).add(object : StringRequest(Method.POST, url, { obtenerRutasDeServidor() }, {}) {
            override fun getParams() = mapOf("id_ruta" to id.toString())
        })
    }
}