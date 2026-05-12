package br.ufpr.dac.mailService.controller;

import br.ufpr.dac.mailService.service.MailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class MailController {

    @Autowired
    private MailService mailService;

    @PostMapping("/send-email")
    public String enviar(@RequestBody EmailRequest request) {

        mailService.enviarEmail(request.destinatario(), request.assunto(), request.conteudoHtml());

        return "E-mail enviado com sucesso!";
    }

    public record EmailRequest(String destinatario, String assunto, String conteudoHtml) {
    }
}
