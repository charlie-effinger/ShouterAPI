/*
 * $Id$
 * $HeadURL$
 */
package shouter.common.gcm;

import shouter.api.dao.AwsDao;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;

/**
 * Class that handles making calls to the Google Project for Push Notifications
 *
 * @author Charles Effinger (charles.effinger@cbsinteractive.com)
 * @version $Revision$, $LastChangedDate$
 */
public class GoogleAPIClient {

    private static final String GCM_URL = "https://android.googleapis.com/gcm/send";

    public static void sendCommentNotification(Collection<String> phoneIds) {
        // create thread here?

        if (!phoneIds.isEmpty()) {
            AwsDao awsDao = new AwsDao();
            Collection<String> registrationIds = awsDao.getRegistrationIds(phoneIds);

            if (!registrationIds.isEmpty()) {
                // send the GCM shit I guess
                try {
                    URL url = new URL(GCM_URL);
                    HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Authorization", "key=AIzaSyBp25TGsbYCGvlxmh65kSD4XqVKRJgcTRQ");
                    connection.setUseCaches (false);
                    connection.setDoOutput(true);
                    DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                    wr.writeBytes(buildJsonRequest(registrationIds));
                    wr.flush();
                    wr.close();
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();


                } catch (Exception ignore) { }
            }
        }
    }

    private static String buildJsonRequest(Collection<String> registrationIds) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"registration_ids\": [");
        for (String registrationId : registrationIds) {
            sb.append("\"").append(registrationId).append("\",");
        }
        sb.append("]}");
        return sb.toString();
    }
}
