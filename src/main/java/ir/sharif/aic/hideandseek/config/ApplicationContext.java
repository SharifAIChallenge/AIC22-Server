//package ir.sharif.aic.hideandseek.config;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import ir.sharif.aic.hideandseek.database.InMemoryDataBase;
//import ir.sharif.aic.hideandseek.models.Node;
//import ir.sharif.aic.hideandseek.models.Vector;
//import org.modelmapper.ModelMapper;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.io.Resource;
//
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.List;
//
//@Configuration
//public class ApplicationContext {
//    @Value("jsons/Nodes.json")
//    private Resource nodesJsonFile;
//    @Value("jsons/Vectors.json")
//    private Resource vectorsJsonFile;
//
//    @Bean
//    public InMemoryDataBase dataBase() throws IOException {
//        ObjectMapper objectMapper = new ObjectMapper();
//        var nodes = ReadNodesFromJson(objectMapper);
//        var vectors = readVectorsFromJson(objectMapper);
//        return InMemoryDataBase.builder().nodes(nodes).vectors(vectors).build();
//    }
//
//    private List<Node> ReadNodesFromJson(ObjectMapper objectMapper) throws IOException {
//        var nodesDto = Arrays.asList(objectMapper.readValue(nodesJsonFile.getInputStream(), Node[].class));
//        return nodesDto;
//    }
//
//    private List<Vector> readVectorsFromJson(ObjectMapper objectMapper) throws IOException {
//        var vectorsDto = Arrays.asList(objectMapper.readValue(vectorsJsonFile.getInputStream(), Vector[].class));
//        return vectorsDto;
//    }
//}
