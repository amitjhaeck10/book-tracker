package com.javadecode.booktrackerloader.userBooks;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.javadecode.booktrackerloader.book.Book;
import com.javadecode.booktrackerloader.book.BookRepository;
import com.javadecode.booktrackerloader.user.BooksByUser;
import com.javadecode.booktrackerloader.user.BooksByUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Controller
public class UserBooksController {

    @Autowired
    UserBooksRepository userBooksRepository;

    @Autowired
    BooksByUserRepository booksByUserRepository;

    @Autowired
    BookRepository bookRepository;

    @PostMapping("/addUserBook")
    public ModelAndView getUserBooks(@AuthenticationPrincipal OAuth2User principal,
                               @RequestBody MultiValueMap<String,String> formData){

        if(principal == null || principal.getAttribute("login") ==null){
            return null;
        }

        UserBooks userBooks = new UserBooks();
        String userId = principal.getAttribute("login");

        UserBooksPrimaryKey key = new UserBooksPrimaryKey();
        key.setUserId(userId);
        String bookId = formData.getFirst("bookId");
        key.setBookId(bookId);

        userBooks.setKey(key);
        userBooks.setStartDate(LocalDate.parse(formData.getFirst("startDate")));
        userBooks.setCompletedDate(LocalDate.parse(formData.getFirst("completedDate")));

        int rating = Integer.parseInt(formData.getFirst("rating"));
        userBooks.setRating(rating);
        userBooks.setReadingStatus(formData.getFirst("readingStatus"));
        userBooks.setFeedback(formData.getFirst("feedback"));

        userBooksRepository.save(userBooks);

         Optional<Book> optionalBook = bookRepository.findById(bookId);
         if(!optionalBook.isPresent()){
             return new ModelAndView("/redirect");
         }

         Book book = optionalBook.get();

        BooksByUser booksByUser = new BooksByUser();
        booksByUser.setId(userId);
        booksByUser.setBookId(bookId);
        booksByUser.setBookName(book.getName());
        booksByUser.setCoverIds(book.getCoverId());
        booksByUser.setAuthorNames(book.getAuthorNames());
        booksByUser.setReadingStatus(formData.getFirst("readingStatus"));
        booksByUser.setRating(rating);
        booksByUser.setTimeUuid(Uuids.timeBased());

        booksByUserRepository.save(booksByUser);

        return new ModelAndView("redirect:/books/"+bookId);
    }
}
