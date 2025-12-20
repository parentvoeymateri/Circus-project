package com.teslya.circus;

import com.teslya.circus.controller.LoginController;
import com.teslya.circus.controller.MainController;
import com.teslya.circus.dao.UserDAO;
import com.teslya.circus.model.User;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    private Stage primaryStage;

    // Метод для автоматического создания админа, если его нет
    private void createAdminIfNotExists() {
        UserDAO userDAO = new UserDAO();
        if (userDAO.findByUsername("admin") == null) {
            User admin = new User("admin", "123", "ADMIN");
            userDAO.save(admin);
            System.out.println("Админ автоматически создан: логин 'admin', пароль '123'");
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        this.primaryStage = stage;

        // Создаём админа при первом запуске (если БД пустая)
        createAdminIfNotExists();

        // Загружаем экран логина
        FXMLLoader loginLoader = new FXMLLoader(MainApp.class.getResource("/login.fxml"));
        Scene loginScene = new Scene(loginLoader.load(), 700, 600);
        LoginController loginController = loginLoader.getController();
        loginController.setPrimaryStage(primaryStage);

        // Загружаем главный экран
        FXMLLoader mainLoader = new FXMLLoader(MainApp.class.getResource("/main.fxml"));
        Scene mainScene = new Scene(mainLoader.load(), 1100, 750);
        MainController mainController = mainLoader.getController();

        // Передаём stage и scene в MainController (для корректного logout)
        mainController.setPrimaryStage(primaryStage);
        mainController.setMainScene(mainScene);  // если используешь в logout для передачи дальше

        // Связываем контроллеры
        loginController.setMainScene(mainScene);
        loginController.setMainController(mainController);

        stage.setTitle("Информационно-справочная система цирка");
        stage.setScene(loginScene);
        stage.setResizable(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}