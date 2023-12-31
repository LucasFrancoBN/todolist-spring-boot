package br.com.lucasfranco.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.lucasfranco.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {

  @Autowired
  private ITaskRepository taskRepository;

  @PostMapping("/")
  public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
    var idUser = (UUID) request.getAttribute("idUser");
    taskModel.setIdUser(idUser);

    var currentDate = LocalDateTime.now();
    if (currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("A data de início / data final deve ser maior do que a data atual");
    }

    if (taskModel.getStartAt().isAfter(taskModel.getEndAt())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data inicial deve ser maior do que a data final");
    }

    var task = this.taskRepository.save(taskModel);
    return ResponseEntity.status(HttpStatus.CREATED).body(task);
  }

  @GetMapping("/")
  public List<TaskModel> list(HttpServletRequest request) {
    var idUser = (UUID) request.getAttribute("idUser");
    var tasks = this.taskRepository.findByIdUser(idUser);
    return tasks;
  }

  @PutMapping("/{id}")
  public ResponseEntity update(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID id) {
    var task = this.taskRepository.findById(id).orElse(null);
    var user = request.getAttribute("idUser");
    if (task == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tarefa não encontrada");
    }

    if (!task.getIdUser().equals(user)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A tarefa não pertence a esse usuário");
    }

    Utils.copyNonNullProperties(taskModel, task);
    var taskUpdated = this.taskRepository.save(task);

    return ResponseEntity.status(HttpStatus.ACCEPTED).body(taskUpdated);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity delete(HttpServletRequest request, @PathVariable UUID id) {
    var task = this.taskRepository.findById(id).orElse(null);
    var idUser = request.getAttribute("idUser");

    if (task == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Task não encontrada");
    }

    if (!task.getIdUser().equals(idUser)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A task não pertence a esse usuário");
    }

    taskRepository.delete(task);

    return ResponseEntity.status(HttpStatus.ACCEPTED).body("Task deletada com sucesso");

  }

}
