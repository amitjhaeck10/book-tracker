package com.javadecode.booktrackerloader.book;

import com.javadecode.booktrackerloader.DataDumpLoader;
import com.javadecode.booktrackerloader.userBooks.UserBooks;
import com.javadecode.booktrackerloader.userBooks.UserBooksPrimaryKey;
import com.javadecode.booktrackerloader.userBooks.UserBooksRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Controller
public class BookController {

    @Autowired
    BookRepository bookRepository;

    @Autowired
    DataDumpLoader dataDumpLoader;

    @Autowired
    UserBooksRepository userBooksRepository;

    private final String COVER_IMAGE_ROOT = "https://covers.openlibrary.org/b/id/";
    private final String BOOK_LOOKUP_URL = "https://openlibrary.org/works/";

    private WebClient webClient;

    public BookController(WebClient.Builder webClientBuilder){
        this.webClient = webClientBuilder.baseUrl(BOOK_LOOKUP_URL).build();
    }

    @GetMapping(value = "/books/{bookId}")
    public String getBook(@PathVariable String bookId, Model model,
                          @AuthenticationPrincipal OAuth2User principal) {
       Optional<Book>  bookOptional = bookRepository.findById(bookId);
        Book book = null;
        String coverImageUrl = "/images/no-image-available.jpg";

        if(bookOptional.isPresent()) {
               book = bookOptional.get();
        }else{
               Mono<String> monoString = webClient.get().uri("{bookId}.json",bookId).retrieve().bodyToMono(String.class);
               String jsonString = monoString.block();
               book = dataDumpLoader.saveBook(jsonString);
        }

        if(book == null){
            return "book-not-found";
        }else{
            if(book.getCoverId() !=null && book.getCoverId().size()>0){
                coverImageUrl = COVER_IMAGE_ROOT+book.getCoverId().get(0)+"-M.jpg";
            }
            model.addAttribute("coverImageUrl",coverImageUrl);
            model.addAttribute("book", book);

            if(principal !=null && principal.getAttribute("login") !=null){
                model.addAttribute("loginId", principal.getAttribute("login"));

                UserBooksPrimaryKey key = new UserBooksPrimaryKey();
                key.setBookId(bookId);
                key.setUserId(principal.getAttribute("login"));

               Optional<UserBooks> optionalUserBooks = userBooksRepository.findById(key);
               UserBooks userBooks = new UserBooks();
               if(optionalUserBooks.isPresent()){
                   userBooks = optionalUserBooks.get();
               }
               model.addAttribute("userBooks",userBooks);

            }
            return "book";
        }
    }
}
