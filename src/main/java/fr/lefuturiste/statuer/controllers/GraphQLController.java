package fr.lefuturiste.statuer.controllers;

import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;
import fr.lefuturiste.statuer.App;
import fr.lefuturiste.statuer.HibernateService;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import org.json.JSONObject;
import spark.Route;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class GraphQLController {
    public static Route execute = (req, res) -> {
        GraphQLSchema graphQLSchema = new GraphQLJpaSchemaBuilder(HibernateService.getEntityManager()).build();
        GraphQL build = GraphQL.newGraphQL(graphQLSchema).build();
        JSONObject jsonObject = new JSONObject(req.body());
        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                .query(jsonObject.getString("query"))
                .build();
        ExecutionResult executionResult = build.execute(executionInput);
        return App.returnJSON(res, new JSONObject(executionResult.toSpecification()));
    };
}
