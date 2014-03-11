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

import javax.xml.crypto.Data;
import java.util.*;
import java.util.concurrent.locks.*;

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
        // default the time constraint to 15 minutes
        if (timeConstraint == null) {
            timeConstraint = AwsConstants.DEFAULT_TIME_CONSTRAINT;
        }

        if (locationConstraint == null) {
            locationConstraint = AwsConstants.DEFAULT_LOCATION_CONSTRAINT;
        }
        long timeFrame = (System.currentTimeMillis() / 1000L) - timeConstraint;

        // set up the latitude condition (between +/- LOCATION_CONSTRAINT)
        Condition latitudeCondition = new Condition().withComparisonOperator(ComparisonOperator.BETWEEN.toString())
                .withAttributeValueList(new AttributeValue().withN(String.valueOf(latitude - locationConstraint)),
                        new AttributeValue().withN(String.valueOf(latitude + locationConstraint)));

        // set up the longitude condition (between +/- LOCATION_CONSTRAINT)
        Condition longitudeCondition = new Condition().withComparisonOperator(ComparisonOperator.BETWEEN.toString())
                .withAttributeValueList(new AttributeValue().withN(String.valueOf(longitude - locationConstraint)),
                        new AttributeValue().withN(String.valueOf(longitude + locationConstraint)));

        // set up the time condition (> CURRENT_TIME - TIME_CONSTRAINT)
        Condition timeCondition = new Condition().withComparisonOperator(ComparisonOperator.GT.toString())
                .withAttributeValueList(new AttributeValue().withN(String.valueOf(timeFrame)));

        List<AttributeValue> filterUserNames = getBlockedUserNames(userName);
        filterUserNames.addAll(getBlockedByUserNames(userName));
        Condition blockedUserNameCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.NE.toString())
                .withAttributeValueList(filterUserNames);

        // build the condition map
        Map<String, Condition> conditions = new HashMap<String, Condition>();
        conditions.put(AwsConstants.LATITUDE, latitudeCondition);
        conditions.put(AwsConstants.LONGITUDE, longitudeCondition);
        conditions.put(AwsConstants.EXPIRATION_TIMESTAMP, timeCondition);
        conditions.put(AwsConstants.USER_NAME, blockedUserNameCondition);

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        scanExpression.setScanFilter(conditions);

        Collection<Shout> shouts = mapper.scan(Shout.class, scanExpression);
        shouts = addLikes(shouts, userName);

        return shouts;
    }

    /**
     * Determines the shouts that a given user has liked.
     *
     * @param shouts - the collection of shouts to check
     * @param userName - the userName to check for the shouts
     * @return - the same collection of shouts given, but with the proper 'liked' information
     */
    private Collection<Shout> addLikes(Collection<Shout> shouts, String userName) {
        Collection<AttributeValue> shoutIds = new HashSet<AttributeValue>();
        Map<String, Shout> shoutsById = new TreeMap<String, Shout>();
        for (Shout shout : shouts) {
            shoutIds.add(new AttributeValue().withS(shout.getId()));
            shoutsById.put(shout.getId(), shout);
        }

        Condition shoutIdCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.IN.toString())
                .withAttributeValueList(shoutIds);
        Condition userNameCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.EQ.toString())
                .withAttributeValueList(new AttributeValue(userName));

        Map<String, Condition> conditions = new HashMap<String, Condition>();
        conditions.put(AwsConstants.SHOUT_ID, shoutIdCondition);
        conditions.put(AwsConstants.USER_NAME, userNameCondition);

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        scanExpression.setScanFilter(conditions);

        List<LikedShout> likedShouts = mapper.scan(LikedShout.class, scanExpression);
        for (LikedShout likedShout : likedShouts) {
            shoutsById.get(likedShout.getShoutId()).setLiked(true);
        }

        return shoutsById.values();
    }

    public Collection<Shout> postShout(Shout shout, Long timeConstraint, Double locationConstraint) {
        try {
            mapper.save(shout);
        } catch (Exception ignore) { }

        return getShouts(shout.getLatitude(), shout.getLongitude(),
                timeConstraint, locationConstraint, shout.getUserName());
    }

    public void unblockUser(BlockedUser blockedUser) {
        mapper.delete(blockedUser);
    }

    public void blockUser(BlockedUser blockedUser) {
        mapper.save(blockedUser);
    }

    public List<AttributeValue> getBlockedUserNames(String userName) {
        // set up the shoutId condition
        Condition userNameCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.EQ.toString())
                .withAttributeValueList(new AttributeValue().withS(userName));

        Map<String, Condition> conditions = new HashMap<String, Condition>();
        conditions.put(AwsConstants.USER_NAME, userNameCondition);

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        scanExpression.setScanFilter(conditions);

        Collection<BlockedUser> blockedUsers = mapper.scan(BlockedUser.class, scanExpression);

        List<AttributeValue> blockedUserNames = new LinkedList<AttributeValue>();
        for (BlockedUser blockedUser : blockedUsers) {
            blockedUserNames.add(new AttributeValue(blockedUser.getBlockedUserName()));
        }

        return blockedUserNames;
    }

    public List<AttributeValue> getBlockedByUserNames(String blockedUserName) {
        // set up the shoutId condition
        Condition userNameCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.EQ.toString())
                .withAttributeValueList(new AttributeValue().withS(blockedUserName));

        Map<String, Condition> conditions = new HashMap<String, Condition>();
        conditions.put(AwsConstants.BLOCKED_USER_NAME, userNameCondition);

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        scanExpression.setScanFilter(conditions);

        Collection<BlockedUser> blockedUsers = mapper.scan(BlockedUser.class, scanExpression);

        List<AttributeValue> blockedByUserNames = new LinkedList<AttributeValue>();
        for (BlockedUser blockedUser : blockedUsers) {
            blockedByUserNames.add(new AttributeValue(blockedUser.getBlockedUserName()));
        }

        return blockedByUserNames;
    }
    public void likeShout(LikedShout likedShout) {
        mapper.save(likedShout);

        //update expirationTimestamp and numLikes of shout
        Shout shout = getShoutFromId(likedShout.getShoutId());
        shout.setNumLikes(shout.getNumLikes()+1);
        shout.setExpirationTimestamp(System.currentTimeMillis() / 1000L);
        mapper.save(shout);
    }

    public void unLikeShout(LikedShout likedShout) {
        mapper.delete(likedShout);

        //update numLikes of shout
        Shout shout = getShoutFromId(likedShout.getShoutId());
        shout.setNumLikes(shout.getNumLikes()-1);
        mapper.save(shout);
    }

    public User saveUser(User user) {
        try {
            mapper.save(user);
        }  catch (Exception ignore) { }
        return user;
    }


    public User getUser(String userName) {
        return mapper.load(User.class, userName);
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
        try {
            mapper.save(comment);
            Shout shout = getShoutFromId(comment.getShoutId());
            shout.setNumComments(shout.getNumComments()+1);
            mapper.save(shout);

        } catch (Exception ignore) { }


        return getShoutComments(comment.getShoutId(), comment.getUserName());
    }

    public Shout getShoutFromId(String id) {
        return mapper.load(Shout.class, id);
    }

    /**
     * Returns all the comments for a given shout ID
     *
     * @param shoutId - the shout ID to query for comment shouts
     * @return all comments for the given shout
     */
    public Collection<Comment> getShoutComments(String shoutId, String userName) {
        // set up the shoutId condition
        Condition shoutIdCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.EQ.toString())
                .withAttributeValueList(new AttributeValue().withS(shoutId));

        List<AttributeValue> filterUserNames = getBlockedUserNames(userName);
        filterUserNames.addAll(getBlockedByUserNames(userName));

        Condition blockedUserNameCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.NE.toString())
                .withAttributeValueList(filterUserNames);

        Map<String, Condition> conditions = new HashMap<String, Condition>();
        conditions.put(AwsConstants.SHOUT_ID, shoutIdCondition);
        conditions.put(AwsConstants.USER_NAME, blockedUserNameCondition);

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        scanExpression.setScanFilter(conditions);

        return mapper.scan(Comment.class, scanExpression);
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
}
