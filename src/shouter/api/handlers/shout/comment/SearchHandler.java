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

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

/**
 * Handles retrieving comments for the given shout
 *
 * @author Charles Effinger (charles.effinger@cbsinteractive.com)
 * @version $Revision$, $LastChangedDate$
 */
public class SearchHandler extends BaseApiHandler {

    private final String shoutId;
    private final String userName;

    public SearchHandler(HttpServletRequest request) {
        super(request);

        this.responseString = "shouts";

        this.shoutId = DataUtil.formatParameter(request, ApiConstants.PARAM_SHOUT_ID);
        this.userName = DataUtil.formatParameter(request, ApiConstants.PARAM_USER_NAME);
    }

    @Override
    protected boolean validateParameters() {

        // validate the parent ID
        if (DataUtil.isEmpty(shoutId)) {
            errors.add(new ApiError("400", ApiConstants.MISSING_PREFIX + ApiConstants.PARAM_SHOUT_ID,
                    "Missing parentId. Please provide a parentId."));
        }

        return super.validateParameters();
    }

    @Override
    protected void performRequest() {
        // retrieve all the comments
        Collection<Comment> comments = awsDao.getShoutComments(shoutId, userName);
        Shout parentShout = awsDao.getShoutFromId(shoutId);
        parentShout.setComments(comments);
        responseObjects.add(parentShout);
    }
}
