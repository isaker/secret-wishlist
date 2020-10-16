package secretwishlist.utils;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.json.JSONObject;
import software.amazon.awssdk.http.Header;

import java.util.HashMap;

public class HttpResponseUtil {

    public static APIGatewayProxyResponseEvent errorResponse(int statusCode, String description) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(statusCode);
        JSONObject body = new JSONObject().put("description", description);
        response.setBody(body.toString());
        HashMap<String, String> headers = new HashMap<>();
        headers.put(Header.CONTENT_TYPE, "application/json");

        return response;
    }
}
