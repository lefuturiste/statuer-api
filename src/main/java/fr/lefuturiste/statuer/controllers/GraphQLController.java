package fr.lefuturiste.statuer.controllers;

import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;
import fr.lefuturiste.statuer.App;
import fr.lefuturiste.statuer.HibernateService;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import org.json.JSONException;
import org.json.JSONObject;
import spark.Route;

public class GraphQLController {
    public static Route execute = (req, res) -> {
        GraphQLSchema graphQLSchema = new GraphQLJpaSchemaBuilder(HibernateService.getEntityManager()).build();
        GraphQL build = GraphQL.newGraphQL(graphQLSchema).build();
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(req.body());
        } catch (JSONException exception) {
            res.status(400);
            return new JSONObject()
                    .put("error", "Invalid JSON input")
                    .put("success", false);
        }
        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                .query(jsonObject.getString("query"))
                .build();
        ExecutionResult executionResult = build.execute(executionInput);
        return App.returnJSON(res, new JSONObject(executionResult.toSpecification()));
    };
}
