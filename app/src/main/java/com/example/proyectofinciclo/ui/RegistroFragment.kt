package com.example.proyectofinciclo.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.proyectofinciclo.R
import com.example.proyectofinciclo.databinding.FragmentRegistroBinding
import org.json.JSONObject

class RegistroFragment : Fragment(R.layout.fragment_registro) {

    private lateinit var binding: FragmentRegistroBinding
    private val rol = "idRol"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRegistroBinding.bind(view)

        binding.registerButton.setOnClickListener {
            ejecutarRegistroServidor()
        }

        binding.volverLoginTextView.setOnClickListener {
            findNavController().navigate(R.id.loginFragment)
        }
    }

    private fun ejecutarRegistroServidor() {
        //Empleamos el trim para evitar saltos de línea y espacios en blanco
        val nombre = binding.nombreEditText.text.toString().trim()
        val correo = binding.emailEditText.text.toString().trim()
        val pass1 = binding.passwordEditText.text.toString().trim()
        val pass2 = binding.passwordEditText2.text.toString().trim()

        // Validaciones básicas
        if (nombre.isEmpty() || correo.isEmpty() || pass1.isEmpty() || pass2.isEmpty()) {
            Toast.makeText(requireContext(), "Rellena todos los campos", Toast.LENGTH_LONG).show()
            return
        }
        //Que las contraseñas coincidan
        if (pass1 != pass2) {
            Toast.makeText(requireContext(), "Las contraseñas no coinciden", Toast.LENGTH_LONG).show()
            return
        }

        //  Configuración de la conexión, mismo procedimiento que en el login
        val url = "http://192.168.56.1/cycling_together_api/registro.php"
        val conexion = Volley.newRequestQueue(requireContext())

        val peticionRegistro = object : StringRequest(
            Request.Method.POST, url,
            { respuesta ->
                try {
                    val jsonRespuesta = JSONObject(respuesta)
                    if (jsonRespuesta.getString("status") == "success") {
                        Toast.makeText(requireContext(), "Usuario registrado con éxito", Toast.LENGTH_LONG).show()
                        findNavController().popBackStack() // Volver al Login tras el éxito
                    } else {
                        Toast.makeText(requireContext(), jsonRespuesta.getString("message"), Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error en la respuesta del servidor", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                Toast.makeText(requireContext(), "Error de conexión: Verifica tu red", Toast.LENGTH_LONG).show()
            }
        ) {
            //Le enviamos los datos al servidor
            override fun getParams(): Map<String, String> {
                val parametros = HashMap<String, String>()
                parametros["nombre"] = nombre
                parametros["email"] = correo
                parametros["password"] = pass1
                // Por defecto, todo usuario nuevo es un ciclista.
                parametros["id_rol"] = "2"
                return parametros
            }
        }
        conexion.add(peticionRegistro)
    }
}