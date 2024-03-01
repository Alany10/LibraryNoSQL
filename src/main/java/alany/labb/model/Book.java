package alany.labb.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Book {
    private String bookId;
    private final String isbn;
    private final String title;
    private final Date published;
    private final Genre genre;
    private int rating;
    private final List<Author> authors;

    public Book(String bookId, String isbn, String title, Date published, Genre genre, int rating, List<Author> authors) {
        this.bookId = bookId;
        this.isbn = isbn;
        this.title = title;
        this.published = published;
        this.genre = genre;
        this.rating = rating;
        this.authors = new ArrayList<>();
        this.authors.addAll(authors);
    }

    public Book(String isbn, String title, Date published, Genre genre, int rating) {
        this.isbn = isbn;
        this.title = title;
        this.published = published;
        this.genre = genre;
        this.rating = rating;
        this.authors = new ArrayList<>();
    }

    public Book(String bookId, String isbn, String title, Date published, Genre genre, int rating) {
        this.bookId = bookId;
        this.isbn = isbn;
        this.title = title;
        this.published = published;
        this.genre = genre;
        this.rating = rating;
        this.authors = new ArrayList<>();
    }

    public String getBookId() { return bookId; }
    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public Date getPublished() { return published; }
    public Genre getGenre() {
        return genre;
    }
    public int getRating() {
        return rating;
    }
    public void setRating(int rating) {
        this.rating = rating;
    }
    public List<Author> getAuthors() {
        return new ArrayList<>(authors);
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public void addAuthor(Author author){
        for (Author a: authors){
            if (a.getAuthorId().equals(author.getAuthorId())){
                return;
            }
        }
        authors.add(author);
    }

    @Override
    public String toString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = dateFormat.format(published);

        return title + ", " + isbn + ", " + formattedDate + ", " + genre + ", " + rating;
    }
}