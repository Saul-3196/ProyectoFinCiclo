package com.example.proyectofinciclo.model

// El constructor de las rutas
data class Ruta(
    val id_ruta: Int? = null,
    val id_creador: Int = 0,
    val titulo: String = "",
    val localidad: String = "",
    val distancia: Double = 0.0,
    val desnivel: Int = 0,
    val dificultad: String = "",
    val id_bici: Int = 0,
    val fecha: String = "",
    val hora: String = "",
    val puntoEncuentro: String = "",
    val descripcion: String = "",
    val mapa_trazado: String? = null,
    val nombre_creador: String? = null
)