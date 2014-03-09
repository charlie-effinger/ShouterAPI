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

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

/**
 * Handles retrieving comments for the given shout
 *
 * @author Charles Effinger (charles.effinger@cbsinteractive.com)
 * @version $Revision$, $LastChangedDate$
 */
public class SearchHandler extends BaseApiHandler {

    private final String parentId;

    public SearchHandler(HttpServletRequest request) {
        super(request);

        this.responseString = "shouts";

        this.parentId = DataUtil.formatParameter(request, ApiConstants.PARAM_PARENT_ID);
    }

    @Override
    protected boolean validateParameters() {

        // validate the parent ID
        if (DataUtil.isEmpty(parentId)) {
            errors.add(new ApiError("400", ApiConstants.MISSING_PREFIX + ApiConstants.PARAM_PARENT_ID,
                    "Missing parentId. Please provide a parentId."));
        }

        return super.validateParameters();
    }

    @Override
    protected void performRequest() {
        // retrieve all the comments
        Collection<Shout> comments = awsDao.getShoutComments(parentId);
        Shout parentShout = awsDao.getShoutFromId(parentId);
        parentShout.setComments(comments);
        responseObjects.add(parentShout);
    }
}
