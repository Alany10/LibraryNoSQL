package alany.labb.view;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import alany.labb.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class BooksPane extends VBox {
    private TableView<Book> booksTable;
    private ObservableList<Book> booksInTable; // the data backing the table view
    private ComboBox<SearchMode> searchModeBox;
    private TextField searchField;
    private Button searchButton;
    private MenuBar menuBar;

    public BooksPane(BooksDb booksDb) {
        final Controller controller = new Controller(booksDb, this);
        this.init(controller);
    }

    /**
     * Display a new set of books, e.g. from a database select, in the
     * booksTable table view.
     *
     * @param books the books to display
     */
    public void displayBooks(List<Book> books) {
        booksInTable.clear();
        booksInTable.addAll(books);
    }

    public Author chooseAuthor(List<Author> authors) {
        if (authors.isEmpty()) {
            // Handle the case where bookList is empty
            return null; // Or handle it as per your application's logic
        }

        ChoiceDialog<Author> dialog = new ChoiceDialog<>(authors.get(0), authors);
        dialog.setTitle("Choose an Author");
        dialog.setHeaderText(null);
        dialog.setContentText("Select an author:");

        Optional<Author> result = dialog.showAndWait();
        return result.orElse(null);
    }

    /**
     * Notify user on input error or exceptions.
     *
     * @param msg the message
     * @param type types: INFORMATION, WARNING et c.
     */
    protected void showAlertAndWait(String msg, Alert.AlertType type) {
        // types: INFORMATION, WARNING et c.
        Alert alert = new Alert(type, msg);
        alert.showAndWait();
    }

    private void init(Controller controller) {
        booksInTable = FXCollections.observableArrayList();

        // init views and event handlers
        initBooksTable();
        initSearchView(controller);
        initMenus(controller);

        FlowPane bottomPane = new FlowPane();
        bottomPane.setHgap(10);
        bottomPane.setPadding(new Insets(10, 10, 10, 10));
        bottomPane.getChildren().addAll(searchModeBox, searchField, searchButton);

        BorderPane mainPane = new BorderPane();
        mainPane.setCenter(booksTable);
        mainPane.setBottom(bottomPane);
        mainPane.setPadding(new Insets(10, 10, 10, 10));

        this.getChildren().addAll(menuBar, mainPane);
        VBox.setVgrow(mainPane, Priority.ALWAYS);
    }

    private void initBooksTable() {
        booksTable = new TableView<>();
        booksTable.setEditable(false); // don't allow user updates (yet)
        booksTable.setPlaceholder(new Label("No rows to display"));

        // define columns
        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        TableColumn<Book, String> isbnCol = new TableColumn<>("ISBN");
        TableColumn<Book, Date> publishedCol = new TableColumn<>("Published");
        TableColumn<Book, Genre> genreCol = new TableColumn<>("Genre");
        TableColumn<Book, Integer> ratingCol = new TableColumn<>("Rating");
        TableColumn<Book, Author> authorsCol = new TableColumn<>("Authors");
        booksTable.getColumns().addAll(titleCol, isbnCol, publishedCol, genreCol, ratingCol, authorsCol);
        // give title column some extra space
        titleCol.prefWidthProperty().bind(booksTable.widthProperty().multiply(0.2));
        isbnCol.prefWidthProperty().bind(booksTable.widthProperty().multiply(0.1));
        publishedCol.prefWidthProperty().bind(booksTable.widthProperty().multiply(0.1));
        authorsCol.prefWidthProperty().bind(booksTable.widthProperty().multiply(0.4));

        // define how to fill data for each cell,
        // get values from Book properties
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        isbnCol.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        publishedCol.setCellValueFactory(new PropertyValueFactory<>("published"));
        genreCol.setCellValueFactory(new PropertyValueFactory<>("Genre"));
        ratingCol.setCellValueFactory(new PropertyValueFactory<>("rating"));
        authorsCol.setCellValueFactory(new PropertyValueFactory<>("authors"));

        // associate the table view with the data
        booksTable.setItems(booksInTable);
    }

    private void initSearchView(Controller controller) {
        searchField = new TextField();
        searchField.setPromptText("Search for...");
        searchModeBox = new ComboBox<>();
        searchModeBox.getItems().addAll(SearchMode.values());
        searchModeBox.setValue(SearchMode.Title);
        searchButton = new Button("Search");

        // event handling (dispatch to controller)
        searchButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String searchFor = searchField.getText();
                SearchMode mode = searchModeBox.getValue();
                controller.onSearchSelected(searchFor, mode);
            }
        });
    }

    private void initMenus(Controller controller) {
        Menu manageMenu = new Menu("Manage");
        MenuItem addBookItem = new MenuItem("Add Book");
        MenuItem addAuthorItem = new MenuItem("Add Author");
        MenuItem addRelationItem = new MenuItem("Add Relation");
        MenuItem rateItem = new MenuItem("Rate");
        MenuItem updateItem = new MenuItem("Update");
        manageMenu.getItems().addAll(addBookItem, addAuthorItem, addRelationItem, rateItem, updateItem);

        addBookItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                controller.onAddBookSelected(); // Call the controller method for Update action
            }
        });

        addAuthorItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                controller.onAddAuthorSelected(); // Call the controller method for Update action
            }
        });

        addRelationItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                controller.onAddRelationSelected(); // Call the controller method for Update action
            }
        });

        rateItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                controller.onRateSelected(); // Call the controller method for Update action
            }
        });

        updateItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                controller.onUpdateSelected(); // Call the controller method for Update action
            }
        });

        menuBar = new MenuBar();
        menuBar.getMenus().addAll(manageMenu);
    }

    public String getSelectedGenre() {
        ComboBox<String> genreComboBox = new ComboBox<>();
        genreComboBox.getItems().addAll(
                "Fiction", "NonFiction", "ScienceFiction",
                "Mystery", "Fantasy", "Romance", "Thriller", "Comedy"
        );

        Alert genreAlert = new Alert(Alert.AlertType.INFORMATION);
        genreAlert.setTitle("Select Genre");
        genreAlert.setHeaderText(null);
        genreAlert.setContentText("Choose a genre:");
        genreAlert.getDialogPane().setContent(genreComboBox);
        genreAlert.showAndWait();

        return genreComboBox.getValue();
    }

    public String getSelectedRating() {
        ComboBox<String> ratingComboBox = new ComboBox<>();
        ratingComboBox.getItems().addAll("1", "2", "3", "4", "5");

        Alert ratingAlert = new Alert(Alert.AlertType.INFORMATION);
        ratingAlert.setTitle("Select Rating");
        ratingAlert.setHeaderText(null);
        ratingAlert.setContentText("Choose a rating:");
        ratingAlert.getDialogPane().setContent(ratingComboBox);
        ratingAlert.showAndWait();

        return ratingComboBox.getValue();
    }

    public Book chooseBook(List<Book> books) {
        if (books.isEmpty()) {
            // Handle the case where bookList is empty
            return null; // Or handle it as per your application's logic
        }

        ChoiceDialog<Book> dialog = new ChoiceDialog<>(books.get(0), books);
        dialog.setTitle("Choose a Book");
        dialog.setHeaderText(null);
        dialog.setContentText("Select a book:");

        Optional<Book> result = dialog.showAndWait();
        return result.orElse(null);
    }

    public String enterTitle() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Enter Title");
        dialog.setHeaderText(null);
        dialog.setContentText("Please enter the title:");

        Optional<String> result = dialog.showAndWait();
        return result.filter(title -> !title.trim().isEmpty()).orElse(null);
    }

    public String enterISBN() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Enter ISBN");
        dialog.setHeaderText(null);
        dialog.setContentText("Please enter the ISBN:");

        Optional<String> result = dialog.showAndWait();
        return result.filter(isbn -> !isbn.trim().isEmpty()).orElse(null);
    }

    public String selectGenre() {
        ComboBox<String> genreComboBox = new ComboBox<>();
        genreComboBox.getItems().addAll(
                "Fiction", "NonFiction", "ScienceFiction",
                "Mystery", "Fantasy", "Romance", "Thriller", "Comedy"
        );
        genreComboBox.setPromptText("Select Genre");

        Alert genreAlert = new Alert(Alert.AlertType.CONFIRMATION);
        genreAlert.setTitle("Choose Genre");
        genreAlert.setHeaderText(null);
        genreAlert.setContentText("Please select the genre:");
        genreAlert.getDialogPane().setContent(genreComboBox);

        Optional<ButtonType> result = genreAlert.showAndWait();
        return result.isPresent() ? genreComboBox.getValue() : null;
    }

    public String selectRating() {
        ComboBox<String> ratingComboBox = new ComboBox<>();
        ratingComboBox.getItems().addAll("1", "2", "3", "4", "5");
        ratingComboBox.setPromptText("Select Rating");

        Alert ratingAlert = new Alert(Alert.AlertType.CONFIRMATION);
        ratingAlert.setTitle("Choose Rating");
        ratingAlert.setHeaderText(null);
        ratingAlert.setContentText("Please select the rating:");
        ratingAlert.getDialogPane().setContent(ratingComboBox);

        Optional<ButtonType> result = ratingAlert.showAndWait();
        return result.isPresent() ? ratingComboBox.getValue() : null;
    }

    public String enterFirstName() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Enter First Name");
        dialog.setHeaderText(null);
        dialog.setContentText("Please enter the first name:");

        Optional<String> result = dialog.showAndWait();
        return result.filter(firstName -> !firstName.trim().isEmpty()).orElse(null);
    }

    public String enterLastName() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Enter Last Name");
        dialog.setHeaderText(null);
        dialog.setContentText("Please enter the last name:");

        Optional<String> result = dialog.showAndWait();
        return result.filter(lastName -> !lastName.trim().isEmpty()).orElse(null);
    }

    public LocalDate pickDateOfBirth() {
        DatePicker datePicker = new DatePicker();

        Dialog<LocalDate> dialog = new Dialog<>();
        dialog.setTitle("Pick Date of Birth");
        dialog.setHeaderText(null);
        dialog.getDialogPane().setContent(datePicker);

        ButtonType pickButton = new ButtonType("Pick", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(pickButton, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == pickButton) {
                return datePicker.getValue();
            }
            return null;
        });

        Optional<LocalDate> result = dialog.showAndWait();
        return result.orElse(null);
    }

    public void alreadyAssociated(String bookTitle, String authorName) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Already Associated");
        alert.setHeaderText(null);
        alert.setContentText("The book '" + bookTitle + "' is already associated with the author '" + authorName + "'.");
        alert.showAndWait();
    }
}