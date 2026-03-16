package com.example.proyectofinciclo.model

/*La data class implementa directamente los métodos necesarios, getters, setters...etc
Creamos la clase de los usuarios con unos valores por defecto ya que son imprescindibles a la hora de
trabajar luego con la Base de Datos
*/
data class Usuario(
    val usuarioID: String = "",
    val nombre: String = "",
    val email: String = "",
    val fechaRegistro: Long = 0L
)