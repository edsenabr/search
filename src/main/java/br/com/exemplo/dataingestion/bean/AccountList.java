package br.com.exemplo.dataingestion.bean;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class AccountList {

	@Value("${br.com.exemplo.dataingestion.accounts-file}")
	private String accountsFile;

	private String[] accounts;

	public String[] get() {
		return this.accounts;
	}

	public int size() {
		return this.accounts.length;
	}

	@PostConstruct
	private void load() throws IOException {
		Path path = Path.of(accountsFile);
		List<String> accounts = Files.readAllLines(path);
		this.accounts = new String[accounts.size()];
		this.accounts = accounts.toArray(this.accounts);
	}
}
