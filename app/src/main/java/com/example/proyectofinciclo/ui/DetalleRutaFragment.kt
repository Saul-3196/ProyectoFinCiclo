package com.example.proyectofinciclo.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.proyectofinciclo.R
import com.example.proyectofinciclo.databinding.FragmentDetalleRutaBinding
import java.text.SimpleDateFormat
import java.util.*

class DetalleRutaFragment : Fragment(R.layout.fragment_detalle_ruta) {

    private lateinit var binding: FragmentDetalleRutaBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDetalleRutaBinding.bind(view)

        // Controlador del banner de la Activity principal
        actualizarBannerActividad()

        // Lógica del botón volver
        binding.tvVolverDetalle.setOnClickListener {
            findNavController().popBackStack()
        }

        // Recuperamos los datos de los argumentos
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
        val nombreOrganizador = arguments?.getString("creador") ?: "Usuario Desconocido"

        // Recuperamos el ID real del usuario logueado desde SharedPreferences
        val sharedPrefs = requireContext().getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val idUsuarioLogueado = sharedPrefs.getInt("id_usuario", 0)

        // Traducimos el ID de bicicleta a texto
        val textoBici = when(idBici){
            1 -> "Carretera"
            2 -> "MTB"
            3 -> "Gravel"
            4 -> "E-Bike"
            else -> "Desconocido"
        }

        // Verificamos si la ruta está vigente
        val esVigente = comprobarFecha(fechaString)

        // Lógica del botón Unirse
        binding.btnUnirseRuta.apply {
            when {
                !esVigente -> {
                    isEnabled = false
                    text = "Ruta finalizada"
                    setBackgroundColor(android.graphics.Color.GRAY)
                }
                idCreadorRuta == idUsuarioLogueado -> {
                    isEnabled = false
                    text = "Eres el organizador"
                    setBackgroundColor(android.graphics.Color.parseColor("#2196F3"))
                }
                else -> {
                    isEnabled = true
                    text = "Unirse a esta ruta"
                    setBackgroundColor(android.graphics.Color.parseColor("#FF9800"))
                    setOnClickListener {
                        // Simulación de inscripción (Se implementará en la Fase 6)
                        text = "¡Te has unido!"
                        setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"))
                        isEnabled = false
                        Toast.makeText(requireContext(), "Inscripción realizada", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        // Rellenamos la UI
        binding.apply {
            tvDetalleTitulo.text = titulo
            tvDetalleKm.text = "📏 $distancia km"
            tvDetalleDesnivel.text = "⛰️ Desnivel: $desnivelRecibido m"
            tvDetalleDificultad.text = "📊 $dificultad"
            tvDetalleLocalidad.text = "📍 $localidad"
            tvDetalleFecha.text = "📅 $fechaString"
            tvDetalleHora.text = "⏰ $hora"
            tvDetallePunto.text = "🏁 Punto de encuentro: $puntoEncuentro"
            tvDetalleDescripcion.text = descripcion
            tvDetalleTipo.text = "🚴 $textoBici"
            tvDetalleCreador.text = "Organizado por: $nombreOrganizador"
        }
    }

    // Controlador del banner de la Activity principal
    private fun actualizarBannerActividad() {
        val sharedPrefs = requireContext().getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val idRol = sharedPrefs.getInt("id_rol", 2)

        // Buscamos el banner en la MainActivity
        val tvBanner = activity?.findViewById<TextView>(R.id.tvModoAdmin)

        if (idRol == 1) {
            tvBanner?.visibility = View.VISIBLE
        } else {
            tvBanner?.visibility = View.GONE
        }
    }

    private fun comprobarFecha(fechaRuta: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val dateRuta = sdf.parse(fechaRuta)

            // Obtenemos la fecha de hoy sin horas (solo fecha)
            val hoy = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            dateRuta != null && !dateRuta.before(hoy)
        } catch (e: Exception) {
            true
        }
    }
}