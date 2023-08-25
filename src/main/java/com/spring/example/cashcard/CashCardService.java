package com.spring.example.cashcard;

import com.spring.example.cashcard.exception.CashCardException;
import com.spring.example.cashcard.exception.CashCardNotFoundException;
import com.spring.example.cashcard.exception.CashCardNotOwnedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CashCardService {
    private final CashCardRepository cashCardRepository;

    public CashCardService(CashCardRepository cashCardRepository){
        this.cashCardRepository = cashCardRepository;
    }

    @Transactional(readOnly = true)
    public Optional<CashCard> findByIdAndOwner(Long id, String name) {
        return Optional.ofNullable(cashCardRepository.findByIdAndOwner(id, name));
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public CashCard create(CashCard newCashCardReq) throws CashCardException {

        try {
            return cashCardRepository.save(newCashCardReq);
        }catch (Exception ex){
            throw new CashCardException("could not be created");
        }
    }


    @Transactional(readOnly = true)
    public Page<CashCard> findAll(Pageable pageable, String owner) {

        PageRequest pageRequest = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSortOr(
                        Sort.by(Sort.Direction.ASC, "amount"))
        );
        return cashCardRepository.findAllByOwner(owner, pageRequest);
    }


    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public CashCard update(Long id, CashCard toUpdate, String owner) throws  CashCardNotOwnedException, CashCardNotFoundException

    {

        if (!cashCardRepository.findById(id).isPresent())
            throw new CashCardNotFoundException("id does not exist");

        CashCard existingCashCard = cashCardRepository.findByIdAndOwner(id, owner);
        if (cashCardRepository == null)
            throw new CashCardNotOwnedException("this cashcard is not owned by " + owner);

        toUpdate = new CashCard(existingCashCard.id(), toUpdate.amount(), owner);

        return cashCardRepository.save(toUpdate);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void deleteByIdAndOwner(Long id, String owner) throws CashCardNotFoundException
    {
        if (!cashCardRepository.existsByIdAndOwner(id, owner))
            throw new CashCardNotFoundException("not owned");

        cashCardRepository.deleteById(id);
    }
}
