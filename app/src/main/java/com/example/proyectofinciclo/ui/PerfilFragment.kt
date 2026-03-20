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
import com.example.proyectofinciclo.R
import com.example.proyectofinciclo.adapter.RutaAdapter
import com.example.proyectofinciclo.databinding.FragmentPerfilBinding
import com.example.proyectofinciclo.model.Ruta
import java.text.SimpleDateFormat
import java.util.*

class PerfilFragment : Fragment(R.layout.fragment_perfil) {

    private lateinit var binding: FragmentPerfilBinding
    private lateinit var adapter: RutaAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPerfilBinding.bind(view)

        // Controlador del banner de la Activity principal
        actualizarBannerActividad()

        configurarDesplegables()
        setupRecyclerView()

        binding.etFechaNacimiento.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                val fechaSeleccionada = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                binding.etFechaNacimiento.setText(fechaSeleccionada)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

            datePickerDialog.show()
        }

        binding.btnGuardarPerfil.setOnClickListener {
            Toast.makeText(requireContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show()
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

    // Controlador del banner de la Activity principal
    private fun actualizarBannerActividad() {
        val sharedPrefs = requireContext().getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val idRol = sharedPrefs.getInt("id_rol", 2)

        // Accedemos al TextView de la MainActivity
        val tvBanner = activity?.findViewById<TextView>(R.id.tvModoAdmin)

        if (idRol == 1) {
            tvBanner?.visibility = View.VISIBLE
        } else {
            tvBanner?.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        val sharedPrefs = requireContext().getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val idLogueado = sharedPrefs.getInt("id_usuario", 0)
        val rolLogueado = sharedPrefs.getInt("id_rol", 2)

        val misRutas = listOf(
            Ruta(
                id_ruta = 1,
                titulo = "Clásica Somiedo 2026",
                distancia = 145.0,
                desnivel = 3400,
                dificultad = "Extrema",
                localidad = "Pola de Somiedo",
                id_bici = 1,
                fecha = "15/05/2026",
                hora = "09:00",
                puntoEncuentro = "Plaza del Ayuntamiento",
                descripcion = "Ruta de alta montaña con tres puertos de primera categoría.",
                id_creador = idLogueado,
                mapa_trazado = null
            ),
            Ruta(
                id_ruta = 2,
                titulo = "Ruta Playas de Gijón",
                distancia = 20.0,
                desnivel = 20,
                dificultad = "Baja",
                localidad = "Gijón",
                id_bici = 1,
                fecha = "10/01/2023",
                hora = "10:00",
                puntoEncuentro = "Puerto Deportivo",
                descripcion = "Paseo costero tranquilo por la zona del muro.",
                id_creador = idLogueado,
                mapa_trazado = null
            )
        )

        adapter = RutaAdapter(
            misRutas,
            rolLogueado,
            idLogueado,
            { ruta -> irADetalle(ruta) },
            { ruta -> mostrarDialogoBorrar(ruta) }
        )

        binding.rvMisRutas.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@PerfilFragment.adapter
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
            .setTitle("Eliminar mi ruta")
            .setMessage("¿Deseas eliminar '${ruta.titulo}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                Toast.makeText(requireContext(), "Funcionalidad de borrado lista", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun comprobarFecha(fechaRuta: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
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