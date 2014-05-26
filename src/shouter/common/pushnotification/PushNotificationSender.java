/*
 * $Id$
 * $HeadURL$
 */
package shouter.common.pushnotification;

import shouter.api.dao.AwsConstants;

import java.util.Collection;
import java.util.Map;

/**
 * TODO: Enter class description...
 *
 * @author chuck (charlie.effinger@gmail.com)
 * @version $Revision$ $LastChangedDate$
 */
public class PushNotificationSender {

    public static void sendNotifications(Map<String, Collection<String>> ids) {
        Collection<String> androidIds = ids.get(AwsConstants.ANDROID_ID);
        Collection<String> iosIds = ids.get(AwsConstants.IOS_ID);
        if (!androidIds.isEmpty()) {

        }

        if (!iosIds.isEmpty()) {

        }
    }

    protected void buildNotification() {

    }

    protected void callAPI() {

    }
}
