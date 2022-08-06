package ir.sharif.aic.hideandseek.config;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class GraphicLogger implements AutoCloseable {
    private static final GraphicLogger ourInstance = new GraphicLogger();
    private BufferedWriter graphicLogger;

    public static GraphicLogger getInstance() {
        return ourInstance;
    }

    private GraphicLogger() {
        try {
            graphicLogger = new BufferedWriter(new FileWriter("logs/server.log", StandardCharsets.UTF_8, true));
        } catch (IOException e) {
            System.err.println("Can not write to graphic.log");
            e.printStackTrace();
        }
    }

    public void appendLog(String message){
        try {
            this.graphicLogger.write("\"" + message + "\"");
            this.graphicLogger.write("\n");
            this.graphicLogger.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws Exception {
        this.graphicLogger.close();
    }
}
