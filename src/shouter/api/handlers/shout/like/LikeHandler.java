/*
 * $Id$
 * $HeadURL$
 */
package shouter.api.handlers.shout.like;

import shouter.api.ApiConstants;
import shouter.api.beans.LikedShout;
import shouter.api.handlers.BaseApiHandler;
import shouter.api.utils.DataUtil;

import javax.servlet.http.HttpServletRequest;
import javax.xml.crypto.Data;

/**
 * TODO: Enter class description...
 *
 * @author chuck (charlie.effinger@gmail.com)
 * @version $Revision$ $LastChangedDate$
 */
public class LikeHandler extends BaseApiHandler {

    private final String userName;

    private final String shoutId;

    public LikeHandler(HttpServletRequest request) {
        super(request);
        this.responseString = "isLiked";
        this.userName = DataUtil.formatParameter(request, ApiConstants.PARAM_USER_NAME);
        this.shoutId = DataUtil.formatParameter(request, ApiConstants.PARAM_SHOUT_ID);
    }

    @Override
    protected boolean validateParameters() {
        if (DataUtil.isEmpty(userName)) {
            // missing.userName
        }

        if (DataUtil.isEmpty(shoutId)) {
            // missing.shoutId
        }
        return super.validateParameters();
    }

    @Override
    protected void performRequest() {
        LikedShout likedShout = new LikedShout(userName, shoutId);
        awsDao.likeShout(likedShout);
        responseObjects.add(true);
    }
}