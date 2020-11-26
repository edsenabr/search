package br.com.exemplo.dataingestion.bean;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class AccountList {

	@Value("${br.com.exemplo.dataingestion.accounts-file}")
	private String accountsFile;

	@Value("${br.com.exemplo.dataingestion.accounts:0}")
	private int accountsNumber;


	private String[] accounts;
	private Random random = new Random();

	public String[] get() {
		return this.accounts;
	}

	public String next() {
		if (accountsNumber <= 0) {
			return 	accounts[random.nextInt(accounts.length)];
		}

		return UUID.nameUUIDFromBytes(
			StringUtils.leftPad(
				String.valueOf(
					random.nextInt(accountsNumber)
				),
				12,
				'0'
			)
			.getBytes()
		).toString();
	}

	public int size() {
		return (accountsNumber <= 0) ? 
			this.accounts.length :
			accountsNumber;
	}

	@PostConstruct
	private void load() throws IOException {
		if (accountsNumber <= 0) {
			Path path = Path.of(accountsFile);
			List<String> accounts = Files.readAllLines(path);
			this.accounts = new String[accounts.size()];
			this.accounts = accounts.toArray(this.accounts);
		}
	}
}
