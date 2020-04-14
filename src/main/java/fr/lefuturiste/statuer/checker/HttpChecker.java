package fr.lefuturiste.statuer.checker;

import fr.lefuturiste.statuer.models.Service;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class HttpChecker implements CheckerInterface {

    private static OkHttpClient httpClient;

    public HttpChecker() {
        httpClient = new OkHttpClient();
    }

    public boolean isAvailable(Service service) {
        Request request = new Request.Builder().url(service.getUrl()).build();
        Response response;
        int code = 0;
        int expectedStatus = service.getHttpExpectedStatus() == 0 ? 200 : service.getHttpExpectedStatus();
        try {
            response = httpClient.newCall(request).execute();
            response.close();
            code = response.code();
        } catch (IOException ignored) {}
        return expectedStatus == code;
    }
}
