package com.javadecode.booktrackerloader.Home;

import com.javadecode.booktrackerloader.user.BooksByUser;
import com.javadecode.booktrackerloader.user.BooksByUserRepository;
import com.javadecode.booktrackerloader.userBooks.UserBooksRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.query.CassandraPageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired
    BooksByUserRepository booksByUserRepository;

    private final String COVER_IMAGE_ROOT = "https://covers.openlibrary.org/b/id/";

    @GetMapping("/")
    public String home(@AuthenticationPrincipal OAuth2User principal, Model model){
        if(principal == null || principal.getAttribute("login") == null){
            return "index";
        }
        String userId = principal.getAttribute("login");
        Slice<BooksByUser> booksSlice = booksByUserRepository.findAllById(userId,
                CassandraPageRequest.of(0,10));
         List<BooksByUser> booksByUser = booksSlice.getContent();
        booksByUser = booksByUser.stream().map(book ->{
             String coverImageUrl = "/images/no-image-available.jpg";
             if(book.getCoverIds() !=null && book.getCoverIds().size()>0){
                 coverImageUrl = COVER_IMAGE_ROOT+book.getCoverIds().get(0)+"-M.jpg";
             }
             book.setCoverUrl(coverImageUrl);
             return book;
         }).collect(Collectors.toList());

         model.addAttribute("books",booksByUser);

        return "home";
    }
}
