package com.example.proyectofinciclo

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.proyectofinciclo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val prefs = getSharedPreferences("preferences", Context.MODE_PRIVATE)
            val idRol = prefs.getInt("id_rol", 2)

            // Controlamos qué se muestra dependiendo de la ventana
            if (destination.id == R.id.registroFragment || destination.id == R.id.loginFragment) {
                //En caso de cerrar la app sin cerrar sesión, limpiamos el sharedPreferences
                prefs.edit().clear().apply()
                // En Login o Registro, la pantalla debe estar limpia
                binding.bottomNavigation.visibility = View.GONE
                binding.tvModoAdmin.visibility = View.GONE
            } else {
                binding.bottomNavigation.visibility = View.VISIBLE

                // Mostramos el banner sólo en caso de que el id_rol sea 1 (Administrador)
                if (idRol == 1) {
                    binding.tvModoAdmin.visibility = View.VISIBLE
                } else {
                    binding.tvModoAdmin.visibility = View.GONE
                }
            }
        }

        // Si venimos impulsados por un Intent explícito al registro
        if (intent.getBooleanExtra("VengoDelLogin", false)) {
            navController.navigate(R.id.registroFragment)
        }
    }
}