package secretwishlist.lambda;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import secretwishlist.model.CreateWishlistInput;
import secretwishlist.model.Wishlist;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class CreateWishlist implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final Gson gson = new Gson();
    private final DynamoDbClient dynamoDbClient = DynamoDbClient.create();
    private final DynamoDBMapper dynamoDBMapper = new DynamoDBMapper()

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        System.out.println(String.format("Incoming request: %s", requestEvent.toString()));

        CreateWishlistInput input = gson.fromJson(requestEvent.getBody(), CreateWishlistInput.class);
        Wishlist newWishlist = new Wishlist(input.getName());

        return createResponse(200, gson.toJson(newWishlist));
    }

    private APIGatewayProxyResponseEvent createResponse(int statusCode, String body) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(statusCode);
        response.setBody(body);
        return response;
    }

    private void storeWishlist(Wishlist wishlist) {

    }
}
