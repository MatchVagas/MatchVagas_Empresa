package com.edu.matchvagasempresas;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        setContentView(R.layout.activity_main);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        NavigationUI.setupWithNavController(bottomNav, navController);

        Set<Integer> hiddenDestinations = new HashSet<>(Arrays.asList(
                R.id.loginFragment,
                R.id.cadastroEmpresaFragment,
                R.id.cadastroVagaFragment,
                R.id.editarVagaFragment,
                R.id.detalhesCandidaturaFragment,
                R.id.gerenciarVagaFragment,
                R.id.editarPerfilEmpresaFragment
        ));

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (hiddenDestinations.contains(destination.getId())) {
                bottomNav.setVisibility(View.GONE);
            } else {
                bottomNav.setVisibility(View.VISIBLE);
            }
        });
    }
}
