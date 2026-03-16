package com.example.proyectofinciclo.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
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

        configurarDesplegables()
        setupRecyclerView()

        // Accionador para cuando pulsamos sobre fecha nacimiento nos aparezca automáticamente el calendario.
        binding.etFechaNacimiento.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                // Importante darle el mismo formato de fecha que en la base de datos (AAAA/MM/DD)
                val fechaSeleccionada = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                binding.etFechaNacimiento.setText(fechaSeleccionada)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

            datePickerDialog.show()
        }

        binding.btnGuardarPerfil.setOnClickListener {
            Toast.makeText(requireContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show()
        }

        binding.btnLogOut.setOnClickListener {
            Toast.makeText(requireContext(), "Hasta Pronto", Toast.LENGTH_SHORT).show()

            // Creamos un intent para ir al LoginActivity
            // Usamos FLAG_ACTIVITY_NEW_TASK y FLAG_ACTIVITY_CLEAR_TASK para borrar el rastro del rol anterior en este caso.
            val intent = Intent(requireContext(), com.example.proyectofinciclo.viewmodel.LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        // Simulamos rutas: una futura y una pasada (10/01/2023)
        val misRutas = listOf(
            Ruta(
                id_ruta =  1,
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
                id_creador = 1,
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
                id_creador = 1,
                mapa_trazado = null
            )
        )

        adapter = RutaAdapter(misRutas, idRol = id, idUsuarioActual = id) { ruta ->
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

                // CLAVE: Calculamos si la fecha ya pasó
                putBoolean("esVigente", comprobarFecha(ruta.fecha))
            }
            findNavController().navigate(R.id.detalleRutaFragment, bundle)
        }

        binding.rvMisRutas.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@PerfilFragment.adapter
        }
    }

    private fun comprobarFecha(fechaRuta: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = sdf.parse(fechaRuta)
            // Retorna true si la fecha de la ruta es después de "ahora"
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