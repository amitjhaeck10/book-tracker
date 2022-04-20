package com.javadecode.booktrackerloader.search;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class SearchController {

    private final String COVER_IMAGE_ROOT = "https://covers.openlibrary.org/b/id/";
    private WebClient webClient;

    public SearchController(WebClient.Builder webClientBuilder){
      this.webClient = webClientBuilder.exchangeStrategies(
                          ExchangeStrategies.builder().codecs(
                             clientCodecConfigurer -> clientCodecConfigurer.defaultCodecs().
                               maxInMemorySize(16*1024*1024)).build()
                         ).baseUrl("http://openlibrary.org/search.json").build();
    }

    @GetMapping(value = "/search")
    public String search(@RequestParam String query, Model model){
          Mono<SearchResult> searchResultMono = webClient.get().uri("?q={query}",query)
                .retrieve().bodyToMono(SearchResult.class);
         SearchResult searchResult = searchResultMono.block();
         List<SearchResultBook> books = searchResult.getDocs().
                 stream().limit(50).
                 map(bookResult ->{
                     bookResult.setKey(bookResult.getKey().replace("/works/",""));
                     String coverImageUrl = "/images/no-image-available.jpg";
                     if(StringUtils.hasText(bookResult.getCover_i())){
                         coverImageUrl = COVER_IMAGE_ROOT+bookResult.getCover_i()+"-L.jpg";
                     }
                     bookResult.setCover_i(coverImageUrl);
                     return bookResult;
                 }).
                 collect(Collectors.toList());

         model.addAttribute("searchResults",books);

        return "search";
    }
}
