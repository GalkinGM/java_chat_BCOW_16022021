package client;

import commands.Command;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    public TextArea textArea;
    @FXML
    public TextField textField;
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public HBox authPanel;
    @FXML
    public HBox msgPanel;
    @FXML
    public ListView<String> clientList;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private final int PORT = 8189;
    private final String IP_ADDRESS = "localhost";

    private boolean authenticated;
    private String nickname;
    private String login;
    private Stage stage;
    private Stage regStage;
    private RegController regController;
    private Stage chengNickNameStage;
    private ChengNickNameController chengNickNameController;
    private String loginForHistory;

    /** сеттеры*/
    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        msgPanel.setVisible(authenticated);
        msgPanel.setManaged(authenticated);
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);
        clientList.setVisible(authenticated);
        clientList.setManaged(authenticated);

        if (!authenticated) {

            nickname = "";

        }
        textArea.clear();
        setTitle(nickname);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            stage = (Stage) textArea.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                System.out.println("bye");
                if (socket != null && !socket.isClosed()) {
                    try {
                        out.writeUTF(Command.END);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
        setAuthenticated(false);
    }

    /** метод работы: подключение к серверу, цикл аутентификации, цикл работы*/
    private void connect() {

//        MessagesHistory messagesHistory = new MessagesHistory();
        try {

            socket = new Socket(IP_ADDRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    // цикл аутентификации
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/")) {
                            if (str.equals(Command.END)) {
                                throw new RuntimeException("Сервак нас отключает");
                            }
                            if (str.startsWith(Command.AUTH_OK)) {
                                String[] token = str.split("\\s");
                                nickname = token[1];
                                loginForHistory = token[2];
//                                MessagesHistory t = new MessagesHistory();
//                                try {
//
//                                    System.out.println(messagesHistory.readerHistoryUserFiletxt(loginForHistory));
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
                                setAuthenticated(true);
//                                textArea.appendText(messagesHistory.readerHistoryUserFiletxt(loginForHistory));
                                break;

                            }

                            if(str.startsWith(Command.REG_OK)){
                                String[] token = str.split("\\s");
                                loginForHistory = token[1];
//                                messagesHistory.createHistoryUserFiletxt(loginForHistory);
                                regController.setResultTryToReg(Command.REG_OK);
                            }

                            if(str.equals(Command.REG_NO)){
                                regController.setResultTryToReg(Command.REG_NO);
                            }
                        } else {
                            textArea.appendText(str + "\n");
                        }
                    }
                    //цикл работы
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/")) {
                            if (str.equals(Command.END)) {

                                System.out.println("Client disconnected");
//                                messagesHistory.closeWriteHistoryUserFiletxt();
                                break;
                            }
                            if (str.startsWith(Command.CLIENT_LIST)) {
                                String[] token = str.split("\\s");
                                Platform.runLater(() -> {
                                    clientList.getItems().clear();
                                    for (int i = 1; i < token.length; i++) {
                                        clientList.getItems().add(token[i]);
                                    }
                                });
                            }

                            //==============//
                            if (str.startsWith("/yournickis ")) {
                                nickname = str.split(" ")[1];
                                setTitle(nickname);
                            }
                            //==============//

                        } else {
                            textArea.appendText(str + "\n");

//                            messagesHistory.writeHistoryUserFiletxt(loginForHistory, str + "\n");

                        }
                    }
                } catch (RuntimeException e) {
                    System.out.println(e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                finally {
                    setAuthenticated(false);
                    try {

                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /** метод отправляет сообщения на сервер*/
    public void sendMsg(ActionEvent actionEvent) {
        try {
            out.writeUTF(textField.getText());
            textField.clear();
            textField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** метод отправляет сообщение на сервер с логином и паролем для аутентификации*/
    public void tryToAuth(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connect();
        }
        try {
            out.writeUTF(String.format("%s %s %s", Command.AUTH, loginField.getText().trim(), passwordField.getText().trim()));

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            passwordField.clear();
        }
    }

    /** метод изменяет название шапки, добавляет туда Nick после прохождения аутентификации*/
    private void setTitle(String nickname) {
        Platform.runLater(() -> {
            if (nickname.equals("")) {
                stage.setTitle("Best chat of World");
            } else {
                stage.setTitle(String.format("Best chat of World - [ %s ]", nickname));
            }
        });
    }

    /** метод автоматически прописывает команду для того чтобы можно было посылать
     *  личные сообщения при клике на клиента из списка клиентов*/
    public void clientListMouseReleased(MouseEvent mouseEvent) {
        System.out.println(clientList.getSelectionModel().getSelectedItem());
        String msg = String.format("%s %s ", Command.PRIVATE_MSG, clientList.getSelectionModel().getSelectedItem());
        textField.setText(msg);
    }

    /** метод делает окно регистрации видимым при нажатии на кнопку Reg*/
    public void showRegWindow(ActionEvent actionEvent) {
        if (regStage == null) {
            initRegWindow();
        }
        regStage.show();
    }

    /** метод создает окно регистрации с механикой всех кнопок*/
    private void initRegWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/reg.fxml"));
            Parent root = fxmlLoader.load();

            regController = fxmlLoader.getController();
            regController.setController(this);

            regStage = new Stage();
            regStage.setTitle("Best chat of World registration");
            regStage.setScene(new Scene(root, 450, 350));
            regStage.initStyle(StageStyle.UTILITY);
            regStage.initModality(Modality.APPLICATION_MODAL);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** метод посылает на сервер данные юзера для регистрации*/
    public void registration(String login, String password, String nickname){
        if (socket == null || socket.isClosed()) {
            connect();
        }
        try {
            out.writeUTF(String.format("%s %s %s %s", Command.REG, login, password, nickname));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** метод делает окно изменения имени видимым при нажатии на кнопку ChengNick*/
    public void showChengNickNameWindow(ActionEvent actionEvent) {
        if (chengNickNameStage == null) {
            initChengNickNameWindow();
        }
        chengNickNameStage.show();
    }

    /** метод создает окно изменения имени с механикой всех кнопок*/
    private void initChengNickNameWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/chengNickName.fxml"));
            Parent root = fxmlLoader.load();

            chengNickNameController = fxmlLoader.getController();
            chengNickNameController.setController(this);

            chengNickNameStage = new Stage();
            chengNickNameStage.setTitle("Best chat of World registration");
            chengNickNameStage.setScene(new Scene(root, 450, 350));
            chengNickNameStage.initStyle(StageStyle.UTILITY);
            chengNickNameStage.initModality(Modality.APPLICATION_MODAL);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** метод посылает на сервер данные юзера для проверки сушествует ли Nick
     * на который юзер хочет поменять старый Nick*/
    public void chengNickName(String login, String password, String nickname, String newNickName){
        if (socket == null || socket.isClosed()) {
            connect();
        }
        try {
            out.writeUTF(String.format("%s %s %s %s %s", Command.CHENGNICKNAME, login, password, nickname, newNickName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
