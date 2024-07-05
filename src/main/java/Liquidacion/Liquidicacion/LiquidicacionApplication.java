package Liquidacion.Liquidicacion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LiquidicacionApplication {

	public static void main(String[] args) {
		SpringApplication.run(LiquidicacionApplication.class, args);
	}

}
