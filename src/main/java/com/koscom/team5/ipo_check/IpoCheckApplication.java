package com.koscom.team5.ipo_check;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

@SpringBootApplication
@EnableConfigurationProperties(financial.dart.config.DartProperties.class)
@EntityScan(basePackages = {
		"financial.dart.domain",
		"financial.dart.vector.domain"
})
@EnableJpaRepositories(basePackages = {"financial.dart.repository"})
@org.springframework.context.annotation.ComponentScan(
		basePackages = {
				"com.koscom.team5.ipo_check",
				"financial.dart"
		},
		excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
				type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
				classes = com.koscom.team5.ipo_check.controller.ListedCorpApiController.class
		)
)
public class IpoCheckApplication {
	public static void main(String[] args) {
		SpringApplication.run(IpoCheckApplication.class, args);
	}

}
