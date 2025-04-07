package com.challenge.rental_cars_spring_api.access;

import com.challenge.rental_cars_spring_api.core.queries.dtos.ListarAlugueisDto;
import com.challenge.rental_cars_spring_api.services.AluguelService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/alugueis")
@RequiredArgsConstructor
public class AluguelRestController {

    private final AluguelService aluguelService;

    @PostMapping("/processar")
    public ResponseEntity<String> processarAluguel(@RequestParam("file") MultipartFile file) {
        try {
            aluguelService.processarArquivoAluguel(file);
            return ResponseEntity.ok("Arquivo processado com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar o arquivo: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<ListarAlugueisDto>> listarAlugueis() {
        List<ListarAlugueisDto> alugueis = aluguelService.listarAlugueis();
        return ResponseEntity.ok(alugueis);
    }
}