package com.soportehub.backend;

//import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SoporteHubBackendApplication {

	public static void main(String[] args) {

		// Carga el archivo .env de la raíz del proyecto y lo inyecta como propiedades del sistema
        //Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        //dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

		SpringApplication.run(SoporteHubBackendApplication.class, args);
	}

}
