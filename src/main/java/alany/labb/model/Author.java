package alany.labb.model;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

public class Author {
    private String authorId;
    private final String firstName;
    private final String lastName;
    private final Date birthDay;
    private final List<Book> books;

    public Author(String id, String firstName, String lastName, Date birthDay) {
        this.authorId = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDay = birthDay;
        this.books = new ArrayList<>();
    }

    public Author(String id, String firstName, String lastName, Date birthDay, List<Book> books) {
        this.authorId = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDay = birthDay;
        this.books = new ArrayList<>();
        this.books.addAll(books);
    }

    public Author(String firstName, String lastName, Date birthDay) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDay = birthDay;
        this.books = new ArrayList<>();
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Date getBirthDay() {
        return birthDay;
    }

    public List<Book> getBooks() {
        return new ArrayList<>(books);
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public void addBook(Book book){
        for (Book b: books){
            if (b.getBookId().equals(book.getBookId())){
                return;
            }
        }
        books.add(book);
    }

    @Override
    public String toString() {
        return firstName + " " + lastName;
    }
}