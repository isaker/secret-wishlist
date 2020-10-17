package secretwishlist.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import secretwishlist.utils.HttpResponseUtil;
import software.amazon.awssdk.http.HttpStatusCode;

public class RemoveItem implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent, Context context) {
        return HttpResponseUtil.errorResponse(HttpStatusCode.NOT_FOUND, "NOT IMPLEMENTED");
    }
}
