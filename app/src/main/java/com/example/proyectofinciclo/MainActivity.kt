package com.example.proyectofinciclo

import android.content.Context // Añade esta importación
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

        // Usamos el sharedPrefferences creado en el login para saber si es admin o no
        val prefs = getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val idRol = prefs.getInt("id_rol", 2)

        // Si es admin, mostramos el banner nada más abrir la actividad
        if (idRol == 1) {
            binding.tvModoAdmin.visibility = View.VISIBLE
        } else {
            binding.tvModoAdmin.visibility = View.GONE
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Controlamos el menú inferior
            if (destination.id == R.id.registroFragment || destination.id == R.id.loginFragment) {
                binding.bottomNavigation.visibility = View.GONE
                // Ocultamos el banner del login y el registro
                binding.tvModoAdmin.visibility = View.GONE
            } else {
                binding.bottomNavigation.visibility = View.VISIBLE
                // Al navegar por el Home/Perfil, si es administrador, mantenemos el banner
                if (idRol == 1) {
                    binding.tvModoAdmin.visibility = View.VISIBLE
                }
            }
        }

        if (intent.getBooleanExtra("VengoDelLogin", false)) {
            navController.navigate(R.id.registroFragment)
        }
    }
}