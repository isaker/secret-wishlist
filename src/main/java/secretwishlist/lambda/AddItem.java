package secretwishlist.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import secretwishlist.dao.WishlistDao;
import secretwishlist.exceptions.NotFoundException;
import secretwishlist.model.Item;
import secretwishlist.model.Wishlist;
import secretwishlist.utils.HttpResponseUtil;
import software.amazon.awssdk.http.HttpStatusCode;

import java.util.Optional;
import java.util.UUID;

public class AddItem implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final WishlistDao wishlistDao = new WishlistDao();
    private final Gson gson = new Gson();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        try {
            System.out.println("Request: " + event);
            Optional<String> id = Optional.ofNullable(event.getPathParameters().get("id"));
            Optional<String> body = Optional.ofNullable(event.getBody());
            String wishlistId = id.orElseThrow(() -> new IllegalArgumentException("'id' cannot be null."));
            if (id.get().isEmpty())
                throw new IllegalArgumentException("'id' cannot be null.");

            Optional<Wishlist> wishlist = wishlistDao.getWishlist(wishlistId);

            Item newItem = gson.fromJson(body.orElseThrow(() -> new IllegalArgumentException("Request body cannot be null.")), Item.class);
            newItem.setId(UUID.randomUUID().toString());
            wishlist.orElseThrow(() -> new NotFoundException(id.get())).addItem(newItem);

            wishlistDao.updateWishlist(wishlist.get());

            return HttpResponseUtil.successWishlistResponse(wishlist.get());
        } catch (IllegalArgumentException e) {
            return HttpResponseUtil.errorResponse(HttpStatusCode.BAD_REQUEST, e.getMessage());
        } catch (NotFoundException e) {
            return HttpResponseUtil.errorResponse(HttpStatusCode.NOT_FOUND, String.format("No wishlist found with id '%s'.", e.getId()));
        } catch (JsonSyntaxException e) {
            return HttpResponseUtil.errorResponse(HttpStatusCode.BAD_REQUEST, "Invalid JSON in request body.");
        }
    }
}
