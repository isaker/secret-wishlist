package secretwishlist.utils;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import org.json.JSONObject;
import secretwishlist.model.Wishlist;
import software.amazon.awssdk.http.Header;
import software.amazon.awssdk.http.HttpStatusCode;

import java.util.HashMap;

public class HttpResponseUtil {

    private static final Gson gson = new Gson();

    public static APIGatewayProxyResponseEvent errorResponse(int statusCode, String description) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(statusCode);
        JSONObject body = new JSONObject().put("description", description);
        response.setBody(body.toString());
        HashMap<String, String> headers = new HashMap<>();
        headers.put(Header.CONTENT_TYPE, "application/json");

        return response;
    }

    public static APIGatewayProxyResponseEvent successWishlistResponse(Wishlist wishlist) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(HttpStatusCode.OK);
        response.setBody(gson.toJson(wishlist));
        HashMap<String, String> headers = new HashMap<>();
        headers.put(Header.CONTENT_TYPE, "application/json");

        return response;
    }
}
