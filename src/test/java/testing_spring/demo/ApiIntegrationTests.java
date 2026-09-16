package testing_spring.demo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import testing_spring.demo.book.BookRepository;

@SpringBootTest
@Transactional
class ApiIntegrationTests {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private BookRepository bookRepository;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void listsBooksWithPaginationMetadata() throws Exception {
        mvc.perform(get("/api/books").param("size", "2").param("sort", "title,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void emptySearchReturnsAnEmptyPage() throws Exception {
        mvc.perform(get("/api/books").param("keyword", "does-not-exist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void paginationUsesOffsetAndDescendingSort() throws Exception {
        mvc.perform(get("/api/books").param("page", "1").param("size", "2").param("sort", "title,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Clean Code"))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void searchReturnsMatchingPageAndCount() throws Exception {
        mvc.perform(get("/api/books").param("keyword", " SPRING ").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Spring in Action"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void rejectsUnknownSortProperty() throws Exception {
        mvc.perform(get("/api/books").param("sort", "unknown,asc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createsAndBorrowsABook() throws Exception {
        String bookJson = """
                {"title":"Domain-Driven Design","author":"Eric Evans",
                 "isbn":"9780321125217","publishedYear":2003}
                """;

        mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON).content(bookJson))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));

        Long id = bookRepository.findAll().stream()
                .filter(book -> book.getIsbn().equals("9780321125217"))
                .findFirst().orElseThrow().getId();

        mvc.perform(post("/api/books/{id}/borrow", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"borrowerName\":\"Ada Lovelace\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BORROWED"))
                .andExpect(jsonPath("$.borrowedBy").value("Ada Lovelace"))
                .andExpect(jsonPath("$.borrowedDate").exists());
    }

    @Test
    void validationErrorsUseAConsistentContract() throws Exception {
        mvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"isbn\":\"abc\",\"publishedYear\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.fieldErrors.title").exists())
                .andExpect(jsonPath("$.fieldErrors.author").exists())
                .andExpect(jsonPath("$.fieldErrors.isbn").exists())
                .andExpect(jsonPath("$.fieldErrors.publishedYear").exists());
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        Long id = bookRepository.findAll().getFirst().getId();
        mvc.perform(delete("/api/books/{id}", id))
                .andExpect(status().isNoContent());
    }
}
