package com.challenge.rental_cars_spring_api.services;

import com.challenge.rental_cars_spring_api.core.domain.Aluguel;
import com.challenge.rental_cars_spring_api.infrastructure.repositories.AluguelRepository;
import com.challenge.rental_cars_spring_api.infrastructure.repositories.CarroRepository;
import com.challenge.rental_cars_spring_api.infrastructure.repositories.ClienteRepository;
import com.challenge.rental_cars_spring_api.core.queries.dtos.ListarAlugueisDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AluguelService {

    private final AluguelRepository aluguelRepository;
    private final CarroRepository carroRepository;
    private final ClienteRepository clienteRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

public void processarArquivoAluguel(MultipartFile file) throws Exception {
    BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
    String line;

    while ((line = reader.readLine()) != null) {
        if (line.length() != 20) {
            System.out.println("Linha inválida: " + line);
            continue;
        }

        try {
            String carroIdStr = line.substring(0, 2).trim();
            String clienteIdStr = line.substring(2, 4).trim();
            LocalDate dataAluguel = LocalDate.parse(line.substring(4, 12).trim(), DATE_FORMATTER);
            LocalDate dataDevolucao = LocalDate.parse(line.substring(12, 20).trim(), DATE_FORMATTER);

            Long carroId = Long.valueOf(carroIdStr);
            Long clienteId = Long.valueOf(clienteIdStr);

            var carroOptional = carroRepository.findById(carroId);
            var clienteOptional = clienteRepository.findById(clienteId);

            if (carroOptional.isPresent() && clienteOptional.isPresent()) {
                var carro = carroOptional.get();
                long diasAlugados = java.time.temporal.ChronoUnit.DAYS.between(dataAluguel, dataDevolucao);
                BigDecimal valor = carro.getVlrDiaria().multiply(BigDecimal.valueOf(diasAlugados));

                Aluguel aluguel = new Aluguel(carro, clienteOptional.get(), dataAluguel, dataDevolucao, valor, true);
                aluguelRepository.save(aluguel);
            } else {
                String missingIds = (!carroOptional.isPresent() ? "Carro ID: " + carroId : "") +
                                    (!clienteOptional.isPresent() ? " Cliente ID: " + clienteId : "");
                System.out.println("Alerta: " + missingIds + " não encontrado.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao processar a linha: " + line + " - " + e.getMessage());
        }
    }
}


    public List<ListarAlugueisDto> listarAlugueis() {
        return aluguelRepository.findAll().stream()
                .map(aluguel -> new ListarAlugueisDto(
                        aluguel.getDataAluguel(),
                        aluguel.getCarro().getModelo(),
                        aluguel.getCarro().getKm(),
                        aluguel.getCliente().getNome(),
                        formatTelefone(aluguel.getCliente().getTelefone()),
                        aluguel.getDataDevolucao(),
                        aluguel.getValor(),
                        aluguel.isPago() ? "SIM" : "NAO"))
                .collect(Collectors.toList());
    }

    private String formatTelefone(String telefone) {

        return String.format("+%s(%s)%s-%s", telefone.substring(0, 2), telefone.substring(2, 4), telefone.substring(4, 9), telefone.substring(9));
    }
}