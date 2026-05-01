package ru.neoflex.deal.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.neoflex.deal.dto.EmailMessageDto;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, EmailMessageDto> kafkaTemplate;

    public void sendMessage(String topic, EmailMessageDto message) {
        kafkaTemplate.send(topic, message);
    }
}