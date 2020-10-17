package secretwishlist.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import secretwishlist.dao.WishlistDao;
import secretwishlist.exceptions.NotFoundException;
import secretwishlist.model.Wishlist;
import secretwishlist.utils.HttpResponseUtil;
import software.amazon.awssdk.http.HttpStatusCode;

import java.util.Optional;

public class ItemHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    WishlistDao wishlistDao = new WishlistDao();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        try {

            String wishlistId = event.getPathParameters().get("id");
            String itemId = event.getPathParameters().get("itemId");

            Optional<Wishlist> wishlist = wishlistDao.getWishlist(wishlistId);
            String method = event.getHttpMethod();
            Wishlist updatedWishlist;
            switch (method) {
                case "DELETE":
                    updatedWishlist = deleteItem(wishlist.orElseThrow(() -> new NotFoundException(wishlistId)), itemId);
                    break;
                case "POST":
                default:
                    updatedWishlist = updateItem(wishlist.orElseThrow(() -> new NotFoundException(wishlistId)));
            }
            return HttpResponseUtil.successWishlistResponse(updatedWishlist);

        } catch (NotFoundException e) {
            return HttpResponseUtil.errorResponse(HttpStatusCode.NOT_FOUND, String.format("No Wishlist with id '%s' found."));
        }
    }

    private Wishlist updateItem(Wishlist wishlist) {
        return wishlist;
    }

    private Wishlist deleteItem(Wishlist wishlist, String itemId) {
        Wishlist updatedWishlist = new Wishlist(wishlist);
        if (updatedWishlist.removeItem(itemId))
            wishlistDao.updateWishlist(updatedWishlist);
        return updatedWishlist;
    }
}
