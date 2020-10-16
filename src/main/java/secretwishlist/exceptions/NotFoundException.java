package secretwishlist.exceptions;

public class NotFoundException extends Exception{
    private String id;

    public NotFoundException(String id) {
        super();
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
