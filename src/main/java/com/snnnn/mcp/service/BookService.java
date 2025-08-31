package com.snnnn.mcp.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * BookService - 图书服务
 *
 * @author zj
 * @since 2025/04/22 21:11
 */
@Service
@RestController
@RequestMapping("/book")
public class BookService {

    // 模拟图书数据库
    private final Map<String, Book> books = new HashMap<>();
    private final Map<String, List<Book>> authorBooks = new HashMap<>();

    public BookService() {
        // 初始化一些示例图书数据
        initializeBooks();
    }

    private void initializeBooks() {
        Book book1 = new Book("1", "Java编程思想", "Bruce Eckel", "计算机科学", 89.0, 4.5);
        Book book2 = new Book("2", "Spring实战", "Craig Walls", "计算机科学", 79.0, 4.3);
        Book book3 = new Book("3", "算法导论", "Thomas H. Cormen", "计算机科学", 128.0, 4.8);
        Book book4 = new Book("4", "设计模式", "Erich Gamma", "计算机科学", 59.0, 4.2);
        Book book5 = new Book("5", "深入理解计算机系统", "Randal E. Bryant", "计算机科学", 139.0, 4.7);
        
        books.put(book1.getId(), book1);
        books.put(book2.getId(), book2);
        books.put(book3.getId(), book3);
        books.put(book4.getId(), book4);
        books.put(book5.getId(), book5);

        // 按作者分组
        authorBooks.put("Bruce Eckel", Arrays.asList(book1));
        authorBooks.put("Craig Walls", Arrays.asList(book2));
        authorBooks.put("Thomas H. Cormen", Arrays.asList(book3));
        authorBooks.put("Erich Gamma", Arrays.asList(book4));
        authorBooks.put("Randal E. Bryant", Arrays.asList(book5));
    }

    @Tool(description = "根据图书ID查询图书详细信息")
    @RequestMapping(value = "/get", method = RequestMethod.GET, produces = "application/json")
    public Map<String, Object> getBookById(@ToolParam(description = "图书ID") @RequestParam String bookId) {
        Map<String, Object> response = new HashMap<>();
        Book book = books.get(bookId);
        
        if (book != null) {
            response.put("status", "success");
            response.put("data", book);
            response.put("message", "查询成功");
        } else {
            response.put("status", "error");
            response.put("message", "未找到ID为 " + bookId + " 的图书");
        }
        
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return response;
    }

    @Tool(description = "根据作者姓名查询该作者的所有图书")
    @RequestMapping(value = "/author", method = RequestMethod.GET, produces = "application/json")
    public Map<String, Object> getBooksByAuthor(@ToolParam(description = "作者姓名") @RequestParam String author) {
        Map<String, Object> response = new HashMap<>();
        List<Book> authorBookList = authorBooks.get(author);
        
        if (authorBookList != null && !authorBookList.isEmpty()) {
            response.put("status", "success");
            response.put("data", authorBookList);
            response.put("count", authorBookList.size());
            response.put("message", "查询成功");
        } else {
            response.put("status", "error");
            response.put("message", "未找到作者 " + author + " 的图书");
        }
        
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return response;
    }

    @Tool(description = "根据分类查询图书列表")
    @RequestMapping(value = "/category", method = RequestMethod.GET, produces = "application/json")
    public Map<String, Object> getBooksByCategory(@ToolParam(description = "图书分类") @RequestParam String category) {
        Map<String, Object> response = new HashMap<>();
        List<Book> categoryBooks = books.values().stream()
                .filter(book -> book.getCategory().equals(category))
                .toList();
        
        if (!categoryBooks.isEmpty()) {
            response.put("status", "success");
            response.put("data", categoryBooks);
            response.put("count", categoryBooks.size());
            response.put("message", "查询成功");
        } else {
            response.put("status", "error");
            response.put("message", "未找到分类为 " + category + " 的图书");
        }
        
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return response;
    }

    @Tool(description = "搜索图书（根据书名关键词）")
    @RequestMapping(value = "/search", method = RequestMethod.GET, produces = "application/json")
    public Map<String, Object> searchBooks(@ToolParam(description = "搜索关键词") @RequestParam String keyword) {
        Map<String, Object> response = new HashMap<>();
        List<Book> searchResults = books.values().stream()
                .filter(book -> book.getTitle().toLowerCase().contains(keyword.toLowerCase()))
                .toList();
        
        if (!searchResults.isEmpty()) {
            response.put("status", "success");
            response.put("data", searchResults);
            response.put("count", searchResults.size());
            response.put("keyword", keyword);
            response.put("message", "搜索成功");
        } else {
            response.put("status", "error");
            response.put("message", "未找到包含关键词 '" + keyword + "' 的图书");
        }
        
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return response;
    }

    @Tool(description = "获取图书推荐列表（按评分排序）")
    @RequestMapping(value = "/recommend", method = RequestMethod.GET, produces = "application/json")
    public Map<String, Object> getRecommendedBooks(@ToolParam(description = "推荐数量") @RequestParam(defaultValue = "3") int count) {
        Map<String, Object> response = new HashMap<>();
        List<Book> recommendedBooks = books.values().stream()
                .sorted((b1, b2) -> Double.compare(b2.getRating(), b1.getRating()))
                .limit(count)
                .toList();
        
        response.put("status", "success");
        response.put("data", recommendedBooks);
        response.put("count", recommendedBooks.size());
        response.put("message", "推荐图书（按评分排序）");
        
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return response;
    }

    @Tool(description = "获取所有图书列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET, produces = "application/json")
    public Map<String, Object> getAllBooks() {
        Map<String, Object> response = new HashMap<>();
        List<Book> allBooks = new ArrayList<>(books.values());
        
        response.put("status", "success");
        response.put("data", allBooks);
        response.put("count", allBooks.size());
        response.put("message", "查询成功");
        
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return response;
    }

    // 图书实体类
    public static class Book {
        private String id;
        private String title;
        private String author;
        private String category;
        private double price;
        private double rating;

        public Book(String id, String title, String author, String category, double price, double rating) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.category = category;
            this.price = price;
            this.rating = rating;
        }

        // Getters
        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getAuthor() { return author; }
        public String getCategory() { return category; }
        public double getPrice() { return price; }
        public double getRating() { return rating; }

        // Setters
        public void setId(String id) { this.id = id; }
        public void setTitle(String title) { this.title = title; }
        public void setAuthor(String author) { this.author = author; }
        public void setCategory(String category) { this.category = category; }
        public void setPrice(double price) { this.price = price; }
        public void setRating(double rating) { this.rating = rating; }
    }
}
