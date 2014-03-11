/*
 * $Id$
 * $HeadURL$
 */
package shouter.api.handlers.user;

import shouter.api.ApiConstants;
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
public class BlockHandler extends BaseApiHandler {

    private final String userName;

    private final String blockedUserName;

    public BlockHandler(HttpServletRequest request) {
        super(request);

        this.responseString = "isBlocked";
        this.userName = DataUtil.formatParameter(request, ApiConstants.PARAM_USER_NAME);
        this.blockedUserName = DataUtil.formatParameter(request, ApiConstants.PARAM_USER_NAME);
    }

    @Override
    protected boolean validateParameters() {
        if (DataUtil.isEmpty(userName)) {
            // missing.userName
        }

        if (DataUtil.isEmpty(blockedUserName)) {
            // missing.blockedUserName
        }

        if (errors.isEmpty()) {
            if (userName.equals(blockedUserName)) {
                // can't block self
            }
        }

        return super.validateParameters();
    }


    protected void performRequest() {
        BlockedUser blockedUser = new BlockedUser(userName, blockedUserName);
        awsDao.blockUser(blockedUser);
        responseObjects.add(true);
    }
}
