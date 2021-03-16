package client;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class MessagesHistory {
    FileOutputStream fileOutputStream;


    public MessagesHistory() {
    }

    public String getNameFiletxt(String login) {
        return "client/messagesHistory/history_" + login + ".txt";
    }

    /** метод который создает файл с историей клиента*/
    public void createHistoryUserFiletxt (String login) throws IOException {
        File file = new File (getNameFiletxt(login));
        if(file.createNewFile()){
            System.out.println("file.txt файл создан в директории messagesHistory");
        }else System.out.println("file.txt файл уже существует");

    }

    /**
     * метод который создает файл с историей клиента и записывает в файл историю переписки юзера
     */
    public void writeHistoryUserFiletxt(String login, String srt) throws IOException {
        fileOutputStream = new FileOutputStream(getNameFiletxt(login), true);
        fileOutputStream.write(srt.getBytes());

    }

    public void closeWriteHistoryUserFiletxt() {
        try {
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * метод который читает инфу из файла с историей клиента Java NIO
     */
    public String readerHistoryUserFiletxt(String login) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();

        List<String> lines = Files.readAllLines(Paths.get(getNameFiletxt(login)), UTF_8);
        if (lines.size()==0){return "";}
        if (lines.size() > 5) {
            for (int i = lines.size() - 5; i < lines.size(); i++) {
                stringBuilder.append(lines.get(i)).append("\n");
            }
        } else
            for (int i = 0; i < lines.size(); i++) {
                stringBuilder.append(lines.get(i)).append("\n");
            }

//        for (String s: lines) {
//            System.out.println(s);
//        }
        return stringBuilder.toString();
    }




//    public static void main(String[] args) {
//        MessagesHistory w = new MessagesHistory();
//        try {
//            w.writeHistoryUserFiletxt("rr", "ww");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

//    /** метод который читает инфу из файла с историей клиента Java IO*/
//    public static void readerHistoryUserFiletxt () throws IOException {
//
//        FileInputStream fileInputStream = new FileInputStream(getNameFiletxt("rw"));
//        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream, 200);
//        int i;
//        while((i = bufferedInputStream.read())!= -1){
//
//            System.out.print((char)i);
//        }
//    }

}
