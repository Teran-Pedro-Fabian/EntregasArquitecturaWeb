package com;


import com.Repositorys.CarreraRepository;
import com.Repositorys.EstudianteCarreraRepository;
import com.Repositorys.EstudianteRepository;
import com.utils.CargarDatosCSV;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
    public class SpringApp {

    public static void main(String[] args) {
        SpringApplication.run(SpringApp.class, args);
    }

    @Bean
    public CommandLineRunner cargarDatos(CargarDatosCSV cargarDatosCSV, CarreraRepository carreraRepo,
                                         EstudianteRepository estudianteRepo,
                                         EstudianteCarreraRepository estudianteCarreraRepo) {
        return args -> {
            if (carreraRepo.count() == 0 && estudianteRepo.count() == 0 && estudianteCarreraRepo.count() == 0) {
                cargarDatosCSV.cargarTodo();
            } else {
                System.out.println("La DB ya esta cargada");
            }
        };
    }
}