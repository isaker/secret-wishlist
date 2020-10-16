package secretwishlist.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import secretwishlist.dao.WishlistDao;
import secretwishlist.exceptions.NotFoundException;
import secretwishlist.model.Wishlist;
import software.amazon.awssdk.http.Header;
import software.amazon.awssdk.http.HttpStatusCode;

import java.util.HashMap;
import java.util.Optional;

public class GetWishlist implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final WishlistDao wishlistDao = new WishlistDao();
    private final Gson gson = new Gson();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        try {
        Optional<String> id = Optional.of(event.getPathParameters().get("id"));
        Optional<Wishlist> wishlist = wishlistDao.getWishlist(id.orElseThrow(IllegalArgumentException::new));

        return createResponse(HttpStatusCode.OK, gson.toJson(wishlist.orElseThrow(() -> new NotFoundException(id.get()))));
        } catch (IllegalArgumentException e) {
            return createResponse(HttpStatusCode.BAD_REQUEST, "'id' is a required field.");
        } catch (NotFoundException e) {
            return createResponse(HttpStatusCode.NOT_FOUND, String.format("No wishlist found with id '%s'.", e.getId()));
        }
    }

    private APIGatewayProxyResponseEvent createResponse(int statusCode, String body) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(statusCode);
        response.setBody(body);
        HashMap<String, String> headers = new HashMap<>();
        headers.put(Header.CONTENT_TYPE, "application/json");
        return response;
    }
}
