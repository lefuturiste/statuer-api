package fr.lefuturiste.statuer;

import fr.lefuturiste.statuer.models.Service;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class Checker {

    private static OkHttpClient httpClient = new OkHttpClient();

    /**
     * Find out if a service is now available
     */
    public static boolean isAvailable(Service service) {
        Request request = new Request.Builder().url(service.getUrl()).build();
        Response response;
        int code = 0;
        int expectedStatus = service.getHttpExpectedStatus() == 0 ? 200 : service.getHttpExpectedStatus();
        try {
            response = httpClient.newCall(request).execute();
            code = response.code();
        } catch (IOException ignored) {}

        return expectedStatus == code;
    }
}
