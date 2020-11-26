package br.com.exemplo.dataingestion.bean;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Random;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class Datas {

	@DateTimeFormat(iso = ISO.DATE)
	@Value("${br.com.exemplo.dataingestion.ultimo-dia}")
	private LocalDate ultimoDia;

	@DateTimeFormat(iso = ISO.DATE)
	@Value("${br.com.exemplo.dataingestion.primeiro-dia}")
	private LocalDate primeiroDia;

	@Value("${br.com.exemplo.dataingestion.periodo}")
	private int periodo;

	private int range;

	private Random random = new Random();

	@PostConstruct
	private void setup() {
		int diferenca = (int)ChronoUnit.DAYS.between(primeiroDia, ultimoDia) + 1; //inclusive
		if (periodo > diferenca) {
			throw new RuntimeException("periodo de consulta maior que a diferença das datas");
		}
		range =  diferenca - periodo;
	}
	
	public Range next() {
		int start = random.nextInt(range + 1); //inclusive
		LocalDate termino = ultimoDia.minusDays(start);
		LocalDate inicio = termino.minusDays(periodo - 1);
		return new Range(inicio, termino);
	}

	public class Range {
		private Range(LocalDate start, LocalDate end) {
			this.start = start;
			this.end = end;
		}

		private final LocalDate start;
		private final LocalDate end;

		public String inicio() {
			return format(start);
		}

		public String termino() {
			return format(end);
		}

		private String format(LocalDate date) {
			return OffsetDateTime.of(
				date,
				LocalTime.MIDNIGHT, 
				ZoneOffset.ofHours(-3)
			).format(
					DateTimeFormatter.ISO_DATE_TIME
			);
		}
	}
}

