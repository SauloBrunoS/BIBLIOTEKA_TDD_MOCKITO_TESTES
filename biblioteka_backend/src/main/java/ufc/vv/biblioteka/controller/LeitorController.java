package ufc.vv.biblioteka.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.persistence.EntityNotFoundException;
import ufc.vv.biblioteka.model.Emprestimo;
import ufc.vv.biblioteka.model.EmprestimoUtils;
import ufc.vv.biblioteka.model.Leitor;
import ufc.vv.biblioteka.model.OnCreate;
import ufc.vv.biblioteka.model.OnUpdate;
import ufc.vv.biblioteka.model.Reserva;
import ufc.vv.biblioteka.model.StatusReserva;
import ufc.vv.biblioteka.repository.EmprestimoRepository;
import ufc.vv.biblioteka.repository.LeitorRepository;
import ufc.vv.biblioteka.repository.ReservaRepository;
import ufc.vv.biblioteka.service.LeitorService;

import org.springframework.data.domain.Page;

@RepositoryRestController("/leitores")
public class LeitorController {

    private final LeitorService leitorService;
    private final LeitorRepository leitorRepository;
    private final EmprestimoRepository emprestimoRepository;
    private final ReservaRepository reservaRepository;

    @Autowired
    public LeitorController(LeitorService leitorService, LeitorRepository leitorRepository,
            EmprestimoRepository emprestimoRepository,
            ReservaRepository reservaRepository) {
        this.leitorRepository = leitorRepository;
        this.leitorService = leitorService;
        this.emprestimoRepository = emprestimoRepository;
        this.reservaRepository = reservaRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Leitor> getLeitorById(@PathVariable int id) {
        return leitorRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ResponseObject> criarLeitor(@Validated(OnCreate.class) @RequestBody Leitor leitor) {
        try {
            Leitor novoLeitor = leitorService.criarLeitor(leitor);
            return ResponseEntity.ok(new ResponseObject(HttpStatus.OK, "Leitor criado com sucesso", novoLeitor));
        } catch (DuplicateKeyException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ResponseObject(HttpStatus.CONFLICT, "Leitor já existe", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseObject(HttpStatus.BAD_REQUEST, "Dados inválidos", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao criar leitor", null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject> atualizarLeitor(@PathVariable int id, @Validated(OnUpdate.class) @RequestBody Leitor leitorAtualizado) {
        try {
            Leitor leitorAtualizadoResponse = leitorService.atualizarLeitor(id, leitorAtualizado);
            return ResponseEntity
                    .ok(new ResponseObject(HttpStatus.OK, "Leitor atualizado com sucesso", leitorAtualizadoResponse));
        } catch (DuplicateKeyException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ResponseObject(HttpStatus.CONFLICT, "Leitor já existe", e.getMessage()));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseObject(HttpStatus.NOT_FOUND, "Leitor não encontrado", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseObject(HttpStatus.BAD_REQUEST, "Dados inválidos", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao atualizar leitor", null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject> excluirLeitor(@PathVariable int id) {
        try {
            leitorService.excluirLeitor(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseObject(HttpStatus.NOT_FOUND, "Leitor não encontrado", null));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ResponseObject(HttpStatus.CONFLICT,
                            "Não é possível excluir o leitor. Existem dados associados.",
                            e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao excluir leitor", null));
        }
    }

    @GetMapping("/{leitorId}/emprestimos")
    public ResponseEntity<Page<EmprestimoResponseDTO>> getEmprestimosPorLeitorId(@PathVariable("leitorId") int leitorId,
            @RequestParam(required = false) String search, @RequestParam(required = false) Integer livroId,
            @RequestParam(required = false) Boolean devolvido,
            Pageable pageable) {
        Page<Emprestimo> emprestimos = emprestimoRepository.findByLeitorIdAndSearch(leitorId, search, livroId,
                devolvido, pageable);
        Page<EmprestimoResponseDTO> emprestimosComValores = emprestimos.map(emprestimo -> {
            double multa = EmprestimoUtils.calcularMulta(emprestimo.getDataDevolucao(), emprestimo.getDataLimite());
            double valorBase = EmprestimoUtils.calcularValorBase(emprestimo.getDataEmprestimo(),
                    emprestimo.getDataDevolucao(), emprestimo.getDataLimite());
            double valorTotal = EmprestimoUtils.calcularValorTotal(valorBase, multa);
            return new EmprestimoResponseDTO(emprestimo, multa, valorBase, valorTotal);
        });

        return ResponseEntity.ok(emprestimosComValores);
    }

    @GetMapping("/{leitorId}/reservas")
    public ResponseEntity<Page<Reserva>> getReservasPorLeitorId(@PathVariable int leitorId, String search,
            @RequestParam(required = false) Integer livroId,
            StatusReserva status,
            Pageable pageable) {
        Page<Reserva> reservas = reservaRepository.findByLeitorIdAndSearch(leitorId, search, livroId, status, pageable);
        return ResponseEntity.ok(reservas);
    }

    @GetMapping("/buscar")
    public ResponseEntity<Page<Leitor>> buscarLeitores(
            @RequestParam String search, Pageable pageable) {
        Page<Leitor> leitores = leitorRepository.findByAllFields(search, pageable);
        return ResponseEntity.ok(leitores);
    }
}