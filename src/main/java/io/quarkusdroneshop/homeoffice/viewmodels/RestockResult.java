package io.quarkusdroneshop.homeoffice.viewmodels;

public class RestockResult {

    public boolean success;

    public String message;

    public InventoryLevel level;

    public RestockResult() {
    }

    public RestockResult(boolean success, String message, InventoryLevel level) {
        this.success = success;
        this.message = message;
        this.level = level;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
