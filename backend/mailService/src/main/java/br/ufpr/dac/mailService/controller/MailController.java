package br.ufpr.dac.mailService.controller;

import br.ufpr.dac.mailService.service.MailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/email")
public class MailController {

    @Autowired
    private MailService mailService;

    @PostMapping("/send")
    public String enviar(
            @RequestParam String destinatario,
            @RequestParam String assunto,
            @RequestParam String conteudo
    ) {

        mailService.enviarEmail(destinatario, assunto, conteudo);

        return "E-mail enviado com sucesso!";
    }
}