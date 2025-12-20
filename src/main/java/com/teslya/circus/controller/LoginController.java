package com.teslya.circus.controller;

import com.teslya.circus.dao.UserDAO;
import com.teslya.circus.model.User;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;  // Для выбора роли при регистрации

    private Stage primaryStage;
    private Scene mainScene;
    private MainController mainController;
    private UserDAO userDAO = new UserDAO();

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setMainScene(Scene mainScene) {
        this.mainScene = mainScene;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void login() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Ошибка", "Заполните логин и пароль.");
            return;
        }

        User user = userDAO.findByUsername(username);
        if (user == null || !userDAO.checkPassword(user, password)) {
            showAlert("Ошибка", "Неверный логин или пароль.");
            return;
        }

        mainController.setUser(user);

        primaryStage.setScene(mainScene);
        primaryStage.setTitle("Информационно-справочная система цирка — Главное меню");
    }

    @FXML
    private void register() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String selectedRole = roleComboBox.getValue();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Ошибка", "Заполните логин и пароль.");
            return;
        }

        if (selectedRole == null) {
            showAlert("Ошибка", "Выберите роль для регистрации.");
            return;
        }

        User existing = userDAO.findByUsername(username);
        if (existing != null) {
            showAlert("Ошибка", "Пользователь с таким логином уже существует.");
            return;
        }

        // Запрещаем выбирать роль ADMIN при регистрации
        if ("ADMIN".equals(selectedRole)) {
            showAlert("Ошибка", "Роль ADMIN недоступна для выбора.");
            return;
        }

        User newUser = new User(username, password, selectedRole);
        userDAO.save(newUser);
        showAlert("Успех",
                "Регистрация прошла успешно!\n" +
                        "Логин: " + username + "\n" +
                        "Роль: " + selectedRole + "\n" +
                        "Теперь войдите в систему.");

        // Очистка полей после успешной регистрации
        usernameField.clear();
        passwordField.clear();
        roleComboBox.setValue(null);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}