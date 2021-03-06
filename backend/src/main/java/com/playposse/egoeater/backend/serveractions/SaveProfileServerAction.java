package com.playposse.egoeater.backend.serveractions;

import com.google.api.server.spi.response.BadRequestException;
import com.playposse.egoeater.backend.beans.UserBean;
import com.playposse.egoeater.backend.firebase.NotifyProfileUpdatedFirebaseServerAction;
import com.playposse.egoeater.backend.schema.EgoEaterUser;

import java.io.IOException;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * A server action that saves the user's profile.
 */
public class SaveProfileServerAction extends AbstractServerAction {

    public static UserBean saveProfile(long sessionId, String profileText)
            throws BadRequestException, IOException {

        // Verify session id and find user.
        EgoEaterUser egoEaterUser = loadUser(sessionId);

        // Save profile.
        egoEaterUser.setProfileText(profileText);
        ofy().save().entity(egoEaterUser).now();

        NotifyProfileUpdatedFirebaseServerAction.notifyProfileUpdated(egoEaterUser.getId());

        return new UserBean(egoEaterUser);
    }
}
