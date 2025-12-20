package com.teslya.circus.controller;

import com.teslya.circus.dao.*;
import com.teslya.circus.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.scene.control.Alert.AlertType;
import javafx.util.Callback;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public class MainController {

    @FXML private TabPane tabPane;
    @FXML private Menu statsMenu;
    @FXML private Menu adminMenu;

    private User currentUser;
    private PerformerDAO performerDAO = new PerformerDAO();
    private ShowDAO showDAO = new ShowDAO();
    private UserDAO userDAO = new UserDAO();
    private TicketDAO ticketDAO = new TicketDAO();
    private ScheduleDAO scheduleDAO = new ScheduleDAO();
    private Stage primaryStage;
    private Scene mainScene;

    public void setMainScene(Scene mainScene) {
        this.mainScene = mainScene;
    }
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    public void setUser(User user) {
        this.currentUser = user;
        String role = user.getRole();
        statsMenu.setVisible("ADMIN".equals(role) || "MANAGER".equals(role));
        adminMenu.setVisible("ADMIN".equals(role));
    }
    private void deleteUser(TableView<User> table) {
        User selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Ошибка", "Выберите пользователя для удаления.");
            return;
        }

        // Запрещаем удалять самого себя (чтобы админ не заблокировал себя)
        if (selected.getId().equals(currentUser.getId())) {
            showAlert("Ошибка", "Нельзя удалить самого себя.");
            return;
        }

        // Запрещаем удалять другого админа (опционально, но рекомендуется)
        if ("ADMIN".equals(selected.getRole())) {
            showAlert("Ошибка", "Нельзя удалить другого администратора.");
            return;
        }

        // Подтверждение удаления
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение");
        confirm.setHeaderText(null);
        confirm.setContentText("Вы уверены, что хотите удалить пользователя \"" + selected.getUsername() + "\"?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                userDAO.delete(selected.getId());  // Добавим метод delete в UserDAO ниже
                table.setItems(FXCollections.observableArrayList(userDAO.findAll()));
                showAlert("Успех", "Пользователь успешно удалён.");
            }
        });
    }
    private TableView<Performer> createPerformerTable() {
        TableView<Performer> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Performer, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Performer, String> nameCol = new TableColumn<>("Имя");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Performer, String> specialtyCol = new TableColumn<>("Специальность");
        specialtyCol.setCellValueFactory(new PropertyValueFactory<>("specialty"));

        TableColumn<Performer, Integer> experienceCol = new TableColumn<>("Опыт");
        experienceCol.setCellValueFactory(new PropertyValueFactory<>("experience"));

        table.getColumns().addAll(idCol, nameCol, specialtyCol, experienceCol);
        table.setItems(FXCollections.observableArrayList(performerDAO.findAll()));

        return table;
    }

    private TableView<Show> createShowTable() {
        TableView<Show> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Show, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Show, String> nameCol = new TableColumn<>("Название");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Show, Date> dateCol = new TableColumn<>("Дата");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<Show, Integer> durationCol = new TableColumn<>("Продолжительность");
        durationCol.setCellValueFactory(new PropertyValueFactory<>("duration"));

        table.getColumns().addAll(idCol, nameCol, dateCol, durationCol);
        table.setItems(FXCollections.observableArrayList(showDAO.findAll()));

        return table;
    }

    private TableView<Ticket> createTicketTable() {
        TableView<Ticket> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Ticket, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Ticket, String> showCol = new TableColumn<>("Представление");
        showCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getShow().getName()));

        TableColumn<Ticket, String> userCol = new TableColumn<>("Пользователь");
        userCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getUser().getUsername()));

        TableColumn<Ticket, String> seatCol = new TableColumn<>("Место");
        seatCol.setCellValueFactory(new PropertyValueFactory<>("seat"));

        TableColumn<Ticket, Double> priceCol = new TableColumn<>("Цена");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<Ticket, String> statusCol = new TableColumn<>("Статус");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(idCol, showCol, userCol, seatCol, priceCol, statusCol);

        return table;
    }

    private TableView<Schedule> createScheduleTable() {
        TableView<Schedule> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Schedule, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Schedule, String> performerCol = new TableColumn<>("Артист");
        performerCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getPerformer().getName()));

        TableColumn<Schedule, String> showCol = new TableColumn<>("Представление");
        showCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getShow().getName()));

        TableColumn<Schedule, String> roleCol = new TableColumn<>("Роль в шоу");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("roleInShow"));

        table.getColumns().addAll(idCol, performerCol, showCol, roleCol);

        return table;
    }

    private TableView<User> createUserTable() {
        TableView<User> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<User, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<User, String> usernameCol = new TableColumn<>("Логин");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<User, String> roleCol = new TableColumn<>("Роль");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        table.getColumns().addAll(idCol, usernameCol, roleCol);
        table.setItems(FXCollections.observableArrayList(userDAO.findAll()));

        return table;
    }

    @FXML
    private void showPerformers() {
        Tab tab = new Tab("Артисты");
        TableView<Performer> table = createPerformerTable();

        TextField searchField = new TextField();
        searchField.setPromptText("Поиск по имени или специальности...");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                table.setItems(FXCollections.observableArrayList(performerDAO.findAll()));
            } else {
                table.setItems(FXCollections.observableArrayList(performerDAO.search(newVal)));
            }
        });

        Button sortNameBtn = new Button("Сортировка по имени");
        sortNameBtn.setOnAction(e -> table.setItems(FXCollections.observableArrayList(performerDAO.sortByName())));

        Button sortExpBtn = new Button("Сортировка по опыту (убыв.)");
        sortExpBtn.setOnAction(e -> table.setItems(FXCollections.observableArrayList(performerDAO.sortByExperienceDesc())));

        HBox controls = new HBox(10, searchField, sortNameBtn, sortExpBtn);

        String role = currentUser.getRole();
        if ("ADMIN".equals(role) || "MANAGER".equals(role) || "ARTIST".equals(role)) {
            Button addBtn = new Button("Добавить");
            addBtn.setOnAction(e -> addPerformer(table));

            Button editBtn = new Button("Редактировать");
            editBtn.setOnAction(e -> editPerformer(table));

            Button deleteBtn = new Button("Удалить");
            deleteBtn.setOnAction(e -> deletePerformer(table));

            controls.getChildren().addAll(addBtn, editBtn, deleteBtn);
        }

        VBox vbox = new VBox(10, controls, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        tab.setContent(vbox);
        addTab(tab);
    }

    private void addPerformer(TableView<Performer> table) {
        Dialog<Performer> dialog = new Dialog<>();
        dialog.setTitle("Добавить артиста");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nameField = new TextField();
        TextField specialtyField = new TextField();
        TextField experienceField = new TextField();

        grid.add(new Label("Имя:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Специальность:"), 0, 1);
        grid.add(specialtyField, 1, 1);
        grid.add(new Label("Опыт (лет):"), 0, 2);
        grid.add(experienceField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    int experience = Integer.parseInt(experienceField.getText());
                    if (experience < 0) {
                        showAlert("Ошибка", "Опыт артиста не может быть отрицательным.");
                        return null;
                    }
                    Performer performer = new Performer(
                            nameField.getText().trim(),
                            specialtyField.getText().trim(),
                            experience
                    );
                    if (performer.getName().isEmpty() || performer.getSpecialty().isEmpty()) {
                        showAlert("Ошибка", "Заполните все поля.");
                        return null;
                    }
                    performerDAO.save(performer);
                    table.setItems(FXCollections.observableArrayList(performerDAO.findAll()));
                    return performer;
                } catch (NumberFormatException ex) {
                    showAlert("Ошибка", "Опыт должен быть целым положительным числом.");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void editPerformer(TableView<Performer> table) {
        Performer selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Ошибка", "Выберите артиста.");
            return;
        }

        Dialog<Performer> dialog = new Dialog<>();
        dialog.setTitle("Редактировать артиста");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nameField = new TextField(selected.getName());
        TextField specialtyField = new TextField(selected.getSpecialty());
        TextField experienceField = new TextField(String.valueOf(selected.getExperience()));

        grid.add(new Label("Имя:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Специальность:"), 0, 1);
        grid.add(specialtyField, 1, 1);
        grid.add(new Label("Опыт (лет):"), 0, 2);
        grid.add(experienceField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    int experience = Integer.parseInt(experienceField.getText());
                    if (experience < 0) {
                        showAlert("Ошибка", "Опыт артиста не может быть отрицательным.");
                        return null;
                    }
                    selected.setName(nameField.getText().trim());
                    selected.setSpecialty(specialtyField.getText().trim());
                    selected.setExperience(experience);

                    if (selected.getName().isEmpty() || selected.getSpecialty().isEmpty()) {
                        showAlert("Ошибка", "Заполните все поля.");
                        return null;
                    }
                    performerDAO.update(selected);
                    table.refresh();
                    return selected;
                } catch (NumberFormatException ex) {
                    showAlert("Ошибка", "Опыт должен быть целым положительным числом.");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void deletePerformer(TableView<Performer> table) {
        Performer selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Ошибка", "Выберите артиста.");
            return;
        }

        performerDAO.delete(selected.getId());
        table.setItems(FXCollections.observableArrayList(performerDAO.findAll()));
    }

    @FXML
    private void showShows() {
        Tab tab = new Tab("Представления");
        TableView<Show> table = createShowTable();

        TextField searchField = new TextField();
        searchField.setPromptText("Поиск по названию...");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                table.setItems(FXCollections.observableArrayList(showDAO.findAll()));
            } else {
                table.setItems(FXCollections.observableArrayList(showDAO.search(newVal)));
            }
        });

        Button sortDateBtn = new Button("Сортировка по дате");
        sortDateBtn.setOnAction(e -> table.setItems(FXCollections.observableArrayList(showDAO.sortByDate())));

        Button sortDurationBtn = new Button("Сортировка по продолжительности (убыв.)");
        sortDurationBtn.setOnAction(e -> table.setItems(FXCollections.observableArrayList(showDAO.sortByDurationDesc())));

        HBox controls = new HBox(10, searchField, sortDateBtn, sortDurationBtn);

        String role = currentUser.getRole();
        if ("ADMIN".equals(role) || "MANAGER".equals(role)) {
            Button addBtn = new Button("Добавить");
            addBtn.setOnAction(e -> addShow(table));

            Button editBtn = new Button("Редактировать");
            editBtn.setOnAction(e -> editShow(table));

            Button deleteBtn = new Button("Удалить");
            deleteBtn.setOnAction(e -> deleteShow(table));

            controls.getChildren().addAll(addBtn, editBtn, deleteBtn);
        }

        VBox vbox = new VBox(10, controls, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        tab.setContent(vbox);
        addTab(tab);
    }

    private void addShow(TableView<Show> table) {
        Dialog<Show> dialog = new Dialog<>();
        dialog.setTitle("Добавить представление");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nameField = new TextField();
        DatePicker datePicker = new DatePicker();
        TextField durationField = new TextField();

        grid.add(new Label("Название:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Дата:"), 0, 1);
        grid.add(datePicker, 1, 1);
        grid.add(new Label("Продолжительность (мин):"), 0, 2);
        grid.add(durationField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    LocalDate localDate = datePicker.getValue();
                    if (localDate == null) throw new Exception();
                    int duration = Integer.parseInt(durationField.getText());
                    if (duration <= 0) {
                        showAlert("Ошибка", "Продолжительность должна быть положительной.");
                        return null;
                    }
                    Show newShow = new Show(
                            nameField.getText().trim(),
                            Date.valueOf(localDate),
                            duration
                    );
                    if (newShow.getName().isEmpty()) {
                        showAlert("Ошибка", "Заполните все поля.");
                        return null;
                    }

                    if (showDAO.hasOverlap(newShow)) {
                        showAlert("Ошибка", "В этот день уже запланировано другое представление.");
                        return null;
                    }

                    showDAO.save(newShow);
                    table.setItems(FXCollections.observableArrayList(showDAO.findAll()));
                    return newShow;
                } catch (NumberFormatException ex) {
                    showAlert("Ошибка", "Продолжительность должна быть числом.");
                    return null;
                } catch (Exception ex) {
                    showAlert("Ошибка", "Проверьте введенные данные.");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void editShow(TableView<Show> table) {
        Show selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Ошибка", "Выберите представление.");
            return;
        }

        Dialog<Show> dialog = new Dialog<>();
        dialog.setTitle("Редактировать представление");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nameField = new TextField(selected.getName());
        DatePicker datePicker = new DatePicker(selected.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        TextField durationField = new TextField(String.valueOf(selected.getDuration()));

        grid.add(new Label("Название:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Дата:"), 0, 1);
        grid.add(datePicker, 1, 1);
        grid.add(new Label("Продолжительность (мин):"), 0, 2);
        grid.add(durationField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    LocalDate localDate = datePicker.getValue();
                    if (localDate == null) throw new Exception();
                    int duration = Integer.parseInt(durationField.getText());
                    if (duration <= 0) {
                        showAlert("Ошибка", "Продолжительность должна быть положительной.");
                        return null;
                    }
                    Show newShow = new Show(
                            nameField.getText().trim(),
                            Date.valueOf(localDate),
                            duration
                    );
                    if (newShow.getName().isEmpty()) {
                        showAlert("Ошибка", "Заполните все поля.");
                        return null;
                    }

                    if (showDAO.hasOverlap(newShow)) {
                        showAlert("Ошибка", "В этот день уже запланировано другое представление.");
                        return null;
                    }

                    showDAO.save(newShow);
                    table.setItems(FXCollections.observableArrayList(showDAO.findAll()));
                    return newShow;
                } catch (NumberFormatException ex) {
                    showAlert("Ошибка", "Продолжительность должна быть числом.");
                    return null;
                } catch (Exception ex) {
                    showAlert("Ошибка", "Проверьте введенные данные.");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void deleteShow(TableView<Show> table) {
        Show selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Ошибка", "Выберите представление.");
            return;
        }

        showDAO.delete(selected.getId());
        table.setItems(FXCollections.observableArrayList(showDAO.findAll()));
    }

    @FXML
    private void showTickets() {
        Tab tab = new Tab("Билеты");
        TableView<Ticket> table = createTicketTable();

        HBox controls = new HBox(10);

        String role = currentUser.getRole();
        if ("VIEWER".equals(role)) {
            table.setItems(FXCollections.observableArrayList(ticketDAO.findByUser(currentUser)));
            Button buyBtn = new Button("Купить билет");
            buyBtn.setOnAction(e -> buyTicket(table));
            Button cancelBtn = new Button("Отменить билет");
            cancelBtn.setOnAction(e -> cancelTicket(table));
            controls.getChildren().addAll(buyBtn, cancelBtn);
        } else if ("ADMIN".equals(role) || "MANAGER".equals(role)) {
            table.setItems(FXCollections.observableArrayList(ticketDAO.findAll()));
            Button editBtn = new Button("Редактировать");
            editBtn.setOnAction(e -> editTicket(table));
            Button deleteBtn = new Button("Удалить");
            deleteBtn.setOnAction(e -> deleteTicket(table));
            controls.getChildren().addAll(editBtn, deleteBtn);
        }

        VBox vbox = new VBox(10, controls, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        tab.setContent(vbox);
        addTab(tab);
    }

    private void buyTicket(TableView<Ticket> table) {
        Dialog<Ticket> dialog = new Dialog<>();
        dialog.setTitle("Купить билет");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        ComboBox<Show> showCombo = new ComboBox<>(FXCollections.observableArrayList(showDAO.findAll()));
        showCombo.setConverter(new StringConverter<Show>() {
            @Override
            public String toString(Show show) {
                return show != null ? show.getName() : "";
            }

            @Override
            public Show fromString(String string) {
                return null;
            }
        });

        TextField seatField = new TextField();
        TextField priceField = new TextField();

        grid.add(new Label("Представление:"), 0, 0);
        grid.add(showCombo, 1, 0);
        grid.add(new Label("Место:"), 0, 1);
        grid.add(seatField, 1, 1);
        grid.add(new Label("Цена:"), 0, 2);
        grid.add(priceField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    Show selectedShow = showCombo.getValue();
                    if (selectedShow == null) throw new Exception();
                    Ticket ticket = new Ticket(
                            selectedShow,
                            currentUser,
                            seatField.getText(),
                            Double.parseDouble(priceField.getText()),
                            "BOOKED"
                    );
                    ticketDAO.save(ticket);
                    table.setItems(FXCollections.observableArrayList(ticketDAO.findByUser(currentUser)));
                    return ticket;
                } catch (Exception ex) {
                    showAlert("Ошибка", "Проверьте введенные данные.");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void cancelTicket(TableView<Ticket> table) {
        Ticket selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Ошибка", "Выберите билет.");
            return;
        }

        selected.setStatus("CANCELLED");
        ticketDAO.update(selected);
        table.refresh();
    }

    private void editTicket(TableView<Ticket> table) {
        Ticket selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Ошибка", "Выберите билет.");
            return;
        }

        Dialog<Ticket> dialog = new Dialog<>();
        dialog.setTitle("Редактировать билет");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField seatField = new TextField(selected.getSeat());
        TextField priceField = new TextField(String.valueOf(selected.getPrice()));
        ComboBox<String> statusCombo = new ComboBox<>(FXCollections.observableArrayList("BOOKED", "CANCELLED"));
        statusCombo.setValue(selected.getStatus());

        grid.add(new Label("Место:"), 0, 0);
        grid.add(seatField, 1, 0);
        grid.add(new Label("Цена:"), 0, 1);
        grid.add(priceField, 1, 1);
        grid.add(new Label("Статус:"), 0, 2);
        grid.add(statusCombo, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    selected.setSeat(seatField.getText());
                    selected.setPrice(Double.parseDouble(priceField.getText()));
                    selected.setStatus(statusCombo.getValue());
                    ticketDAO.update(selected);
                    table.refresh();
                    return selected;
                } catch (Exception ex) {
                    showAlert("Ошибка", "Проверьте введенные данные.");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void deleteTicket(TableView<Ticket> table) {
        Ticket selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Ошибка", "Выберите билет.");
            return;
        }

        ticketDAO.delete(selected.getId());
        table.setItems(FXCollections.observableArrayList(ticketDAO.findAll()));
    }

    @FXML
    private void showSchedules() {
        Tab tab = new Tab("Расписание");
        TableView<Schedule> table = createScheduleTable();

        HBox controls = new HBox(10);

        String role = currentUser.getRole();
        if ("ARTIST".equals(role)) {
            // Предполагаем, что артист связан с пользователем (для простоты, ищем по имени или добавь связь)
            // Для примера, все расписания
            table.setItems(FXCollections.observableArrayList(scheduleDAO.findAll())); // Адаптируй под конкретного артиста
        } else if ("ADMIN".equals(role) || "MANAGER".equals(role)) {
            table.setItems(FXCollections.observableArrayList(scheduleDAO.findAll()));
            Button addBtn = new Button("Добавить");
            addBtn.setOnAction(e -> addSchedule(table));

            Button editBtn = new Button("Редактировать");
            editBtn.setOnAction(e -> editSchedule(table));

            Button deleteBtn = new Button("Удалить");
            deleteBtn.setOnAction(e -> deleteSchedule(table));

            controls.getChildren().addAll(addBtn, editBtn, deleteBtn);
        }

        VBox vbox = new VBox(10, controls, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        tab.setContent(vbox);
        addTab(tab);
    }

    private void addSchedule(TableView<Schedule> table) {
        Dialog<Schedule> dialog = new Dialog<>();
        dialog.setTitle("Добавить запись в расписание");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        ComboBox<Performer> performerCombo = new ComboBox<>(FXCollections.observableArrayList(performerDAO.findAll()));
        performerCombo.setConverter(new StringConverter<Performer>() {
            @Override
            public String toString(Performer performer) {
                return performer != null ? performer.getName() : "";
            }

            @Override
            public Performer fromString(String string) {
                return null;
            }
        });

        ComboBox<Show> showCombo = new ComboBox<>(FXCollections.observableArrayList(showDAO.findAll()));
        showCombo.setConverter(new StringConverter<Show>() {
            @Override
            public String toString(Show show) {
                return show != null ? show.getName() : "";
            }

            @Override
            public Show fromString(String string) {
                return null;
            }
        });

        TextField roleField = new TextField();

        grid.add(new Label("Артист:"), 0, 0);
        grid.add(performerCombo, 1, 0);
        grid.add(new Label("Представление:"), 0, 1);
        grid.add(showCombo, 1, 1);
        grid.add(new Label("Роль:"), 0, 2);
        grid.add(roleField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    Performer selectedPerformer = performerCombo.getValue();
                    Show selectedShow = showCombo.getValue();
                    if (selectedPerformer == null || selectedShow == null) throw new Exception();
                    Schedule schedule = new Schedule(
                            selectedPerformer,
                            selectedShow,
                            roleField.getText()
                    );
                    scheduleDAO.save(schedule);
                    table.setItems(FXCollections.observableArrayList(scheduleDAO.findAll()));
                    return schedule;
                } catch (Exception ex) {
                    showAlert("Ошибка", "Проверьте введенные данные.");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void editSchedule(TableView<Schedule> table) {
        Schedule selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Ошибка", "Выберите запись.");
            return;
        }

        Dialog<Schedule> dialog = new Dialog<>();
        dialog.setTitle("Редактировать запись в расписание");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        ComboBox<Performer> performerCombo = new ComboBox<>(FXCollections.observableArrayList(performerDAO.findAll()));
        performerCombo.setValue(selected.getPerformer());
        performerCombo.setConverter(new StringConverter<Performer>() {
            @Override
            public String toString(Performer performer) {
                return performer != null ? performer.getName() : "";
            }

            @Override
            public Performer fromString(String string) {
                return null;
            }
        });

        ComboBox<Show> showCombo = new ComboBox<>(FXCollections.observableArrayList(showDAO.findAll()));
        showCombo.setValue(selected.getShow());
        showCombo.setConverter(new StringConverter<Show>() {
            @Override
            public String toString(Show show) {
                return show != null ? show.getName() : "";
            }

            @Override
            public Show fromString(String string) {
                return null;
            }
        });

        TextField roleField = new TextField(selected.getRoleInShow());

        grid.add(new Label("Артист:"), 0, 0);
        grid.add(performerCombo, 1, 0);
        grid.add(new Label("Представление:"), 0, 1);
        grid.add(showCombo, 1, 1);
        grid.add(new Label("Роль:"), 0, 2);
        grid.add(roleField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    Performer selectedPerformer = performerCombo.getValue();
                    Show selectedShow = showCombo.getValue();
                    if (selectedPerformer == null || selectedShow == null) throw new Exception();
                    selected.setPerformer(selectedPerformer);
                    selected.setShow(selectedShow);
                    selected.setRoleInShow(roleField.getText());
                    scheduleDAO.update(selected);
                    table.refresh();
                    return selected;
                } catch (Exception ex) {
                    showAlert("Ошибка", "Проверьте введенные данные.");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void deleteSchedule(TableView<Schedule> table) {
        Schedule selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Ошибка", "Выберите запись.");
            return;
        }

        scheduleDAO.delete(selected.getId());
        table.setItems(FXCollections.observableArrayList(scheduleDAO.findAll()));
    }

    @FXML
    private void showUsers() {
        if (!"ADMIN".equals(currentUser.getRole())) return;

        Tab tab = new Tab("Пользователи");
        TableView<User> table = createUserTable();

        HBox controls = new HBox(10);

        Button changeRoleBtn = new Button("Изменить роль");
        changeRoleBtn.setOnAction(e -> changeUserRole(table));

        Button deleteBtn = new Button("Удалить пользователя");
        deleteBtn.setOnAction(e -> deleteUser(table));

        controls.getChildren().addAll(changeRoleBtn, deleteBtn);

        VBox vbox = new VBox(10, controls, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        tab.setContent(vbox);
        addTab(tab);
    }

    private void changeUserRole(TableView<User> table) {
        User selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Ошибка", "Выберите пользователя.");
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Изменить роль");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        ComboBox<String> roleCombo = new ComboBox<>(FXCollections.observableArrayList("ADMIN", "MANAGER", "ARTIST", "VIEWER"));
        roleCombo.setValue(selected.getRole());

        grid.add(new Label("Новая роль:"), 0, 0);
        grid.add(roleCombo, 1, 0);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String newRole = roleCombo.getValue();
                selected.setRole(newRole);
                userDAO.update(selected);
                table.refresh();
                return newRole;
            }
            return null;
        });

        dialog.showAndWait();
    }

    @FXML
    private void showStats() {
        long userCount = userDAO.countUsers();
        double avgDuration = showDAO.averageDuration();

        BarChart<String, Number> chart = new BarChart<>(new CategoryAxis(), new NumberAxis());
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Пользователи", userCount));
        series.getData().add(new XYChart.Data<>("Средняя продолжительность шоу (мин)", avgDuration));
        chart.getData().add(series);

        Tab tab = new Tab("Статистика");
        VBox vbox = new VBox(10,
                new Label("Количество пользователей: " + userCount),
                new Label("Средняя продолжительность представлений: " + String.format("%.2f", avgDuration) + " мин."),
                chart
        );
        tab.setContent(vbox);
        addTab(tab);
    }

    @FXML
    private void showAbout() {
        Tab tab = new Tab("Об авторе");
        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(
                new Label("ФИО: Тесля Сергей Ярославович"),
                new Label("Группа: ПИ23-1"),
                new Label("Email: tesly2005@yandex.ru"),
                new Label("Телефон: +7(915)390-32-49"),
                new Label("Опыт: Работа с Java, JavaFX, Hibernate, PostgreSQL в учебных проектах"),
                new Label("Даты работ: Начало — 14.10.2025, Завершение — 19.12.2025")
        );
        tab.setContent(vbox);
        addTab(tab);
    }

    @FXML
    private void logout() {

        try {
            FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Scene loginScene = new Scene(loginLoader.load(), 700, 600);

            // Получаем контроллер логина и передаём ему stage (чтобы он мог работать дальше)
            LoginController loginController = loginLoader.getController();
            loginController.setPrimaryStage(primaryStage);
            loginController.setMainScene(mainScene);        // если используешь
            loginController.setMainController(this);         // если нужно

            primaryStage.setScene(loginScene);
            primaryStage.setTitle("Информационно-справочная система цирка");
            primaryStage.centerOnScreen();  // опционально, чтобы окно было по центру

        } catch (IOException e) {
            showAlert("Ошибка", "Не удалось загрузить экран входа.");
            e.printStackTrace();
        }
    }

    private void addTab(Tab tab) {
        tabPane.getTabs().removeIf(t -> t.getText().equals(tab.getText()));
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}