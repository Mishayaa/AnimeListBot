package io.project.MegaCoolAnimeBot.model;

import org.springframework.context.annotation.Bean;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, Long> {


    UserEntity findByChatId(Long chatId);
}
