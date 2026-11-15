package dev.slot;

import dev.slot.shared.config.SlotRuntimeHints;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;

@SpringBootApplication
@ImportRuntimeHints(SlotRuntimeHints.class)
public class SlotApplication {

	public static void main(String[] args) {
		SpringApplication.run(SlotApplication.class, args);
	}

}
