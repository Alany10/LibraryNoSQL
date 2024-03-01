package alany.labb.view;

import alany.labb.model.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static javafx.scene.control.Alert.AlertType.*;

public class Controller {
    private final BooksPane booksView; // view
    private final IBooksDb booksDb; // model
    public Controller(IBooksDb booksDb, BooksPane booksView) {
        this.booksDb = booksDb;
        this.booksView = booksView;
    }

    protected void onSearchSelected(String searchFor, SearchMode mode) {
        List<Book> result;
        try {
            if (mode == SearchMode.Genre){
                searchFor = booksView.getSelectedGenre();
                result = booksDb.searchBooksByGenre(searchFor);
            }
            else if(mode == SearchMode.Rating){
                searchFor = booksView.getSelectedRating();
                result = booksDb.searchBooksByRating(searchFor);
            }
            else {
                if (searchFor == null || searchFor.length() < 2){
                    booksView.showAlertAndWait("Enter a string.", INFORMATION);
                    return;
                }
                switch (mode) {
                    case Title:
                        result = booksDb.searchBooksByTitle(searchFor);
                        break;
                    case ISBN:
                        result = booksDb.searchBooksByISBN(searchFor);
                        break;
                    case Author:
                        result = booksDb.searchBooksByAuthor(searchFor);
                        break;
                    default:
                        result = new ArrayList<>();
                }
            }

            if (result == null || result.isEmpty()) {
                booksView.showAlertAndWait(
                        "No results found.", INFORMATION);
            } else {
                booksView.displayBooks(result);
            }

        } catch (Exception e) {
            booksView.showAlertAndWait("Database error.",ERROR);
        }
    }

    protected void onRateSelected(){
        Book book = booksView.chooseBook(booksDb.getBooks());
        if (book == null){
            return;
        }
        int rating = Integer.parseInt(booksView.getSelectedRating());
        booksDb.rateBook(book, rating);
    }

    protected void onAddBookSelected(){
        String title = booksView.enterTitle();
        String isbn = booksView.enterISBN();
        String genre = booksView.selectGenre();
        String rating = booksView.selectRating();

        booksDb.createBook(title, isbn, genre, rating);
    }

    protected void onAddAuthorSelected(){
        String firstName = booksView.enterFirstName();
        String lastName = booksView.enterLastName();

        // Assuming pickDateOfBirth() returns a LocalDate
        LocalDate localDate = booksView.pickDateOfBirth();

        // Convert LocalDate to Date
        Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        booksDb.createAuthor(firstName, lastName, date);
    }

    protected void onAddRelationSelected(){
        Book book = booksView.chooseBook(booksDb.getBooks());
        if (book == null){
            return;
        }
        Author author = booksView.chooseAuthor(booksDb.getAuthors());
        if (!booksDb.createRelation(book, author)){
            booksView.alreadyAssociated(book.getTitle(), author.getFirstName() + " " + author.getLastName());
        }
    }

    protected void onUpdateSelected() {
        new Thread(() -> {
            try {
                booksDb.updateDb();
            } catch (BooksDbException e) {
                throw new RuntimeException(e);
            }

            javafx.application.Platform.runLater(() -> {
                booksView.displayBooks(booksDb.getBooks());
            });
        }).start();
    }

}