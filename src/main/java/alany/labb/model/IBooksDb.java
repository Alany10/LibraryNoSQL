package alany.labb.model;

import java.util.Date;
import java.util.List;

/**
 * This interface declares methods for querying a Books database.
 * Different implementations of this interface handles the connection and
 * queries to a specific DBMS and database, for example a MySQL or a MongoDB
 * database.
 *
 * NB! The methods in the implementation must catch the SQL/MongoDBExceptions thrown
 * by the underlying driver, wrap in a BooksDbException and then re-throw the latter
 * exception. This way the interface is the same for both implementations, because the
 * exception type in the method signatures is the same. More info in BooksDbException.java.
 *
 * @author anderslm@kth.se
 */
public interface IBooksDb {
    public boolean connect() throws BooksDbException;
    public void disconnect() throws BooksDbException;
    public List<Book> getBooks();
    public List<Author> getAuthors();
    public List<Book> searchBooksByTitle(String title) throws BooksDbException;
    public List<Book> searchBooksByISBN(String isbn) throws BooksDbException;
    public List<Book> searchBooksByAuthor(String name) throws BooksDbException;
    public List<Book> searchBooksByRating(String rating) throws BooksDbException;
    public List<Book> searchBooksByGenre(String genre) throws BooksDbException;
    public void rateBook(Book book, int rating);
    public void createBook(String title, String isbn, String genre, String rating);
    public void createAuthor(String firstName, String lastName, Date birthDay);
    public boolean createRelation(Book book, Author author);
    public void updateDb() throws BooksDbException;
}