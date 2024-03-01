package alany.labb;

import alany.labb.model.BooksDb;
import alany.labb.model.BooksDbException;
import alany.labb.view.BooksPane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {

        BooksDb booksDb = new BooksDb(); // model
        try {
            if (!booksDb.connect()){
                System.out.println("No connection");
            };
        } catch (BooksDbException e) {}

        BooksPane root = new BooksPane(booksDb);

        Scene scene = new Scene(root, 800, 600);

        primaryStage.setTitle("Books Database Client");
        // add an exit handler to the stage (X) ?
        primaryStage.setOnCloseRequest(event -> {
            try {
                booksDb.disconnect();
            } catch (Exception e) {}
        });
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}