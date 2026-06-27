package com.yowyob.auth.infrastructure.adapter.out.persistence;

import com.yowyob.auth.application.port.out.UserRepositoryPort;
import com.yowyob.auth.domain.model.User;
import com.yowyob.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component("hexagonalUserRepositoryAdapter")
@RequiredArgsConstructor
public class JpaUserRepositoryAdapter implements UserRepositoryPort {

    private final UserRepository userRepository;

    @Override
    public User save(User user) {
        com.yowyob.auth.entity.User entity = UserMapper.toEntity(user);
        com.yowyob.auth.entity.User savedEntity = userRepository.save(entity);
        return UserMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email).map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id).map(UserMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
