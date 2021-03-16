package client;

import commands.Command;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ChengNickNameController {


    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField nicknameField;
    @FXML
    private TextField newNickNameField;
    @FXML
    private TextArea textArea;

    private  Controller controller;

    public void setController(Controller controller) {
        this.controller = controller;
    }
    public  void  setResultTryToChengNickName (String connand){
        if (connand.equals(Command.CHENGNICKNAME_OK)){
            textArea.clear();
            textArea.appendText("NickName изменен");
        }

        if (connand.equals(Command.CHENGNICKNAME_NO)){
            textArea.clear();
            textArea.appendText("NickName уже занят");
        }

    }

    public void tryToChengNickName(ActionEvent actionEvent) {
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();
        String nickname = nicknameField.getText().trim();
        String newNickName = newNickNameField.getText().trim();

        if( login.length()*password.length()*nickname.length()*newNickName.length() ==0){
            return;
        }

        controller.chengNickName (login, password, nickname, newNickName);

    }
}
