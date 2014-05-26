/*
 * $Id$
 * $HeadURL$
 */
package shouter.api.handlers.user.block;

import shouter.api.ApiConstants;
import shouter.api.beans.ApiError;
import shouter.api.beans.BlockedUser;
import shouter.api.handlers.BaseApiHandler;
import shouter.api.utils.DataUtil;

import javax.servlet.http.HttpServletRequest;

/**
 * TODO: Enter class description...
 *
 * @author chuck (charlie.effinger@gmail.com)
 * @version $Revision$ $LastChangedDate$
 */
public class UnBlockHandler extends BaseApiHandler {
    private final String userName;

    private final String blockedUserName;

    public UnBlockHandler(HttpServletRequest request) {
        super(request);

        this.responseString = "isBlocked";
        this.userName = DataUtil.formatParameter(request, ApiConstants.PARAM_USER_NAME);
        this.blockedUserName = DataUtil.formatParameter(request, ApiConstants.PARAM_BLOCKED_USER_NAME);
    }

    @Override
    protected boolean validateParameters() {
        if (DataUtil.isEmpty(userName)) {
            // missing.userName
            errors.add(new ApiError(null, null, null));
        }

        if (DataUtil.isEmpty(blockedUserName)) {
            // missing.blockedUserName
            errors.add(new ApiError(null, null, null));
        }

        if (errors.isEmpty()) {
            if (userName.equals(blockedUserName)) {
                // can't unblock self
                errors.add(new ApiError(null, null, null));
            }
        }

        return super.validateParameters();
    }

    @Override
    protected void performRequest() {
        try {
            BlockedUser blockedUser = new BlockedUser(userName, blockedUserName);
            awsDao.unblockUser(blockedUser);
            responseObjects.add(false);
        } catch (Exception e) {
            this.responseString = "errors";
            responseObjects.add(new ApiError(null, null, null));
        }
    }
}
