package alany.labb.model;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

public class BooksDb implements IBooksDb {
    private final List<Book> books;
    private final List<Author> authors;

    public BooksDb() {
        this.books = new ArrayList<>();
        this.authors = new ArrayList<>();
    }

    @Override
    public List<Book> getBooks() {
        return new ArrayList<>(books);
    }

    @Override
    public List<Author> getAuthors() {
        return new ArrayList<>(authors);
    }

    @Override
    public boolean connect() throws BooksDbException {
        try {
            retrieveBooks();
            retrieveAuthors();

            return true;
        } catch (BooksDbException e) {
            throw new BooksDbException("Failed to connect to the database", e);
        }
    }

    @Override
    public void disconnect() throws BooksDbException {
        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017")) {
            MongoDatabase database = mongoClient.getDatabase("Library");

            updateBooks(database);
            updateAuthors(database);

        } catch (Exception e) {
            throw new BooksDbException("Failed to disconnect from the database", e);
        }
    }

    private void retrieveBooks() throws BooksDbException {
        try (MongoClient mongoClient = MongoClients.create(new ConnectionString("mongodb://localhost:27017"))) {
            MongoDatabase database = mongoClient.getDatabase("Library");
            MongoCollection<Document> collection = database.getCollection("Book");

            for (Document bookDoc : collection.find()) {
                ObjectId bookId = bookDoc.getObjectId("_id");
                String isbn = bookDoc.getString("isbn");
                String title = bookDoc.getString("title");
                Date published = bookDoc.getDate("published");
                Genre genre = Genre.valueOf(bookDoc.getString("genre"));
                int rating = bookDoc.getInteger("rating");

                List<Author> authors = retrieveAuthorsForBook(bookDoc.getList("authors", String.class), database);

                books.add(new Book(bookId.toString(), isbn, title, published, genre, rating, authors));
            }
        } catch (Exception e) {
            throw new BooksDbException("Failed to fetch books from the database", e);
        }
    }

    private List<Author> retrieveAuthorsForBook(List<String> authorIds, MongoDatabase database) {
        List<Author> authors = new ArrayList<>();

        if (authorIds != null && !authorIds.isEmpty()) {
            MongoCollection<Document> authorCollection = database.getCollection("Author");

            for (String authorId : authorIds) {
                Document authorDoc = authorCollection.find(new Document("_id", new ObjectId(authorId))).first();

                if (authorDoc != null) {
                    ObjectId retrievedAuthorId = authorDoc.getObjectId("_id");
                    String firstName = authorDoc.getString("firstName").toLowerCase();
                    String lastName = authorDoc.getString("lastName").toLowerCase();
                    Date birthDay = authorDoc.getDate("birthDay");

                    authors.add(new Author(retrievedAuthorId.toString(), firstName, lastName, birthDay));
                }
            }
        }

        return authors;
    }

    private void retrieveAuthors() throws BooksDbException {
        try (MongoClient mongoClient = MongoClients.create(new ConnectionString("mongodb://localhost:27017"))) {
            MongoDatabase database = mongoClient.getDatabase("Library");
            MongoCollection<Document> collection = database.getCollection("Author");

            for (Document authorDoc : collection.find()) {
                ObjectId authorId = authorDoc.getObjectId("_id");
                String firstName = authorDoc.getString("firstName").toLowerCase();
                String lastName = authorDoc.getString("lastName").toLowerCase();
                Date birthDay = authorDoc.getDate("birthDay");

                List<Book> books = retrieveBooksForAuthor(authorDoc.getList("books", String.class), database);

                authors.add(new Author(authorId.toString(), firstName, lastName, birthDay, books));
            }
        } catch (Exception e) {
            throw new BooksDbException("Failed to fetch authors from the database", e);
        }
    }

    private List<Book> retrieveBooksForAuthor(List<String> bookIds, MongoDatabase database) {
        List<Book> books = new ArrayList<>();

        if (bookIds != null && !bookIds.isEmpty()) {
            MongoCollection<Document> bookCollection = database.getCollection("Book");

            for (String bookId : bookIds) {
                Document bookDoc = bookCollection.find(new Document("_id", new ObjectId(bookId))).first();

                if (bookDoc != null) {
                    ObjectId retrievedBookId = bookDoc.getObjectId("_id");
                    String isbn = bookDoc.getString("isbn");
                    String title = bookDoc.getString("title");
                    Date published = bookDoc.getDate("published");
                    Genre genre = Genre.valueOf(bookDoc.getString("genre"));
                    int rating = bookDoc.getInteger("rating");

                    books.add(new Book(retrievedBookId.toString(), isbn, title, published, genre, rating));
                }
            }
        }

        return books;
    }


    private void updateBooks(MongoDatabase database) {
        MongoCollection<Document> bookCollection = database.getCollection("Book");

        for (Book book : books) {
            // Create a filter to find the existing document by bookId

            String bookId = book.getBookId();

            if (ObjectId.isValid(bookId)) {
                Bson filter = Filters.eq("_id", new ObjectId(bookId));

                List<String> authorIds = new ArrayList<>();
                for (Author author : book.getAuthors()) {
                    authorIds.add(author.getAuthorId());
                }

                // Create a document with the new values, including the authors field
                Document updatedDoc = new Document("isbn", book.getIsbn())
                        .append("title", book.getTitle())
                        .append("published", book.getPublished())
                        .append("genre", book.getGenre().toString())
                        .append("rating", book.getRating())
                        .append("authors", authorIds);

                // Update the existing document
                bookCollection.replaceOne(filter, updatedDoc);

            } else {
                List<String> authorIds = new ArrayList<>();
                for (Author author : book.getAuthors()) {
                    authorIds.add(author.getAuthorId());
                }

                // Create a document with the new values, including the authors field
                Document newDoc = new Document("isbn", book.getIsbn())
                        .append("title", book.getTitle())
                        .append("published", book.getPublished())
                        .append("genre", book.getGenre().toString())
                        .append("rating", book.getRating())
                        .append("authors", authorIds);

                // Update the existing document
                bookCollection.insertOne(newDoc);

                book.setBookId(newDoc.getObjectId("_id").toString());
            }
        }
    }

    private void updateAuthors(MongoDatabase database) {
        MongoCollection<Document> authorCollection = database.getCollection("Author");

        for (Author author : authors) {
            // Create a filter to find the existing document by authorId
            String authorId = author.getAuthorId();

            if (ObjectId.isValid(authorId)) {
                // If bookId is a valid ObjectId, create the filter
                Bson filter = Filters.eq("_id", new ObjectId(authorId));

                List<String> bookIds = new ArrayList<>();
                for (Book book : author.getBooks()) {
                    bookIds.add(book.getBookId());
                }

                // Check if bookIds list is empty and include an empty array if needed
                Document updatedDoc = new Document("firstName", author.getFirstName())
                        .append("lastName", author.getLastName())
                        .append("birthDay", author.getBirthDay())
                        .append("books", bookIds);
                authorCollection.replaceOne(filter, updatedDoc);
            } else {

                List<String> bookIds = new ArrayList<>();
                for (Book book : author.getBooks()) {
                    bookIds.add(book.getBookId());
                }

                Document newDoc = new Document("firstName", author.getFirstName())
                        .append("lastName", author.getLastName())
                        .append("birthDay", author.getBirthDay())
                        .append("books", bookIds);
                authorCollection.insertOne(newDoc);

                author.setAuthorId(newDoc.getObjectId("_id").toString());
            }
        }
    }


    @Override
    public List<Book> searchBooksByTitle(String searchTitle) throws BooksDbException {
        List<Book> result = new ArrayList<>();
        searchTitle = searchTitle.toLowerCase();
        for (Book book : books) {
            if (book.getTitle().toLowerCase().contains(searchTitle)) {
                result.add(book);
            }
        }
        return result;
    }

    @Override
    public List<Book> searchBooksByISBN(String isbn) throws BooksDbException {
        List<Book> result = new ArrayList<>();
        for (Book book : books) {
            if (book.getIsbn().contains(isbn)) {
                result.add(book);
            }
        }
        return result;
    }

    @Override
    public List<Book> searchBooksByAuthor(String name) throws BooksDbException {
        List<Book> result = new ArrayList<>();
        name = name.toLowerCase();
        for (Book book : books) {
            for (Author author: book.getAuthors()){
                if ((author.getFirstName() + " " + author.getLastName()).contains(name)) {
                    result.add(book);
                }
            }
        }
        return result;
    }

    @Override
    public List<Book> searchBooksByRating(String ratingStr) throws BooksDbException {
        List<Book> result = new ArrayList<>();
        int rating = Integer.parseInt(ratingStr);
        for (Book book : books) {
                if (book.getRating() == rating){
                    result.add(book);
            }
        }
        return result;
    }

    @Override
    public List<Book> searchBooksByGenre(String genreStr) throws BooksDbException {
        List<Book> result = new ArrayList<>();
        Genre genre = Genre.valueOf(genreStr);
        for (Book book : books) {
            if (book.getGenre() == genre){
                result.add(book);
            }
        }
        return result;
    }

    @Override
    public void rateBook(Book book, int rating) {
        for (Book b: books){
            if (b.getBookId().equals(book.getBookId())){
                b.setRating(rating);
                break;
            }
        }
    }

    @Override
    public void createBook(String title, String isbn, String genre, String rating){
        for (Book book: books){
            if (book.getIsbn().equals(isbn)){
                return;
            }
        }
        LocalDate localDate = LocalDate.now();

        // Convert LocalDate to Date
        Date utilDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Book book = new Book(
                highestBookId(),
                isbn, title,
                utilDate,
                Genre.valueOf(genre),
                Integer.parseInt(rating));
        books.add(book);
    }

    @Override
    public void createAuthor(String firstName, String lastName, Date birthDay){
        Author author = new Author(highestAuthorId(), firstName, lastName, birthDay);
        authors.add(author);
    }

    @Override
    public boolean createRelation(Book book, Author author){
        for (Book b: books){
            if (b.getBookId().equals(book.getBookId())){
                for (Author a: b.getAuthors()){
                    if (a.getAuthorId().equals(author.getAuthorId())){
                        return false;
                    }
                }
            }
        }
        book.addAuthor(author);
        author.addBook(book);
        return true;
    }

    @Override
    public void updateDb() throws BooksDbException {
        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017")) {
            MongoDatabase database = mongoClient.getDatabase("Library");

            // Clear existing relations by resetting arrays to empty in both collections
            MongoCollection<Document> bookCollection = database.getCollection("Book");
            MongoCollection<Document> authorCollection = database.getCollection("Author");

            bookCollection.updateMany(new Document(), Updates.set("authors", new ArrayList<>()));
            authorCollection.updateMany(new Document(), Updates.set("books", new ArrayList<>()));

            updateBooks(database);
            updateAuthors(database);

        } catch (Exception e) {
            throw new BooksDbException("Failed to update the database", e);
        }
    }

    private String highestBookId(){
        int highestId = 1;
        for (Book book: books){
            try {
                int bookId = Integer.parseInt(book.getBookId());
                if (bookId < 100 && bookId > highestId){
                    highestId = bookId;
                }
            } catch (NumberFormatException ignored) {
                // Handle the case where the bookId is not a valid integer
            }
        }
        return String.valueOf(highestId);
    }

    private String highestAuthorId(){
        int highestId = 1;
        for (Author author: authors){
            try {
                int authorId = Integer.parseInt(author.getAuthorId());
                if (authorId < 100 && authorId > highestId){
                    highestId = authorId;
                }
            } catch (NumberFormatException ignored) {
                // Handle the case where the authorId is not a valid integer
            }
        }
        return String.valueOf(highestId);
    }


}