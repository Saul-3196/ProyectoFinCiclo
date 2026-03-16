package com.example.proyectofinciclo.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.proyectofinciclo.R
import com.example.proyectofinciclo.databinding.FragmentLoginBinding

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var binding: FragmentLoginBinding

    //Establecemos la vista creada en el xml con el binding. El binding nos dará acceso a todos los widgets del xml.
    //No tendremos la necesidad de llamarlos uno por uno con el find view by id.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)


        // Pulsando el widget se "Crea una cuenta nueva", nos redireccionará a la ventana de registro.
        binding.registerTextView.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_registro)
        }

        // El botón de "iniciar sesión" nos lleva a la ventana principal de la app (home)
        binding.loginButton.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_home)
        }
    }
}