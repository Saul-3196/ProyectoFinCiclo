package com.example.proyectofinciclo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinciclo.R
import com.example.proyectofinciclo.databinding.ListadoRutasBinding
import com.example.proyectofinciclo.model.Ruta

class RutaAdapter(
    private var listaRutas: List<Ruta>,
    private val idRol: Int,
    private val idUsuarioActual: Int,
    private val onClickListener: (Ruta) -> Unit,
    private val onDeleteClick: (Ruta) -> Unit // Recibimos la función de borrado
) : RecyclerView.Adapter<RutaAdapter.RutaViewHolder>() {

    inner class RutaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ListadoRutasBinding.bind(view)

        fun render(ruta: Ruta, onClickListener: (Ruta) -> Unit, onDeleteClick: (Ruta) -> Unit) {
            binding.tvTituloItem.text = ruta.titulo
            binding.tvLocalidadItem.text = ruta.localidad
            binding.tvDistanciaItem.text = "${ruta.distancia} km"
            binding.tvDificultadItem.text = "Dificultad: ${ruta.dificultad}"
            binding.tvDesnivelItem.text = "⛰️ ${ruta.desnivel} m"
            binding.tvNombreCreadorItem.text = "Por: ${ruta.nombre_creador ?: "Usuario"}"

            val tipoCiclismo = when(ruta.id_bici){
                1-> "Carretera"
                2-> "MTB"
                3-> "Gravel"
                4-> "E-Bike"
                else -> "Desconocido"
            }
            binding.tvTipoCiclismo.text = tipoCiclismo

            // Lógica de colores para la dificultad
            val colorDificultad = when (ruta.dificultad.lowercase().trim()) {
                "baja" -> android.graphics.Color.parseColor("#4DF527")
                "media" -> android.graphics.Color.parseColor("#FFC107")
                "alta" -> android.graphics.Color.parseColor("#F58727")
                "muy alta" -> android.graphics.Color.parseColor("#961208")
                "extrema" -> android.graphics.Color.parseColor("#490669")
                else -> android.graphics.Color.GRAY
            }
            binding.tvDificultadItem.setTextColor(colorDificultad)

            // Lógica para el botón eliminar (Admin o Dueño)
            if (idRol == 1 || ruta.id_creador == idUsuarioActual) {
                binding.btnEliminarRuta.visibility = View.VISIBLE
                binding.btnEliminarRuta.setOnClickListener {
                    // LLAMADA A LA FUNCIÓN DE BORRADO REAL
                    onDeleteClick(ruta)
                }
            } else {
                binding.btnEliminarRuta.visibility = View.GONE
            }

            itemView.setOnClickListener { onClickListener(ruta) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RutaViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return RutaViewHolder(layoutInflater.inflate(R.layout.listado_rutas, parent, false))
    }

    override fun onBindViewHolder(holder: RutaViewHolder, position: Int) {
        // Pasamos también onDeleteClick al render
        holder.render(listaRutas[position], onClickListener, onDeleteClick)
    }

    override fun getItemCount(): Int = listaRutas.size

    fun updateRutas(nuevaLista: List<Ruta>) {
        this.listaRutas = nuevaLista
        notifyDataSetChanged()
    }
}