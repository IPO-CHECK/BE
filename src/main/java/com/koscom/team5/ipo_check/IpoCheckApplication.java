package com.koscom.team5.ipo_check;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

//@EnableScheduling
@SpringBootApplication
public class IpoCheckApplication {
@EnableConfigurationProperties(DartProperties.class)
public class DartApplication {

	public static void main(String[] args) {
		SpringApplication.run(IpoCheckApplication.class, args);
	}

}
