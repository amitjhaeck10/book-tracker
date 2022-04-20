package com.javadecode.booktrackerloader;

import javax.annotation.PostConstruct;

import com.javadecode.booktrackerloader.authors.Author;
import com.javadecode.booktrackerloader.authors.AuthorRepository;

import com.javadecode.booktrackerloader.book.Book;
import com.javadecode.booktrackerloader.book.BookRepository;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class DataDumpLoader {

    @Autowired
    AuthorRepository authorRepository;

    @Autowired
    BookRepository bookRepository;

    @Value("${data.location.author}")
    private String authorDumpLocation;

    @Value("${data.location.work}")
    private String workDumpLocation;

    private final WebClient webClient;

    public DataDumpLoader(WebClient.Builder webClientBuilder){
        this.webClient = webClientBuilder.baseUrl("https://openlibrary.org/authors/").build();
    }


    @PostConstruct
    public void init() {
        System.out.println("**************Book tracker application started**************");
        //initAuthors();
        //initBook();
    }

    public void initAuthors(){
        Path path = Path.of(authorDumpLocation);
        try(Stream<String> lines = Files.lines(path)){
            lines.forEach(line->{
                String jsonString = line.substring(line.indexOf("{"));
                saveAuthor(jsonString);
            });
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public Author saveAuthor(String jsonString){
        if(jsonString == null || !StringUtils.hasText(jsonString)){
            return null;
        }
        Author author = new Author();
        try {
            System.out.println("----Author JsonString---:"+jsonString);
            JSONObject jsonObject = new JSONObject(jsonString);
            author.setName(jsonObject.optString("name"));
            author.setPersonalName(jsonObject.optString("personal_name"));
            author.setId(jsonObject.optString("key").replace("/authors/",""));

            author = authorRepository.save(author);
        } catch (JSONException e) {
            e.printStackTrace();
            return  null;
        }
        return author;
    }
    public void initBook(){
        Path path = Path.of(workDumpLocation);
        try(Stream<String> lines = Files.lines(path)){
            lines.forEach(line->{
                String jsonString = line.substring(line.indexOf("{"));
                 saveBook(jsonString);
            });
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public Book saveBook(String jsonString){
        if(jsonString == null || !StringUtils.hasText(jsonString)){
            return null;
        }
        Book book = book = new Book();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
        try {
            System.out.println("-----Book jsonString-----:"+jsonString);
            JSONObject jsonObject = new JSONObject(jsonString);

            book.setName(jsonObject.optString("title"));

            JSONObject descJsonObject = jsonObject.optJSONObject("description");
            if(descJsonObject !=null){
                book.setDescription(descJsonObject.optString("value"));
            }

            JSONObject publishedDateJson = jsonObject.optJSONObject("created");
            if(publishedDateJson !=null){
                String datetime = publishedDateJson.getString("value");
                book.setPublishedDate(LocalDate.parse(datetime,dateTimeFormatter));
            }

            JSONArray jsonArray = jsonObject.optJSONArray("covers");
            if(jsonArray !=null){
                List<String> coverList = new ArrayList<>();
                for(int i=0;i<jsonArray.length();i++){
                    coverList.add(jsonArray.getString(i));
                }
                book.setCoverId(coverList);
            }

            JSONArray authorArray = jsonObject.optJSONArray("authors");
            if(authorArray !=null){
                List<String> authorList = new ArrayList<>();
                for(int i=0;i<authorArray.length();i++){
                    authorList.add(authorArray.optJSONObject(i).optJSONObject("author").
                            optString("key").replace("/authors/",""));
                }
                book.setAuthorIds(authorList);
                List<String> authorNames = populateAuthor(authorList);
                book.setAuthorNames(authorNames);
            }
                book.setId(jsonObject.optString("key").replace("/works/",""));

            book = bookRepository.save(book);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return book;
    }

    private List<String> populateAuthor(List<String> authorList){
        String name = "Unknown Author";
        List<String> authorNames = new ArrayList<>();
        for(String id:authorList){
            Optional<Author> optionalAuthor = authorRepository.findById(id);
            if(!optionalAuthor.isPresent()) {
                Mono<String> monoString = webClient.get().uri("{id}.json",id).retrieve().bodyToMono(String.class);
                String jsonString = monoString.block();
                Author author = saveAuthor(jsonString);
                if(author == null ){
                    authorNames.add(name);
                }else{
                    authorNames.add(author.getName());
                }
            }else {
                authorNames.add(optionalAuthor.get().getName());
            }
        }
        return  authorNames;
    }

    public AuthorRepository getAuthorRepository() {
        return authorRepository;
    }

    public void setAuthorRepository(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    public String getAuthorDumpLocation() {
        return authorDumpLocation;
    }

    public void setAuthorDumpLocation(String authorDumpLocation) {
        this.authorDumpLocation = authorDumpLocation;
    }

    public String getWorkDumpLocation() {
        return workDumpLocation;
    }

    public void setWorkDumpLocation(String workDumpLocation) {
        this.workDumpLocation = workDumpLocation;
    }
}
