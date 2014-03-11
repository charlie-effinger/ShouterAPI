/*
 * $Id$
 * $HeadURL$
 */
package shouter.api.beans;

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
@DynamoDBTable(tableName = AwsConstants.BLOCKED_USER_TABLE)
public class BlockedUser {

    private String userName;

    private String blockedUserName;

    public BlockedUser(String userName, String blockedUserName) {
        this.userName = userName;
        this.blockedUserName = blockedUserName;
    }

    @DynamoDBHashKey(attributeName = AwsConstants.USER_NAME)
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @DynamoDBRangeKey(attributeName = AwsConstants.BLOCKED_USER_NAME)
    public String getBlockedUserName() {
        return blockedUserName;
    }

    public void setBlockedUserName(String blockedUserName) {
        this.blockedUserName = blockedUserName;
    }
}
