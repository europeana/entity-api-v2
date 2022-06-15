package eu.europeana.entity.web.model.vocabulary;

public class EntityApiErrorResponse {

    private String apikey;
    private String action;
    private boolean success;
    private String error;

    public EntityApiErrorResponse(String apikey, String action, boolean success, String error) {
        this.apikey = apikey;
        this.action = action;
        this.success = success;
        this.error = error;
    }

    public String getApikey() {
        return apikey;
    }

    public void setApikey(String apikey) {
        this.apikey = apikey;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
