package com.spring.example.cashcard;


import com.spring.example.cashcard.exception.CashCardException;
import com.spring.example.cashcard.exception.CashCardNotFoundException;
import com.spring.example.cashcard.exception.CashCardNotOwnedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;


import java.net.URI;
import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("/"+ CashCardDictionary.CASHCARDS_RESOURCE)
public class CashCardController {

    private final CashCardService cashCardService;

    public CashCardController(CashCardService cashCardService){
        this.cashCardService = cashCardService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CashCard> findById(@PathVariable(name = "id") Long id,
                                             Principal principal){

        Optional<CashCard> byId = cashCardService.findByIdAndOwner(id, principal.getName());

        return byId.isPresent() ? ResponseEntity.ok(byId.get())
                : ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<Page<CashCard>> findAllByPage(Pageable pageable, Principal principal){
        return ResponseEntity.ok()
                .body(cashCardService.findAll(pageable, principal.getName()));
    }

    @PostMapping
    public ResponseEntity<CashCard> create(@RequestBody CashCard newCashCardReq,
                                           UriComponentsBuilder uriComponentsBuilder,
                                           Principal principal){

        try {
            CashCard newCashCardReqWithOwner = new CashCard(null, newCashCardReq.amount(), principal.getName());
            CashCard created = cashCardService.create(newCashCardReqWithOwner);

            if (created == null) throw new Exception("cannot create");

            URI locationOfNewCashCard = uriComponentsBuilder
                    .pathSegment(CashCardDictionary.CASHCARDS_RESOURCE, "{id}")
                    .buildAndExpand(created.id())
                    .toUri();

            return ResponseEntity
                    .created(locationOfNewCashCard)
                    .body(created);
        }
        catch (Exception ex){
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody CashCard toUpdate, Principal principal){

        try {
            cashCardService.update(id, toUpdate, principal.getName());
            return ResponseEntity.noContent().build();
        }
        catch (CashCardException exception){
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Principal principal){
        try {
            cashCardService.deleteByIdAndOwner(id, principal.getName());
            return ResponseEntity.noContent().build();
        }catch (CashCardNotFoundException cEx){
            return ResponseEntity.notFound().build();
        }
    }
}
