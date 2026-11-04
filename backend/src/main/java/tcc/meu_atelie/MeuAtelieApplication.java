package tcc.meu_atelie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MeuAtelieApplication {

	public static void main(String[] args) {
		SpringApplication.run(MeuAtelieApplication.class, args);
	}

}
