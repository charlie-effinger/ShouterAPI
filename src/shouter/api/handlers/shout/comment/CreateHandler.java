/*
 * $Id$
 * $HeadURL$
 */
package shouter.api.handlers.shout.comment;

import shouter.api.ApiConstants;
import shouter.api.beans.ApiError;
import shouter.api.beans.Comment;
import shouter.api.beans.Shout;
import shouter.api.handlers.BaseApiHandler;
import shouter.api.utils.DataUtil;
import shouter.common.pushnotification.PushNotificationSender;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

/**
 * Handles posting a comment for a given shout. All comments of the given shout will be returned.
 *
 * @author Charles Effinger (charles.effinger@cbsinteractive.com)
 * @version $Revision$, $LastChangedDate$
 */
public class CreateHandler extends BaseApiHandler {

    private final String userName;

    private final String shoutId;

    private final String message;

    public CreateHandler(HttpServletRequest request) {
        super(request);

        this.responseString = "shouts";

        this.userName = DataUtil.formatParameter(request, ApiConstants.PARAM_USER_NAME);
        this.shoutId = DataUtil.formatParameter(request, ApiConstants.PARAM_SHOUT_ID);
        this.message = DataUtil.formatParameter(request, ApiConstants.PARAM_MESSAGE);
    }

    @Override
    protected boolean validateParameters() {

        // validate the parent ID
        if (DataUtil.isEmpty(shoutId)) {
            errors.add(new ApiError("400", ApiConstants.MISSING_PREFIX + ApiConstants.PARAM_SHOUT_ID,
                    "Missing parentId. Please provide a parentId."));
        }

        // validate the message
        if (!DataUtil.isEmpty(message)) {
            if (!DataUtil.is256characters(message)) {
                errors.add(new ApiError("400", ApiConstants.INVALID_PREFIX + ApiConstants.PARAM_MESSAGE,
                        "Invalid message. Please provide that is 256 characters or less."));
            }
        } else {
            errors.add(new ApiError("400", ApiConstants.MISSING_PREFIX + ApiConstants.PARAM_MESSAGE,
                    "Missing message. Please provide a message."));
        }

        return super.validateParameters();
    }

    @Override
    protected void performRequest() {
        // set up the comment
        try {
            Comment commentToPost = new Comment(shoutId, message, userName);
            commentToPost.setTimestamp(System.currentTimeMillis() / 1000L);

            // post and retrieve comments
            Collection<Comment> comments = awsDao.postComment(commentToPost);
            Shout parentShout = awsDao.getShoutFromId(shoutId);
            parentShout.setComments(comments);
            responseObjects.add(parentShout);
//            if (comments != null) {  // should never be null, just a sanity check I suppose
//                Collection<String> userNames = new HashSet<String>();
//                for (Comment comment: comments) {
//                    if (!comment.getUserName().equals(commentToPost.getUserName())) {
//                        userNames.add(comment.getUserName());
//                    }
//                }
//                if (parentShout != null) { // add the original shouter to the phoneId list
//                    if (!parentShout.getUserName().equals(commentToPost.getUserName())) {
//                        userNames.add(parentShout.getUserName());
//                    }
//                }
//                Map<String, Collection<String>> pushNotificationIds = awsDao.getPushNotificationIds(userNames);
//                PushNotificationSender.sendNotifications(pushNotificationIds);
//            }

        } catch (Exception e) {
            this.responseString = "errors";
            responseObjects.add(new ApiError(null, null, null));
        }
    }


}
