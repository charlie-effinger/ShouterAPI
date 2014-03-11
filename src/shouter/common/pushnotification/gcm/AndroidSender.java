/*
 * $Id$
 * $HeadURL$
 */
package shouter.common.pushnotification.gcm;

import shouter.api.dao.AwsConstants;
import shouter.api.dao.AwsDao;
import shouter.common.pushnotification.PushNotificationSender;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

/**
 * Class that handles making calls to the Google Project for Push Notifications
 *
 * @author Charles Effinger (charles.effinger@cbsinteractive.com)
 * @version $Revision$, $LastChangedDate$
 */
public class AndroidSender extends PushNotificationSender {

    private static final String GCM_URL = "https://android.googleapis.com/gcm/send";

    public static void sendCommentNotification(Collection<String> userNames) {
        // create thread here?

        if (!userNames.isEmpty()) {
            AwsDao awsDao = new AwsDao();
            Map<String, Collection<String>> pushNotificationIds = awsDao.getPushNotificationIds(userNames);

            if (!pushNotificationIds.isEmpty()) {
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
                    wr.writeBytes(buildJsonRequest(pushNotificationIds.get(AwsConstants.ANDROID_ID)));
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

                // TODO: IOS SHIT
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
