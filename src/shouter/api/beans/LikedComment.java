/*
 * $Id$
 * $HeadURL$
 */
package shouter.api.beans;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import shouter.api.dao.AwsConstants;

/**
 * TODO: Enter class description...
 *
 * @author chuck (charlie.effinger@gmail.com)
 * @version $Revision$ $LastChangedDate$
 */
@DynamoDBTable(tableName = AwsConstants.LIKED_COMMENTS_TABLE)
public class LikedComment {

    private String userName;

    private String shoutId;

    private String commentId;

    public LikedComment(String userName, String shoutId, String commentId) {
        this.shoutId = shoutId;
        this.userName = userName;
        this.commentId = commentId;
    }

    @DynamoDBHashKey(attributeName = AwsConstants.USER_NAME)
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @DynamoDBRangeKey(attributeName = AwsConstants.SHOUT_ID)
    public String getShoutId() {
        return shoutId;
    }

    public void setShoutId(String shoutId) {
        this.shoutId = shoutId;
    }

    @DynamoDBAttribute(attributeName = AwsConstants.COMMENT_ID)
    public String getCommentId() { return commentId; }

    public void setCommentId(String commentId) { this.commentId = commentId; }
}
