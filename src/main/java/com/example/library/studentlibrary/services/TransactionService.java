package com.example.library.studentlibrary.services;

import com.example.library.studentlibrary.models.*;
import com.example.library.studentlibrary.repositories.BookRepository;
import com.example.library.studentlibrary.repositories.CardRepository;
import com.example.library.studentlibrary.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class TransactionService {

    @Autowired
    BookRepository bookRepository5;

    @Autowired
    CardRepository cardRepository5;

    @Autowired
    TransactionRepository transactionRepository5;

    @Value("${books.max_allowed}")
    int max_allowed_books;

    @Value("${books.max_allowed_days}")
    int getMax_allowed_days;

    @Value("${books.fine.per_day}")
    int fine_per_day;

    public String issueBook(int cardId, int bookId) throws Exception {
        //check whether bookId and cardId already exist
        //conditions required for successful transaction of issue book:
        Transaction transaction;
        //1. book is present and available
        if(!bookRepository5.existsById(bookId) || bookRepository5.findById(bookId).get().isAvailable()==false){
            throw new Exception("Book is either unavailable or not present");
        }
        // If it fails: throw new Exception("Book is either unavailable or not present");
        //2. card is present and activated
        else if(!cardRepository5.existsById(cardId) || cardRepository5.findById(cardId).get().getCardStatus()==CardStatus.DEACTIVATED){
            throw new Exception("Card is invalid");
        }
        // If it fails: throw new Exception("Card is invalid");
        //3. number of books issued against the card is strictly less than max_allowed_books
        else if(cardRepository5.findById(cardId).get().getBooks().size()>max_allowed_books){
            throw new Exception("Book limit has reached for this card");
        }
        // If it fails: throw new Exception("Book limit has reached for this card");
        //If the transaction is successful, save the transaction to the list of transactions and return the id
        else{
            bookRepository5.findById(bookId).get().setAvailable(false);
            cardRepository5.findById(cardId).get().getBooks().add(bookRepository5.findById(bookId).get());
            bookRepository5.updateBook(bookRepository5.findById(bookId).get());
            transaction=Transaction.builder().transactionDate(new Date()).book(bookRepository5.findById(bookId).get()).transactionId(UUID.randomUUID().toString()).transactionStatus(TransactionStatus.SUCCESSFUL).isIssueOperation(true).card(cardRepository5.findById(cardId).get()).fineAmount(0).build();
        }
        List<Transaction> transactions=bookRepository5.findById(bookId).get().getTransactions();
        transactions.add(transaction);
        //Note that the error message should match exactly in all cases

       return transaction.getTransactionId(); //return transactionId instead
    }

    public Transaction returnBook(int cardId, int bookId) throws Exception{

        List<Transaction> transactions = transactionRepository5.find(cardId, bookId,TransactionStatus.SUCCESSFUL, true);
        Transaction transaction = transactions.get(transactions.size() - 1);
        int fine=transaction.getFineAmount();
        Date date=new Date();
        Long days=date.getTime()-transaction.getTransactionDate().getTime();
        if(TimeUnit.DAYS.convert(days,TimeUnit.MILLISECONDS)>getMax_allowed_days){
            fine+=(int)(TimeUnit.DAYS.convert(days,TimeUnit.MILLISECONDS)-getMax_allowed_days)*getMax_allowed_days;
        }
        //for the given transaction calculate the fine amount considering the book has been returned exactly when this function is called
        bookRepository5.findById(bookId).get().setAvailable(true);
        //make the book available for other users
        //make a new transaction for return book which contains the fine amount as well

        Transaction returnBookTransaction  = Transaction.builder().transactionDate(new Date()).book(bookRepository5.findById(bookId).get()).transactionId(UUID.randomUUID().toString()).transactionStatus(TransactionStatus.SUCCESSFUL).isIssueOperation(false).card(cardRepository5.findById(cardId).get()).fineAmount(fine).build();
        return returnBookTransaction; //return the transaction after updating all details
    }
}