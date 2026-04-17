package com.example.proyectofinciclo.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.proyectofinciclo.R
import com.example.proyectofinciclo.databinding.FragmentLoginBinding
import org.json.JSONObject

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var binding: FragmentLoginBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)

        binding.registerTextView.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_registro)
        }

        binding.loginButton.setOnClickListener {
            ejecutarLoginServidor()
        }
    }

    private fun ejecutarLoginServidor() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_LONG).show()
            return
        }

        val url = "http://192.168.56.1/cycling_together_api/login.php"
        val conexion = Volley.newRequestQueue(requireContext())

        val peticionLogin = object : StringRequest(
            Request.Method.POST, url,
            { respuesta ->
                try {
                    val jsonRespuesta = JSONObject(respuesta)
                    if (jsonRespuesta.getString("status") == "success") {

                        val idUsuario = jsonRespuesta.getInt("id_usuario")
                        val idRol = jsonRespuesta.getInt("id_rol")
                        val nombre = jsonRespuesta.getString("nombre")

                        Toast.makeText(requireContext(), "Bienvenido $nombre", Toast.LENGTH_LONG).show()

                        // Guardamos los datos en SharedPreferences
                        val miSharedPrefs = requireContext().getSharedPreferences("preferences", Context.MODE_PRIVATE)
                        val nuevoEscritor = miSharedPrefs.edit()

                        nuevoEscritor.putInt("id_rol", idRol)
                        nuevoEscritor.putInt("id_usuario", idUsuario)
                        nuevoEscritor.putString("nombre_usuario", nombre)
                        nuevoEscritor.commit() // Usamos commit para guardado inmediato

                        // Una vez guardado, saltamos al Home.
                        findNavController().navigate(R.id.action_login_to_home)

                    } else {
                        Toast.makeText(requireContext(), "Error: " + jsonRespuesta.getString("message"), Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Error procesando datos", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                return mapOf("email" to email, "password" to password)
            }
        }
        conexion.add(peticionLogin)
    }
}