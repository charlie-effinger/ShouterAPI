/*
 * $Id$
 * $HeadURL$
 */
package shouter.api.dao;

import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.model.Condition;
import shouter.api.beans.*;
import shouter.api.utils.DataUtil;

import java.util.*;
/**
 * Data access object for connecting with the Dynamo DB tables in AWS.
 *
 * @author Charles Effinger (charles.effinger@cbsinteractive.com)
 * @version $Revision$, $LastChangedDate$
 */
public class AwsDao {

    private final DynamoDBMapper mapper;

    public AwsDao() {
        AmazonDynamoDB client = new AmazonDynamoDBClient(new ClasspathPropertiesFileCredentialsProvider());
        client.setRegion(Region.getRegion(Regions.US_WEST_2));
        this.mapper = new DynamoDBMapper(client);
    }

    /**
     * Retrieves all shouts around a given location.
     *
     * @param latitude the latitude to search around
     * @param longitude  the longitude to search around
     * @return  all shouts within the given latitude and longitude
     */
    public Collection<Shout> getShouts(Double latitude, Double longitude, Long timeConstraint,
                                       Double locationConstraint, String userName) {

        Condition latitudeCondition = getBetweenCondition(latitude, locationConstraint);
        Condition longitudeCondition = getBetweenCondition(longitude, locationConstraint);
        Condition timeCondition = getTimeConstraintCondition(timeConstraint);

        // build the condition map
        Map<String, Condition> conditions = new HashMap<String, Condition>();
        conditions.put(AwsConstants.LATITUDE, latitudeCondition);
        conditions.put(AwsConstants.LONGITUDE, longitudeCondition);
        conditions.put(AwsConstants.EXPIRATION_TIMESTAMP, timeCondition);

        Collection<BlockedUser> blockedByUsers = getBlockedByUsers(userName);
        if (!blockedByUsers.isEmpty()) {
            List<AttributeValue> blockedByUserList = new LinkedList<AttributeValue>();
            for (BlockedUser user : blockedByUsers) {
                blockedByUserList.add(new AttributeValue().withS(user.getUserName()));
            }
            Condition filteredUserNameCondition = new Condition()
                    .withComparisonOperator(ComparisonOperator.NE.toString())
                    .withAttributeValueList(blockedByUserList);
            conditions.put(AwsConstants.USER_NAME, filteredUserNameCondition);
        }

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        scanExpression.setScanFilter(conditions);

        return mapper.scan(Shout.class, scanExpression);
    }

    /**
     * Determines the shouts that a given user has liked.
     *
     * @param shouts - the collection of shouts to check
     * @param userName - the userName to check for the shouts
     * @return - the same collection of shouts given, but with the proper 'liked' information
     */
    public Collection<Shout> getLikedShouts(Collection<Shout> shouts, String userName, Long timeConstraint) {
        Collection<AttributeValue> shoutIds = new HashSet<AttributeValue>();
        Map<String, Shout> shoutsById = new TreeMap<String, Shout>();
        for (Shout shout : shouts) {
            shoutIds.add(new AttributeValue().withS(shout.getId()));
            shoutsById.put(shout.getId(), shout);
        }

        Condition timeCondition = getTimeConstraintCondition(timeConstraint);

        Condition userNameCondition = getEqualConditionString(userName);

        Map<String, Condition> conditions = new HashMap<String, Condition>();
        conditions.put(AwsConstants.EXPIRATION_TIMESTAMP, timeCondition);
        conditions.put(AwsConstants.USER_NAME, userNameCondition);

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        scanExpression.setScanFilter(conditions);

        List<LikedShout> likedShouts = mapper.scan(LikedShout.class, scanExpression);
        for (LikedShout likedShout : likedShouts) {
            shoutsById.get(likedShout.getShoutId()).setLiked(true);
        }

        return shoutsById.values();
    }


    public Collection<LikedShout> getLikedShouts(String userName, String shoutId) {

        if (DataUtil.isEmpty(userName) && DataUtil.isEmpty(shoutId)) {
            return null;
        }

        Map<String, Condition> conditions = new HashMap<String, Condition>();
        if (!DataUtil.isEmpty(userName)) {
            Condition userNameCondition = getEqualConditionString(userName);
            conditions.put(AwsConstants.USER_NAME, userNameCondition);
        }

        if (!DataUtil.isEmpty(shoutId)) {
            Condition shoutIdCondition = getEqualConditionString(shoutId);
            conditions.put(AwsConstants.SHOUT_ID, shoutIdCondition);
        }

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        scanExpression.setScanFilter(conditions);

        return mapper.scan(LikedShout.class, scanExpression);
    }

    public Collection<Shout> postShout(Shout shout, Long timeConstraint, Double locationConstraint) {
        mapper.save(shout);

        return getShouts(shout.getLatitude(), shout.getLongitude(),
                timeConstraint, locationConstraint, shout.getUserName());
    }

    public void unblockUser(BlockedUser blockedUser) {
        mapper.delete(blockedUser);
    }

    public void blockUser(BlockedUser blockedUser) {
        mapper.save(blockedUser);
    }

    public Collection<BlockedUser> getBlockedUsers(String userName) {
        // set up the shoutId condition
        Condition userNameCondition = getEqualConditionString(userName);

        Map<String, Condition> conditions = new HashMap<String, Condition>();
        conditions.put(AwsConstants.USER_NAME, userNameCondition);

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        scanExpression.setScanFilter(conditions);

        return mapper.scan(BlockedUser.class, scanExpression);
    }

    public Collection<BlockedUser> getBlockedByUsers(String blockedUserName) {
        // set up the shoutId condition
        Condition userNameCondition = getEqualConditionString(blockedUserName);

        Map<String, Condition> conditions = new HashMap<String, Condition>();
        conditions.put(AwsConstants.BLOCKED_USER_NAME, userNameCondition);

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        scanExpression.setScanFilter(conditions);

        return mapper.scan(BlockedUser.class, scanExpression);
    }
    public void likeShout(LikedShout likedShout) {
        mapper.save(likedShout);
        updateShout(likedShout.getShoutId(), 1, 0, true);
    }

    public void unLikeShout(LikedShout likedShout) {
        mapper.delete(likedShout);
        updateShout(likedShout.getShoutId(), -1, 0, false);
    }

    public void likeComment(LikedComment likedComment) {
        mapper.save(likedComment);
        updateComment(likedComment.getCommentId(), 1);
    }

    public void unLikeComment(LikedComment likedComment) {
        mapper.delete(likedComment);
        updateComment(likedComment.getCommentId(), -1);
    }

    public void updateShout(String id, int numLikes, int numComments, boolean updateTimestamp) {
        Shout shout = getShoutFromId(id);
        shout.setNumLikes(shout.getNumLikes() + numLikes);
        shout.setNumComments(shout.getNumComments() + numComments);
        if (updateTimestamp) {
            shout.setExpirationTimestamp(System.currentTimeMillis() / 1000L);
        }
        mapper.save(shout);
    }

    public void updateComment(String id, int numLikes) {
        Comment comment = getCommentFromId(id);
        comment.setNumLikes(comment.getNumLikes() + numLikes);
        mapper.save(comment);
    }

    public User saveUser(User user) {
        mapper.save(user);
        return user;
    }


    public User getUser(String userName) {
        User user = mapper.load(User.class, userName);
//        if (user != null && !DataUtil.isEmpty(user.getUserName())) {
//            user.setBlockedUsers(getBlockedUsers(user.getUserName()));
//            user.setBlockedShouts(getBlockedShouts(user.getUserName()));
//        }
        return user;
    }

    public boolean checkUserName(String userName) {
        boolean isTaken = false;
        User user = getUser(userName);
        if (user != null && !DataUtil.isEmpty(user.getUserName())) {
            isTaken = true;
        }
        return isTaken;
    }


    public Collection<Comment> postComment(Comment comment) {
        mapper.save(comment);
        updateShout(comment.getShoutId(), 0, 1, true);
        return getShoutComments(comment.getShoutId());
    }

    public Shout getShoutFromId(String id) {
        return mapper.load(Shout.class, id);
    }

    public Comment getCommentFromId(String id) {
        return mapper.load(Comment.class, id);
    }

    /**
     * Returns all the comments for a given shout ID
     *
     * @param shoutId - the shout ID to query for comment shouts
     * @return all comments for the given shout
     */
    public Collection<Comment> getShoutComments(String shoutId) {
        // set up the shoutId condition
        Condition shoutIdCondition = getEqualConditionString(shoutId);

        Map<String, Condition> conditions = new HashMap<String, Condition>();
        conditions.put(AwsConstants.SHOUT_ID, shoutIdCondition);

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        scanExpression.setScanFilter(conditions);

        Collection<Comment> comments = mapper.scan(Comment.class, scanExpression);
        return new TreeSet<Comment>(comments);
    }

    /**
     * Returns all the registration IDs for the given phone IDs
     *
     * @param userNames - the phone IDs to retrieve registration Ids
     * @return all registration IDs for the given phone IDs
     */
    public Map<String, Collection<String>> getPushNotificationIds(Collection<String> userNames) {

        // set up the parentId condition
        Condition userNamesCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.IN.toString());

        List<AttributeValue> attributeValues = new LinkedList<AttributeValue>();
        for (String userName : userNames) {
            attributeValues.add(new AttributeValue().withS(userName));
        }

        userNamesCondition.withAttributeValueList(attributeValues);

        Map<String, Condition> keyConditions = new HashMap<String, Condition>();
        keyConditions.put(AwsConstants.USER_NAME, userNamesCondition);

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        scanExpression.setScanFilter(keyConditions);

        Collection<User> users = mapper.scan(User.class, scanExpression);

        // parse the registrationIds
        Collection<String> iosIds = new HashSet<String>();
        Collection<String> androidIds = new HashSet<String>();
        for (User user: users) {
            if (!DataUtil.isEmpty(user.getIosId())) {
                iosIds.add(user.getIosId());
            }
            if (!DataUtil.isEmpty(user.getAndroidId())) {
                androidIds.add(user.getAndroidId());
            }
        }
        Map<String, Collection<String>> pushNotificationIds = new HashMap<String, Collection<String>>();
        pushNotificationIds.put(AwsConstants.IOS_ID, iosIds);
        pushNotificationIds.put(AwsConstants.ANDROID_ID, androidIds);

        return pushNotificationIds;
    }

    public Collection<BlockedShout> getBlockedShouts(String userName) {
        Condition userNameCondition = getEqualConditionString(userName);

        Map<String, Condition> conditions = new HashMap<String, Condition>();
        conditions.put(AwsConstants.USER_NAME, userNameCondition);

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        scanExpression.setScanFilter(conditions);
        return mapper.scan(BlockedShout.class, scanExpression);
    }

    public Collection<BlockedComment> getBlockedComments(String userName) {
        Condition userNameCondition = getEqualConditionString(userName);

        Map<String, Condition> conditions = new HashMap<String, Condition>();
        conditions.put(AwsConstants.USER_NAME, userNameCondition);

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        scanExpression.setScanFilter(conditions);
        return mapper.scan(BlockedComment.class, scanExpression);
    }

    public void blockShout(BlockedShout blockedShout) {
        mapper.save(blockedShout);
    }

    public void unBlockShout(BlockedShout blockedShout) {
        mapper.delete(blockedShout);
    }

    private Condition getTimeConstraintCondition(Long timeConstraint) {
        // set up the time condition (> CURRENT_TIME - TIME_CONSTRAINT)
        // default the time constraint to 15 minutes
        if (timeConstraint == null) {
            timeConstraint = AwsConstants.DEFAULT_TIME_CONSTRAINT;
        }
        long timeFrame = (System.currentTimeMillis() / 1000L) - timeConstraint;

        return new Condition().withComparisonOperator(ComparisonOperator.GT.toString())
                .withAttributeValueList(new AttributeValue().withN(String.valueOf(timeFrame)));
    }

    private Condition getEqualConditionString(String userName) {
        return new Condition()
                .withComparisonOperator(ComparisonOperator.EQ.toString())
                .withAttributeValueList(new AttributeValue().withS(userName));
    }

    private Condition getBetweenCondition(Double location, Double locationConstraint) {
        if (locationConstraint == null) {
            locationConstraint = AwsConstants.DEFAULT_LOCATION_CONSTRAINT;
        }

        return new Condition().withComparisonOperator(ComparisonOperator.BETWEEN.toString())
                .withAttributeValueList(new AttributeValue().withN(String.valueOf(location - locationConstraint)),
                        new AttributeValue().withN(String.valueOf(location + locationConstraint)));
    }
}
