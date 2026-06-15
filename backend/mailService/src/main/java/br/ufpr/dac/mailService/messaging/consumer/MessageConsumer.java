package br.ufpr.dac.mailService.messaging.consumer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import br.ufpr.dac.mailService.service.MailService;
import br.ufpr.dac.shared.dto.EmailDto;
import br.ufpr.dac.shared.dto.MessageWrapper;
import br.ufpr.dac.shared.keys.MessageOperations;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import lombok.AllArgsConstructor;


@Component
@AllArgsConstructor
public class MessageConsumer {

    private final MailService mailService;

    @RabbitListener(queues = RabbitmqConsts.EMAIL_QUEUE)
    public MessageWrapper<EmailDto> receive(
            MessageWrapper<EmailDto> message) {

        try {

            switch (message.getOperation()) {

                case MessageOperations.SEND -> {

                    EmailDto email = message.getData().getFirst();

                    mailService.enviarEmail(
                        email.getDestinatario(),
                        email.getAssunto(),
                        email.getConteudoHtml()
                    );

                    return new MessageWrapper<>(
                        MessageOperations.RESULT,
                        message.getData()
                    );
                }

                default -> throw new UnsupportedOperationException();

            }

        } catch (Exception e) {

            e.printStackTrace();

            return new MessageWrapper<>(
                MessageOperations.ERROR_GENERIC,
                null
            );
        }
    }
}