package br.com.exemplo.dataingestion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import br.com.exemplo.dataingestion.controller.SearchController;

@SpringBootApplication
@EnableAsync 
@EnableScheduling
public class SearchApplication implements CommandLineRunner {

	@Autowired
	private SearchController searchController;

	public static void main(String[] args) {
		SpringApplication.run(SearchApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		searchController.search();
	}
}
