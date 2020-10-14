package secretwishlist.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import org.apache.commons.lang3.RandomStringUtils;
import secretwishlist.model.CreateWishlistInput;
import secretwishlist.model.Wishlist;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.time.Instant;
import java.util.ArrayList;

public class CreateWishlist implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final Gson gson = new Gson();
    private final DynamoDbClient dynamoDbClient = DynamoDbClient.create();
    private final DynamoDbEnhancedClient dynamoDbEnhancedClient = DynamoDbEnhancedClient.builder().dynamoDbClient(dynamoDbClient).build();
    private final String wishlistsTableName = System.getenv("wishlistsTable");

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        System.out.println(String.format("Incoming request: %s", requestEvent.toString()));

        CreateWishlistInput input = gson.fromJson(requestEvent.getBody(), CreateWishlistInput.class);
        Wishlist newWishlist = createNewWishlist(input.getName());
        storeWishlist(newWishlist);

        return createResponse(200, gson.toJson(newWishlist));
    }

    private APIGatewayProxyResponseEvent createResponse(int statusCode, String body) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(statusCode);
        response.setBody(body);
        return response;
    }

    private void storeWishlist(Wishlist newWishlist) {
        DynamoDbTable<Wishlist> wishlistsTable = dynamoDbEnhancedClient.table(wishlistsTableName, TableSchema.fromBean(Wishlist.class));
        wishlistsTable.putItem(newWishlist);
    }

    private Wishlist createNewWishlist(String name) {
        Wishlist newWishlist = new Wishlist();
        newWishlist.setName(name);
        newWishlist.setId(RandomStringUtils.randomAlphanumeric(8));
        newWishlist.setCreated(Instant.now());
        newWishlist.setItems(new ArrayList<>());

        return newWishlist;

    }
}
