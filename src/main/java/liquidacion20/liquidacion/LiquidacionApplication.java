package liquidacion20.liquidacion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LiquidacionApplication {

	public static void main(String[] args) {
		SpringApplication.run(LiquidacionApplication.class, args);
	}

}
