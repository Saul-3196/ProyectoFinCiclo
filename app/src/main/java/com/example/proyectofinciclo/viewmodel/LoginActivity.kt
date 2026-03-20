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

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_LONG).show()
            return
        }

        val url = "http://192.168.56.1/cycling_together_api/login.php"
        val conexion = Volley.newRequestQueue(this)

        val peticionLogin = object : StringRequest(
            Request.Method.POST, url,
            { respuesta ->
                val jsonRespuesta = JSONObject(respuesta)
                if (jsonRespuesta.getString("status") == "success") {
                    val idUsuario = jsonRespuesta.getInt("id_usuario")
                    val idRol = jsonRespuesta.getInt("id_rol")
                    val nombre = jsonRespuesta.getString("nombre")

                    Toast.makeText(this, "Bienvenido $nombre", Toast.LENGTH_LONG).show()
                    // Guardamos los datos en el SharedPreferences
                    val miSharedPrefs = getSharedPreferences(PREFS, MODE_PRIVATE)
                    //val escritor = miSharedPrefs.edit()

                    // Borramos el rastro previo de login
                    //escritor.clear()
                    //escritor.apply() // Aplicamos el borrado inmediatamente

                    // Guardamos los datos nuevos
                    val nuevoEscritor = miSharedPrefs.edit()
                    nuevoEscritor.putInt("id_rol", idRol) // Usamos la llave directa "id_rol"
                    nuevoEscritor.putInt("id_usuario", idUsuario)
                    nuevoEscritor.putString("nombre_usuario", nombre)

                    // Recordamos usuario si está chequeado
                    if (binding.cbRecordar.isChecked) {
                        nuevoEscritor.putString(LLAVE_CORREO, email)
                        nuevoEscritor.putBoolean(LLAVE_CHECKBOX, true)
                    }else{
                        nuevoEscritor.remove(LLAVE_CORREO)
                        nuevoEscritor.remove(LLAVE_CHECKBOX)
                    }
                    nuevoEscritor.apply()

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