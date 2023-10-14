package br.com.lucasfranco.todolist.user;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IUesrRepository extends JpaRepository<UserModel, UUID>{
  UserModel findByUsername(String username);
}
