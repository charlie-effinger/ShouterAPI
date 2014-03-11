/*
 * $Id$
 * $HeadURL$
 */
package shouter.api.servlets;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import shouter.api.handlers.BaseApiHandler;
import shouter.api.handlers.shout.CreateHandler;
import shouter.api.handlers.shout.SearchHandler;
import shouter.api.handlers.shout.like.LikeHandler;
import shouter.api.handlers.shout.like.UnLikeHandler;
import shouter.api.handlers.user.AuthenticateHandler;
import shouter.api.handlers.user.BlockHandler;
import shouter.api.handlers.user.UnBlockHandler;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * The servlet for the Shouter API. Handles all shout and user requests.
 *
 * @author Charles Effinger (charles.effinger@cbsinteractive.com)
 * @version $Revision$, $LastChangedDate$
 */
public class ShouterService extends HttpServlet {

    // Endpoint enum used for determining which handler to use.
    private static enum ServiceMethod {
        /**
         * Method used to create a new shout.
         */
        SHOUT_CREATE(Arrays.asList("/shout/create", "/shout/create/")),
        /**
         * Method used to search for shouts.
         */
        SHOUT_SEARCH(Arrays.asList("/shout/search", "/shout/search/")),

        /**
         * Method used to create a new comment.
         */
        COMMENT_CREATE(Arrays.asList("/shout/comment/create", "/shout/comment/create/")),
        /**
         * Method used to search for comments.
         */
        COMMENT_SEARCH(Arrays.asList("/shout/comment/search", "/shout/comment/search/")),
        /**
         * Method used to authenticate a user.
         */
        /**
         * Method used to create a new shout.
         */
        LIKE_SHOUT(Arrays.asList("/shout/Like", "/shout/like/")),
        /**
         * Method used to search for shouts.
         */
        UN_LIKE_SHOUT(Arrays.asList("/shout/unlike", "/shout/unlike/")),
        /**
         * Method used to authenticate a user
         */
        USER_AUTHENTICATE(Arrays.asList("/user/authenticate", "/user/authenticate/")),
        /**
         * Method used to create a user.
         */
        USER_CREATE(Arrays.asList("/user/create", "/user/create/")),
        /**
         * Method used to update a user.
         */
        USER_UPDATE(Arrays.asList("/user/update", "/user/update/")),
        /**
         * Method used to authenticate a user.
         */
        BLOCK_USER(Arrays.asList("/user/block", "/user/block/")),
        /**
         * Method used to update a user.
         */
        UN_BLOCK_USER(Arrays.asList("/user/unblock", "/user/unblock/"));

        private final Collection<String> paths;
        private static final Map<String, ServiceMethod> methodsByPath = new HashMap<String, ServiceMethod>();

        static {
            initializeMethodsByPath();
        }

        private ServiceMethod(Collection<String> paths) {
            this.paths = paths;
        }

        public Collection<String> getPaths() {
            return paths;
        }

        private static void initializeMethodsByPath() {
            for (ServiceMethod method : values()) {
                for (String path : method.getPaths()) {
                    methodsByPath.put(path.toLowerCase(), method);
                }
            }
        }

        public static ServiceMethod getMethodByPath(String path) {
            path = (path == null) ? "" : path;
            return methodsByPath.get(path.toLowerCase());
        }
    }

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json"); // set the response format to Json
        PrintWriter writer = null;
        try {
            long startTime = System.currentTimeMillis();

            // determine the proper handler
            String path = req.getPathInfo();
            BaseApiHandler handler = determineHandler(path, req);

            if (handler != null) {
                // do the request
                handler.handleRequest();

                // set up the response
                writer = resp.getWriter();

                // set up the Jackson parser
                ObjectMapper mapper = new ObjectMapper();
                mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                Map<String, Object> responseMap = new TreeMap<String, Object>();

                responseMap.put(handler.getResponseString(), handler.getResponseObjects());

                // build the json response
                mapper.writeValue(writer, responseMap);
            } else {
                throw new Exception();
            }

       //     logger.info("Completed request with query String: {} in {} ms", req.getQueryString(), (System.currentTimeMillis() - startTime));
        } catch (Exception e) {
       //     logger.error("Caught an IOException", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unrecognized request.");
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        doGet(req, resp);
    }

    /**
     * Determines the proper handler from the given path
     *
     * @param path the path of the request
     * @param request the HttpServletRequest object to interrogate
     *
     * @return the proper handler (or null)
     * @throws Exception
     */
    private BaseApiHandler determineHandler(String path, HttpServletRequest request)
            throws Exception {
        BaseApiHandler handler = null;

        ServiceMethod method = ServiceMethod.getMethodByPath(path);

        if (method != null) {
            switch (method) {
                case SHOUT_CREATE:
                    handler = new CreateHandler(request);
                    break;
                case SHOUT_SEARCH:
                    handler = new SearchHandler(request);
                    break;
                case COMMENT_CREATE:
                    handler = new shouter.api.handlers.shout.comment.CreateHandler(request);
                    break;
                case COMMENT_SEARCH:
                    handler = new shouter.api.handlers.shout.comment.SearchHandler(request);
                    break;
                case LIKE_SHOUT:
                    handler = new LikeHandler(request);
                    break;
                case UN_LIKE_SHOUT:
                    handler = new UnLikeHandler(request);
                    break;
                case USER_AUTHENTICATE:
                    handler = new AuthenticateHandler(request);
                    break;
                case USER_CREATE:
                    handler = new shouter.api.handlers.user.CreateHandler(request);
                    break;
                case USER_UPDATE:
                    handler = new shouter.api.handlers.user.UpdateHandler(request);
                    break;
                case BLOCK_USER:
                    handler = new BlockHandler(request);
                    break;
                case UN_BLOCK_USER:
                    handler = new UnBlockHandler(request);
                    break;

            }
        }

        return handler;
    }

}
