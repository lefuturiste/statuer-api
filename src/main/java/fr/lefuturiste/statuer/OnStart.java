package fr.lefuturiste.statuer;


import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class OnStart implements Runnable {

    @Override
    public void run() {
        /*GraphQLSchema graphQLSchema = new GraphQLJpaSchemaBuilder(HibernateService.getEntityManager()).build();
        GraphQL build = GraphQL.newGraphQL(graphQLSchema).build();
        String query = null;
        System.out.println(System.getProperty("user.dir"));
        try {
            query = new String(Files.readAllBytes(Paths.get("./src/main/resources/query.txt")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ExecutionResult executionResult = build.execute(query);
        System.out.println(new JSONObject(executionResult.toSpecification()).toString());*/
//        ExecutionResult executionResult = executor.execute("{\n" +
//                "  project(name: \"GraphQL\") {\n" +
//                "    tagline\n" +
//                "  }\n" +
//                "}");
//        System.out.println(executionResult.getData().toString());
//        System.out.println(Objects.requireNonNull(IncidentStore.getOne(UUID.fromString("798641ce-b874-4167-a8ec-80c71903d3d6"))).toJSONObject(1));
        /*Incident incident = new Incident();
        incident.setId(UUID.randomUUID().toString());
        incident.setName("My incident #1");
        incident.setStartedAt(Instant.now());
        incident.setFinishedAt(Instant.now().plus(Duration.ofMinutes(30)));
        incident.setService(ServiceStore.getOne(UUID.fromString("ae5eb5d1-ee0b-4d77-b081-ce7704f8baa7")));
        IncidentStore.persist(incident);*/
        System.exit(0);
    }
}
