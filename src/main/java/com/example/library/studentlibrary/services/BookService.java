package com.example.library.studentlibrary.services;

import com.example.library.studentlibrary.models.Book;
import com.example.library.studentlibrary.models.Genre;
import com.example.library.studentlibrary.repositories.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {


    @Autowired
    BookRepository bookRepository2;

    public void createBook(Book book){
        bookRepository2.save(book);
    }

    public List<Book> getBooks(String genre, boolean available, String author){
        List<Book> books = null; //find the elements of the list by yourself
        if(genre.equals("")){
            books=bookRepository2.findBooksByGenre(genre,available);
            return books;
        }
        if(author.equals("")){
            books=bookRepository2.findBooksByAuthor(author,available);
            return books;
        }
        if(author.equals("") && genre.equals("")){
            books=bookRepository2.findByAvailability(available);
            return books;
        }
        return bookRepository2.findBooksByGenreAuthor(genre,author,available);
    }
}