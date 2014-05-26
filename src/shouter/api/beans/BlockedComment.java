/*
 * $Id$
 * $HeadURL$
 */
package shouter.api.beans;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import shouter.api.dao.AwsConstants;

/**
 * TODO: Enter class description...
 *
 * @author chuck (charlie.effinger@gmail.com)
 * @version $Revision$ $LastChangedDate$
 */
@DynamoDBTable(tableName = AwsConstants.BLOCKED_COMMENT_TABLE)
public class BlockedComment {

    private String userName;

    private String commentId;

    private String shoutId;

    public BlockedComment(String userName, String commentId, String shoutId) {
        this.userName = userName;
        this.commentId = commentId;
        this.shoutId = shoutId;
    }


    @DynamoDBRangeKey(attributeName = AwsConstants.SHOUT_ID)
    public String getShoutId() {
        return shoutId;
    }

    public void setShoutId(String shoutId) {
        this.shoutId = shoutId;
    }

    @DynamoDBRangeKey(attributeName = AwsConstants.COMMENT_ID)
    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    @JsonIgnore
    @DynamoDBHashKey(attributeName = AwsConstants.USER_NAME)
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


}
