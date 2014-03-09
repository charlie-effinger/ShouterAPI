/*
 * $Id$
 * $HeadURL$
 */
package shouter.api.dao;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.*;
import shouter.api.beans.Shout;
import shouter.api.beans.User;

import java.util.*;

/**
 * Data access object for connecting with the Dynamo DB tables in AWS.
 *
 * @author Charles Effinger (charles.effinger@cbsinteractive.com)
 * @version $Revision$, $LastChangedDate$
 */
public class AwsDao {

    private final AmazonDynamoDB client;

    private final DynamoDBMapper mapper;

    // TODO: Get these out of Java code!!!
    private final String ACCESS_KEY = "AKIAJGBUWRJVYV2TZI5A";
    private final String SECRET_KEY = "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";

    private final String SHOUT_TABLE = "Shouts";

    private final String USER_TABLE = "Users";

    public static List<String> SHOUT_TABLE_PARAMS = Arrays.asList("id", "message", "timestamp", "phoneId",
            "parentId", "latitude", "longitude");

    public static List<String> USER_TABLE_PARAMS = Arrays.asList("phoneId", "firstName", "lastName", "registrationId");

    private final double DEFAULT_LOCATION_CONSTRAINT = 1.0 / 120.0;  // default location constraint is half a minute

    public AwsDao() {
        AWSCredentials credentials = new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);
        this.client = new AmazonDynamoDBClient(credentials);
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
    public Collection<Shout> getShouts(Double latitude, Double longitude) {
        // default the time constraint to 15 minutes
        // TODO: make this configurable by the user
        long timeFrame = (System.currentTimeMillis() / 1000L) - 900;

        // set up the latitude condition (between +/- LOCATION_CONSTRAINT)
        Condition latitudeCondition = new Condition().withComparisonOperator(ComparisonOperator.BETWEEN.toString())
                .withAttributeValueList(new AttributeValue().withN(String.valueOf(latitude - DEFAULT_LOCATION_CONSTRAINT)),
                        new AttributeValue().withN(String.valueOf(latitude + DEFAULT_LOCATION_CONSTRAINT)));

        // set up the longitude condition (between +/- LOCATION_CONSTRAINT)
        Condition longitudeCondition = new Condition().withComparisonOperator(ComparisonOperator.BETWEEN.toString())
                .withAttributeValueList(new AttributeValue().withN(String.valueOf(longitude - DEFAULT_LOCATION_CONSTRAINT)),
                        new AttributeValue().withN(String.valueOf(longitude + DEFAULT_LOCATION_CONSTRAINT)));

        // set up the time condition (> CURRENT_TIME - TIME_CONSTRAINT)
        Condition timeCondition = new Condition().withComparisonOperator(ComparisonOperator.GT.toString())
                .withAttributeValueList(new AttributeValue().withN(String.valueOf(timeFrame)));

        // build the condition map
        Map<String, Condition> conditions = new HashMap<String, Condition>();
        conditions.put("latitude", latitudeCondition);
        conditions.put("longitude", longitudeCondition);
        conditions.put("timestamp", timeCondition);

        // build the scan request
        ScanRequest scanRequest = new ScanRequest()
                .withTableName(SHOUT_TABLE)
                .withScanFilter(conditions)
                .withAttributesToGet(SHOUT_TABLE_PARAMS);

        // perform the scan
        ScanResult result = client.scan(scanRequest);

        // return the shouts after they have been parsed.
        return parseShouts(result.getItems());
    }

    public Collection<Shout> postShout(Shout shout) {
        try {
            mapper.save(shout);
        } catch (Exception ignore) { }

        return getShouts(shout.getLatitude(), shout.getLongitude());
    }

    public User createUser(User user) {
        try {
            mapper.save(user);
        }  catch (Exception ignore) { }
        return user;
    }

    public User authenticateUser(String phoneId) {
        Map<String, AttributeValue> key = new HashMap<String, AttributeValue>();
        key.put("phoneId", new AttributeValue().withS(phoneId));

        GetItemRequest getItemRequest = new GetItemRequest()
                .withTableName(USER_TABLE)
                .withKey(key)
                .withAttributesToGet(USER_TABLE_PARAMS);

        GetItemResult result = client.getItem(getItemRequest);

        User user = null;
        if (result.getItem() != null) {
            user = new User();
            if (result.getItem().containsKey("phoneId")) {
                user.setPhoneId(result.getItem().get("phoneId").getS());
            }

            if (result.getItem().containsKey("firstName")) {
                user.setFirstName(result.getItem().get("firstName").getS());
            }

            if (result.getItem().containsKey("lastName")) {
                user.setLastName(result.getItem().get("lastName").getS());
            }

            if (result.getItem().containsKey("registrationId")) {
                user.setRegistrationId(result.getItem().get("registrationId").getS());
            }
        }

        return user;
    }

    public Collection<Shout> postComment(Shout shout) {
        try {
            mapper.save(shout);
        } catch (Exception ignore) { }

        return getShoutComments(shout.getParentId());
    }

    public Shout getShoutFromId(String id) {

        Condition keyCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.EQ.toString())
                .withAttributeValueList(new AttributeValue().withS(id));

        Map<String, Condition> keyConditions = new HashMap<String, Condition>();
        keyConditions.put("id", keyCondition);

        QueryRequest queryRequest = new QueryRequest().withTableName(SHOUT_TABLE)
                .withKeyConditions(keyConditions)
                .withAttributesToGet(SHOUT_TABLE_PARAMS);

        QueryResult result = client.query(queryRequest);

        return parseShout(result.getItems().get(0));
    }

    /**
     * Returns all the comments for a given shout ID
     *
     * @param parentId - the shout ID to query for comment shouts
     * @return all comments for the given shout
     */
    public Collection<Shout> getShoutComments(String parentId) {

        // set up the parentId condition
        Condition parentIdCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.EQ.toString())
                .withAttributeValueList(new AttributeValue().withS(parentId));

        Map<String, Condition> keyConditions = new HashMap<String, Condition>();
        keyConditions.put("parentId", parentIdCondition);

        // build the scan request
        ScanRequest scanRequest = new ScanRequest()
                .withTableName(SHOUT_TABLE)
                .withScanFilter(keyConditions)
                .withAttributesToGet(SHOUT_TABLE_PARAMS);

        // retrieve the comments
        ScanResult result = client.scan(scanRequest);

        return parseShouts(result.getItems());
    }

    /**
     * Returns all the registration IDs for the given phone IDs
     *
     * @param phoneIds - the phone IDs to retrieve registration Ids
     * @return all registration IDs for the given phone IDs
     */
    public Collection<String> getRegistrationIds(Collection<String> phoneIds) {

        // set up the parentId condition
        Condition phoneIdsCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.IN.toString());

        List<AttributeValue> attributeValues = new LinkedList<AttributeValue>();
        for (String phoneId : phoneIds) {
            attributeValues.add(new AttributeValue().withS(phoneId));
        }

        phoneIdsCondition.withAttributeValueList(attributeValues);

        Map<String, Condition> keyConditions = new HashMap<String, Condition>();
        keyConditions.put("phoneId", phoneIdsCondition);

        // build the scan request
        ScanRequest scanRequest = new ScanRequest()
                .withTableName(USER_TABLE)
                .withScanFilter(keyConditions)
                .withAttributesToGet(Arrays.asList("registrationId"));

        // retrieve the comments
        ScanResult result = client.scan(scanRequest);

        // parse the registrationIds
        Collection<String> registrationIds = new HashSet<String>();
        for (Map<String, AttributeValue> item : result.getItems()) {
            registrationIds.add(item.get("registrationId").getS());
        }

        return registrationIds;
    }

    /**
     * Parses a ScanResult of shouts and
     *
     * @param shoutList the list of shouts to parse
     * @return  a collection of parsed shouts
     */
    private Collection<Shout> parseShouts(List<Map<String, AttributeValue>> shoutList) {
        Set<Shout> shouts = new TreeSet<Shout>(new TimestampComparator());
        for (Map<String, AttributeValue> shout : shoutList) {
            shouts.add(parseShout(shout));
        }

        return shouts;
    }

    private Shout parseShout(Map<String, AttributeValue> shoutMap) {
        Shout shout = new Shout();
        if (shoutMap != null) {
            if (shoutMap.containsKey("id")) {
                shout.setId(shoutMap.get("id").getS());
            }
            if (shoutMap.containsKey("message")) {
                shout.setMessage(shoutMap.get("message").getS());
            }
            if (shoutMap.containsKey("timestamp")) {
                shout.setTimestamp(Long.parseLong(shoutMap.get("timestamp").getN()));
            }
            if (shoutMap.containsKey("phoneId")) {
                shout.setPhoneId(shoutMap.get("phoneId").getS());
            }
            if (shoutMap.containsKey("parentId")) {
                shout.setParentId(shoutMap.get("parentId").getS());
            }
            if (shoutMap.containsKey("latitude")) {
                shout.setLatitude(Double.parseDouble(shoutMap.get("latitude").getN()));
            }
            if (shoutMap.containsKey("longitude")) {
                shout.setLongitude(Double.parseDouble(shoutMap.get("longitude").getN()));
            }
        }

        return shout;
    }


    /**
     * Custom comparator to sort sites by contextId, allowing for sites to be displayed
     * in ascending order in the response
     */
    public static class TimestampComparator implements Comparator<Shout> {

        @Override
        public int compare(Shout shout1, Shout shout2) {
            return shout2.getTimestamp().compareTo(shout1.getTimestamp());
        }

    }

}
