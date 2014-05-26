/*
 * $Id$
 * $HeadURL$
 */
package shouter.api.handlers.shout.comment.like;

import shouter.api.ApiConstants;
import shouter.api.beans.ApiError;
import shouter.api.beans.LikedComment;
import shouter.api.handlers.BaseApiHandler;
import shouter.api.utils.DataUtil;

import javax.servlet.http.HttpServletRequest;

/**
 * TODO: Enter class description...
 *
 * @author chuck (charlie.effinger@gmail.com)
 * @version $Revision$ $LastChangedDate$
 */
public class UnLikeHandler extends BaseApiHandler {

    private final String userName;

    private final String shoutId;

    private final String commentId;

    public UnLikeHandler(HttpServletRequest request) {
        super(request);
        this.responseString = "isLiked";
        this.userName = DataUtil.formatParameter(request, ApiConstants.PARAM_USER_NAME);
        this.shoutId = DataUtil.formatParameter(request, ApiConstants.PARAM_SHOUT_ID);
        this.commentId = DataUtil.formatParameter(request, ApiConstants.PARAM_COMMENT_ID);
    }

    @Override
    protected boolean validateParameters() {
        if (DataUtil.isEmpty(userName)) {
            errors.add(new ApiError(null, null, null));
            // missing.userName
        }

        if (DataUtil.isEmpty(shoutId)) {
            errors.add(new ApiError(null, null, null));
            // missing.shoutId
        }

        if (DataUtil.isEmpty(commentId)) {
            errors.add(new ApiError(null, null, null));
            // missing.shoutId
        }
        return super.validateParameters();
    }

    @Override
    protected void performRequest() {
        try {
            LikedComment likedComment = new LikedComment(userName, shoutId, commentId);
            awsDao.unLikeComment(likedComment);
            responseObjects.add(false);
        } catch (Exception e) {
            this.responseString = "errors";
            responseObjects.add(new ApiError(null, null, null));
        }
    }
}
