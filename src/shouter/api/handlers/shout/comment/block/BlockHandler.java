/*
 * $Id$
 * $HeadURL$
 */
package shouter.api.handlers.shout.comment.block;

import shouter.api.ApiConstants;
import shouter.api.beans.ApiError;
import shouter.api.beans.BlockedShout;
import shouter.api.handlers.BaseApiHandler;
import shouter.api.utils.DataUtil;

import javax.servlet.http.HttpServletRequest;

/**
 * TODO: Enter class description...
 *
 * @author chuck (charlie.effinger@gmail.com)
 * @version $Revision$ $LastChangedDate$
 */
public class BlockHandler extends BaseApiHandler {

    private final String userName;

    private final String shoutId;

    public BlockHandler(HttpServletRequest request) {
        super(request);

        this.responseString = "isBlocked";
        this.userName = DataUtil.formatParameter(request, ApiConstants.PARAM_USER_NAME);
        this.shoutId = DataUtil.formatParameter(request, ApiConstants.PARAM_SHOUT_ID);
    }

    @Override
    protected boolean validateParameters() {
        if (DataUtil.isEmpty(userName)) {
            // missing.userName
        }

        if (DataUtil.isEmpty(shoutId)) {
            // missing.blockedUserName
        }

        return super.validateParameters();
    }


    protected void performRequest() {
        try {
            BlockedShout blockedShout = new BlockedShout(userName, shoutId);
            awsDao.blockShout(blockedShout);
            responseObjects.add(true);
        } catch (Exception e) {
            this.responseString = "errors";
            responseObjects.add(new ApiError(null, null, null));
        }
    }
}
