package br.ufpr.dac.apiGatewayService.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.ufpr.dac.apiGatewayService.messaging.dto.UsersDto;
import br.ufpr.dac.apiGatewayService.messaging.producer.MessageProducer;

@CrossOrigin
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

  @Autowired
  MessageProducer producer;

  @GetMapping
  public ResponseEntity<List<UsersDto.Cliente>> getAllClientes() {
    List<UsersDto.Cliente> response = producer.RequestOrchestrator("READ_ALL", null);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<UsersDto.Cliente> getCliente(@PathVariable String id) {
    List<UsersDto.Cliente> response = producer.RequestOrchestrator("READ",
        List.of(
            UsersDto.Cliente.builder()
                .id(Integer.parseInt(id))
                .build()));
    return ResponseEntity.ok(response.getFirst());
  }

  @PostMapping
  public ResponseEntity<UsersDto.Cliente> newClientes(@RequestBody UsersDto.Cliente cliente) {
    List<UsersDto.Cliente> response = producer.RequestOrchestrator("CREATE", List.of(cliente));
    return ResponseEntity.ok(response.getFirst());
  }

  @PutMapping("/{id}")
  public ResponseEntity<UsersDto.Cliente> updateClientes(@PathVariable String id,
      @RequestBody UsersDto.Cliente cliente) {
    List<UsersDto.Cliente> response = producer.RequestOrchestrator("UPDATE", List.of(cliente));
    return ResponseEntity.ok(response.getFirst());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<UsersDto.Cliente> deleteClientes(@PathVariable String id) {
    List<UsersDto.Cliente> response = producer.RequestOrchestrator("DELETE",
        List.of(
            UsersDto.Cliente.builder()
                .id(Integer.parseInt(id))
                .build()));
    return ResponseEntity.ok(response.getFirst());
  }

}
