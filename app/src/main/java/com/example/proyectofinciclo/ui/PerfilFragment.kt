package com.example.proyectofinciclo.ui

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
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
import com.example.proyectofinciclo.databinding.FragmentPerfilBinding
import com.example.proyectofinciclo.model.Ruta
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class PerfilFragment : Fragment(R.layout.fragment_perfil) {

    private lateinit var binding: FragmentPerfilBinding
    private var adapter: RutaAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPerfilBinding.bind(view)

        actualizarBannerActividad()
        configurarDesplegables()
        setupRecyclerView()

        // Se cargan los datos del usuario, si hay cambios, el onResume() los actualizará
        cargarDatosPerfil()
        cargarMisRutas()

        binding.etFechaNacimiento.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                val fechaSeleccionada = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                binding.etFechaNacimiento.setText(fechaSeleccionada)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

            datePickerDialog.show()
        }

        binding.btnGuardarPerfil.setOnClickListener {
            val nombre = binding.etNombrePerfil.text.toString().trim()
            val email = binding.etEmailPerfil.text.toString().trim()
            val sexo = binding.spinnerSexo.text.toString()
            val fechaNacimiento = binding.etFechaNacimiento.text.toString()
            val ciudad = binding.etCiudad.text.toString().trim()
            val nivelUsuario = binding.spinnerNivel.text.toString()
            val tipoBiciTexto = binding.spinnerTipoBici.text.toString()

            val idBici = when (tipoBiciTexto) {
                "Carretera" -> 1
                "MTB", "Montaña" -> 2
                "Gravel" -> 3
                "E-Bike", "Ebike" -> 4
                else -> 0
            }

            val sharedPrefs = requireContext().getSharedPreferences("preferences", Context.MODE_PRIVATE)
            val idUsuario = sharedPrefs.getInt("id_usuario", 0)

            val url = "http://192.168.56.1/cycling_together_api/editar_perfil.php"

            val request = object : StringRequest(Method.POST, url, { response ->
                try {
                    val jsonObject = JSONObject(response)
                    if (jsonObject.getString("status") == "success") {
                        Toast.makeText(requireContext(), "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()

                        val editor = sharedPrefs.edit()
                        editor.putString("nombre_usuario", nombre)
                        editor.apply()

                    } else {
                        Toast.makeText(requireContext(), "Error al guardar: ${jsonObject.getString("message")}", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error procesando respuesta", Toast.LENGTH_SHORT).show()
                }
            }, {
                Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show()
            }) {
                override fun getParams(): Map<String, String> {
                    return mapOf(
                        "id_usuario" to idUsuario.toString(),
                        "nombre" to nombre,
                        "email" to email,
                        "sexo" to sexo,
                        "fecha_nacimiento" to fechaNacimiento,
                        "ciudad" to ciudad,
                        "nivel_usuario" to nivelUsuario,
                        "id_bici" to idBici.toString()
                    )
                }
            }
            Volley.newRequestQueue(requireContext()).add(request)
        }

        binding.btnLogOut.setOnClickListener {
            val sharedPrefs = requireContext().getSharedPreferences("preferences", Context.MODE_PRIVATE)
            val editor = sharedPrefs.edit()
            editor.remove("id_rol")
            editor.remove("id_usuario")
            editor.remove("nombre_usuario")
            editor.apply()

            Toast.makeText(requireContext(), "Hasta Pronto", Toast.LENGTH_SHORT).show()

            val intent = Intent(
                requireContext(),
                com.example.proyectofinciclo.viewmodel.LoginActivity::class.java
            )
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    // Refrescamos la interfaz tras haber ejecutado algún cambio
    override fun onResume() {
        super.onResume()
        // Cada vez que se acceda a la ventana del perfil, se refrescarán los datos
        cargarDatosPerfil()
        cargarMisRutas()
    }

    private fun cargarDatosPerfil() {
        val sharedPrefs = requireContext().getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val idUsuario = sharedPrefs.getInt("id_usuario", 0)

        val url = "http://192.168.56.1/cycling_together_api/obtener_perfil.php"

        val request = object : StringRequest(Method.POST, url, { response ->
            try {
                val userJson = JSONObject(response)

                binding.etNombrePerfil.setText(userJson.optString("nombre"))
                binding.etEmailPerfil.setText(userJson.optString("email"))
                binding.etCiudad.setText(userJson.optString("ciudad"))

                val fecha = userJson.optString("fecha_nacimiento")
                if(fecha != "null" && fecha.isNotEmpty()) {
                    binding.etFechaNacimiento.setText(fecha)
                }

                val sexo = userJson.optString("sexo")
                if(sexo != "null" && sexo.isNotEmpty()) {
                    binding.spinnerSexo.setText(sexo, false)
                }

                val nivel = userJson.optString("nivel_usuario")
                if(nivel != "null") binding.spinnerNivel.setText(nivel, false)

                val idBici = userJson.optInt("id_bici", 0)
                val textoBici = when(idBici) {
                    1 -> "Carretera"
                    2 -> "MTB"
                    3 -> "Gravel"
                    4 -> "E-Bike"
                    else -> ""
                }
                if(textoBici.isNotEmpty()) binding.spinnerTipoBici.setText(textoBici, false)

            } catch (e: Exception) {

            }
        }, {
            Toast.makeText(requireContext(), "Error de red al cargar perfil", Toast.LENGTH_SHORT).show()
        }) {
            override fun getParams(): Map<String, String> {
                return mapOf("id_usuario" to idUsuario.toString())
            }
        }
        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun cargarMisRutas() {
        val sharedPrefs = requireContext().getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val idLogueado = sharedPrefs.getInt("id_usuario", 0)
        val rolLogueado = sharedPrefs.getInt("id_rol", 2)

        val url = "http://192.168.56.1/cycling_together_api/obtener_mis_rutas.php"

        val request = object : StringRequest(Method.POST, url, { response ->
            try {
                val jsonArray = JSONArray(response)
                val listaRutasReales = mutableListOf<Ruta>()

                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)

                    val ruta = Ruta(
                        id_ruta = jsonObject.getInt("id_ruta"),
                        titulo = jsonObject.getString("titulo"),
                        distancia = jsonObject.getDouble("distancia"),
                        desnivel = jsonObject.getInt("desnivel"),
                        dificultad = jsonObject.getString("dificultad"),
                        localidad = jsonObject.getString("localidad"),
                        id_bici = jsonObject.getInt("id_bici"),
                        fecha = jsonObject.getString("fecha"),
                        hora = jsonObject.getString("hora"),
                        puntoEncuentro = jsonObject.getString("punto_encuentro"),
                        descripcion = jsonObject.getString("descripcion"),
                        id_creador = jsonObject.getInt("id_creador"),
                        mapa_trazado = jsonObject.optString("mapa_trazado", null)
                    )
                    listaRutasReales.add(ruta)
                }

                adapter = RutaAdapter(
                    listaRutasReales,
                    rolLogueado,
                    idLogueado,
                    { ruta -> irADetalle(ruta) },
                    { ruta -> mostrarDialogoBorrar(ruta) }
                )
                binding.rvMisRutas.adapter = adapter

            } catch (e: Exception) {
               // Si el usuario no tiene rutas, mostramos un listado vacío
                binding.rvMisRutas.adapter = null
            }
        }, {
            Toast.makeText(requireContext(), "Error al cargar tu historial", Toast.LENGTH_SHORT).show()
        }) {
            override fun getParams(): Map<String, String> {
                return mapOf("id_usuario" to idLogueado.toString())
            }
        }

        Volley.newRequestQueue(requireContext()).add(request)
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

    private fun setupRecyclerView() {
        binding.rvMisRutas.apply {
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun irADetalle(ruta: Ruta) {
        val bundle = Bundle().apply {
            putString("titulo", ruta.titulo)
            putDouble("distancia", ruta.distancia)
            putInt("desnivel", ruta.desnivel)
            putString("dificultad", ruta.dificultad)
            putString("localidad", ruta.localidad)
            putString("fecha", ruta.fecha)
            putString("hora", ruta.hora)
            putString("puntoEncuentro", ruta.puntoEncuentro)
            putString("descripción", ruta.descripcion)
            putInt("tipoCiclismo", ruta.id_bici)
            putInt("creador", ruta.id_creador)
            putString("mapa", ruta.mapa_trazado)
            putBoolean("esVigente", comprobarFecha(ruta.fecha))
        }
        findNavController().navigate(R.id.detalleRutaFragment, bundle)
    }

    private fun mostrarDialogoBorrar(ruta: Ruta) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar ruta")
            .setMessage("¿Estás seguro de que deseas eliminar la ruta '${ruta.titulo}'? Esta acción no se puede deshacer.")
            .setPositiveButton("Sí, eliminar") { _, _ ->
                eliminarRutaDeBaseDatos(ruta.id_ruta!!)
            }
            .setNegativeButton("Cancelar borrado", null)
            .show()
    }

    private fun eliminarRutaDeBaseDatos(idRuta: Int) {
        val url = "http://192.168.56.1/cycling_together_api/borrar_rutas.php"

        val request = object : StringRequest(Method.POST, url, { response ->
            try {
                val jsonObject = JSONObject(response)
                if (jsonObject.getString("status") == "success") {
                    Toast.makeText(requireContext(), "Ruta eliminada correctamente", Toast.LENGTH_SHORT).show()
                    cargarMisRutas() // Recarga inmediata en el perfil
                } else {
                    Toast.makeText(requireContext(), "Error al eliminar la ruta", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Por si el servidor responde con un warning de PHP en vez de JSON
                cargarMisRutas()
            }
        }, {
            Toast.makeText(requireContext(), "Error de red", Toast.LENGTH_SHORT).show()
        }) {
            override fun getParams(): Map<String, String> {
                return mapOf("id_ruta" to idRuta.toString())
            }
        }

        Volley.newRequestQueue(requireContext()).add(request)
    }
    // Comprobamos la vigencia de las rutas para mostrarlas o no.
    private fun comprobarFecha(fechaRuta: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(fechaRuta)
            date?.after(Date()) ?: false
        } catch (e: Exception) {
            false
        }
    }

    private fun configurarDesplegables() {
        val sexo = listOf("Hombre", "Mujer")
        binding.spinnerSexo.setAdapter(ArrayAdapter(requireContext(), R.layout.list_item, sexo))
        val niveles = listOf("Novato", "Amateur", "Entrenado", "Pro")
        binding.spinnerNivel.setAdapter(ArrayAdapter(requireContext(), R.layout.list_item, niveles))
        val tipos = listOf("MTB", "Carretera", "Gravel", "E-Bike")
        binding.spinnerTipoBici.setAdapter(ArrayAdapter(requireContext(), R.layout.list_item, tipos))
    }
}