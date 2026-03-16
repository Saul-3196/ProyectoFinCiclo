package com.example.proyectofinciclo.viewmodel

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.proyectofinciclo.MainActivity
import com.example.proyectofinciclo.databinding.FragmentLoginBinding
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: FragmentLoginBinding

    // Nombres para el sharedPrefferences
    private val PREFS = "preferences"
    private val LLAVE_CORREO = "email_usuario"
    private val LLAVE_CHECKBOX = "recordar_activado"

    private val rol = "id_rol"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Cargamos los datos almacenados
        val misDatos = getSharedPreferences(PREFS, MODE_PRIVATE)
        val correoGuardado = misDatos.getString(LLAVE_CORREO, "")
        val estaChequeado = misDatos.getBoolean(LLAVE_CHECKBOX, false)

        if (estaChequeado) {
            binding.emailEditText.setText(correoGuardado)
            binding.cbRecordar.isChecked = true
        }

        binding.loginButton.setOnClickListener { ejecutarLogin() }

        binding.registerTextView.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("VengoDelLogin", true)
            }
            startActivity(intent)
        }
    }

    private fun ejecutarLogin() {
        val email = binding.emailEditText.text.toString()
        val password = binding.passwordEditText.text.toString()
        //Condiciones para el login exitoso
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_LONG).show()
            return
        }
        //Configuración de la conexión
        val url = "http://192.168.56.1/cycling_together_api/login.php"
        val conexion = Volley.newRequestQueue(this) //

        val peticionLogin = object : StringRequest(
            Request.Method.POST, url,
            { respuesta ->
                val jsonRespuesta = JSONObject(respuesta)
                if (jsonRespuesta.getString("status") == "success") {
                    val idUsuario = jsonRespuesta.getInt("id_usuario")
                    val idRol = jsonRespuesta.getInt("id_rol")
                    val nombre = jsonRespuesta.getString("nombre")

                    Toast.makeText(this, "Bienvenido $nombre", Toast.LENGTH_LONG).show()

                    // Guardamos el estado del check en el sharedPrefferences
                    val miSharedPrefs = getSharedPreferences(PREFS, MODE_PRIVATE)
                    val escritor = miSharedPrefs.edit()
                    // Guardamos el id del usuario y el rol
                    escritor.putInt(rol,idRol)
                    escritor.putInt("id_usuario", idUsuario)
                    escritor.putString("nombre_usuario", "$nombre")
                    // Si está marcado el checkbox, guardamos el email
                    if (binding.cbRecordar.isChecked) {
                        escritor.putString(LLAVE_CORREO, email)
                        escritor.putBoolean(LLAVE_CHECKBOX, true)
                    } else {
                        // Si no está marcado, borramos el email
                        escritor.remove(LLAVE_CORREO)
                        escritor.remove(LLAVE_CHECKBOX)
                    }
                    escritor.apply()

                    // Ir al Main con Flags para limpiar sesión
                    val intent = Intent(this, MainActivity::class.java).apply {
                        putExtra("ID_USUARIO", idUsuario)
                        putExtra("ID_ROL", idRol)
                        putExtra("NOMBRE_USUARIO", nombre)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Error: " + jsonRespuesta.getString("message"), Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                Toast.makeText(this, "Error de conexión", Toast.LENGTH_LONG).show()
            }) {
            override fun getParams(): Map<String, String> {
                return mapOf("email" to email, "password" to password)
            }
        }
        conexion.add(peticionLogin)
    }
}