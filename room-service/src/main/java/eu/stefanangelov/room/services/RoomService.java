package eu.stefanangelov.room.services;

import static eu.stefanangelov.common.kafka.Topics.ROOM_EVENT;

import eu.stefanangelov.room.persistence.entity.Room;
import eu.stefanangelov.room.persistence.repository.RoomRepository;
import eu.stefanangelov.room.services.dto.CreateUpdateRoomDTO;
import eu.stefanangelov.room.services.dto.RoomDTO;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class RoomService {

	private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RoomRepository roomRepository;

    public List<RoomDTO> getRooms(LocalDateTime from, LocalDateTime to) {
        return  roomRepository.findAllByAvailabilitiesFromDateLessThanEqualAndAvailabilitiesToDateGreaterThanEqual(from,to)
            .stream()
            .map(room -> new RoomDTO(UUID.fromString(room.getId()), room.getName()))
            .collect(Collectors.toList());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RoomDTO createRoom(CreateUpdateRoomDTO roomDTO) {
        var room = roomRepository.save(new Room(roomDTO.getName(), new HashSet<>()));
		var createdRoom = new RoomDTO(UUID.fromString(room.getId()), room.getName());
        kafkaTemplate.send(ROOM_EVENT,createdRoom);
        return new RoomDTO(UUID.fromString(room.getId()), room.getName());
    }

    public void updateAvailability(CreateUpdateRoomDTO createUpdateRoomDTO) {

    }
}
