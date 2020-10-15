package secretwishlist.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import org.apache.commons.lang3.RandomStringUtils;
import secretwishlist.dao.WishlistDao;
import secretwishlist.model.CreateWishlistInput;
import secretwishlist.model.Wishlist;
import software.amazon.awssdk.http.HttpStatusCode;

import java.time.Instant;
import java.util.ArrayList;

public class CreateWishlist implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final Gson gson = new Gson();
    private final WishlistDao wishlistDao = new WishlistDao();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        System.out.println(String.format("Incoming request: %s", requestEvent.toString()));

        CreateWishlistInput input = gson.fromJson(requestEvent.getBody(), CreateWishlistInput.class);
        Wishlist newWishlist = createNewWishlist(input.getName());
        wishlistDao.updateWishlist(newWishlist);

        return createResponse(HttpStatusCode.OK, gson.toJson(newWishlist));
    }

    private APIGatewayProxyResponseEvent createResponse(int statusCode, String body) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(statusCode);
        response.setBody(body);
        return response;
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
