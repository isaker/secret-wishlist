package secretwishlist.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import secretwishlist.dao.WishlistDao;
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
        Optional<String> id = Optional.of(event.getPathParameters().get("id"));
        Optional<Wishlist> wishlist = wishlistDao.getWishlist(id.orElseThrow(IllegalArgumentException::new));

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(HttpStatusCode.OK);
        response.setBody(gson.toJson(wishlist.orElseThrow(IllegalArgumentException::new)));
        HashMap<String, String> headers = new HashMap<>();
        headers.put(Header.CONTENT_TYPE, "application/json");
        response.setHeaders(headers);
        return response;
    }
}
