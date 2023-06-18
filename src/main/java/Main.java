import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;


public class Main {
    static final int PORT = 8989;
    static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) throws Exception {

        BooleanSearchEngine engine = new BooleanSearchEngine(new File("pdfs"));

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("\nНачало работы сервера " + PORT + "... \nПодключение...\n");
            while (true) {
                try (
                        Socket clientSocket = serverSocket.accept();
                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
                ) {
                    System.out.println("Новое соединение!");
                    System.out.println("Адрес клиента: " + clientSocket.getInetAddress() + " , port: " + clientSocket.getPort());
                    String json = in.readLine();
                    String word = in.readLine();
                    out.println(gson.toJson(engine.search(word)));

                    if (!word.isEmpty()) {
                        List<PageEntry> result = engine.search(word);
                        System.out.println(listToJson(result));
                        out.println(listToJson(result));
                    }
                    System.out.println("Client сообщение: " + json);
                    out.println("Hello!");
                }
            }
        } catch (IOException e) {
            System.out.println("Нет подключения!");
            e.printStackTrace();
        }
    }

    public static <T> Object listToJson(List<T> list) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        Type listType = new TypeToken<List<T>>() {
        }.getType();
        return gson.toJson(list, listType);
    }
}
