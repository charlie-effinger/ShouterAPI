/*
 * $Id$
 * $HeadURL$
 */
package shouter.api.handlers.shout.comment;

import shouter.api.ApiConstants;
import shouter.api.beans.ApiError;
import shouter.api.beans.Shout;
import shouter.api.handlers.BaseApiHandler;
import shouter.api.utils.DataUtil;
import shouter.common.gcm.GoogleAPIClient;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.HashSet;

/**
 * Handles posting a comment for a given shout. All comments of the given shout will be returned.
 *
 * @author Charles Effinger (charles.effinger@cbsinteractive.com)
 * @version $Revision$, $LastChangedDate$
 */
public class CreateHandler extends BaseApiHandler {

    private final String phoneId;

    private final String parentId;

    private final String message;



    public CreateHandler(HttpServletRequest request) {
        super(request);

        this.responseString = "shouts";

        this.phoneId = DataUtil.formatParameter(request, ApiConstants.PARAM_PHONE_ID);
        this.parentId = DataUtil.formatParameter(request, ApiConstants.PARAM_PARENT_ID);
        this.message = DataUtil.formatParameter(request, ApiConstants.PARAM_MESSAGE);
    }

    @Override
    protected boolean validateParameters() {

        // validate the parent ID
        if (DataUtil.isEmpty(parentId)) {
            errors.add(new ApiError("400", ApiConstants.MISSING_PREFIX + ApiConstants.PARAM_PARENT_ID,
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
        Shout commentToPost = new Shout();
        commentToPost.setParentId(parentId);
        commentToPost.setMessage(message);
        commentToPost.setPhoneId(phoneId);
        commentToPost.setTimestamp(System.currentTimeMillis() / 1000L);
        // post and retrieve comments

        Collection<Shout> comments = awsDao.postComment(commentToPost);
        Shout parentShout = awsDao.getShoutFromId(parentId);
        parentShout.setComments(comments);
        if (comments != null) {  // should never be null, just a sanity check I suppose
            Collection<String> phoneIds = new HashSet<String>();
            for (Shout comment: comments) {
                if (!comment.getPhoneId().equals(commentToPost.getPhoneId())) {
                    phoneIds.add(comment.getPhoneId());
                }
            }
            if (parentShout != null) { // add the original shouter to the phoneId list
                if (!parentShout.getPhoneId().equals(commentToPost.getPhoneId())) {
                    phoneIds.add(parentShout.getPhoneId());
                }
            }
            GoogleAPIClient.sendCommentNotification(phoneIds);
        }

        responseObjects.add(parentShout);

    }


}
