package com.playposse.egoeater.backend.schema;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/**
 * An objectify entity to store the ranking between users.
 */
@Entity
@Cache
public class Ranking {

    @Id
    private Long id;
    @Index
    private Ref<EgoEaterUser> profileId;
    @Index
    private Ref<EgoEaterUser> ratedProfileId;
    private int wins = 0;
    private int losses = 0;
    @Index
    private int winsLossesSum = 0;

    public Ranking() {
    }

    public Ranking(Ref<EgoEaterUser> profileId, Ref<EgoEaterUser> ratedProfileId) {
        this.profileId = profileId;
        this.ratedProfileId = ratedProfileId;
    }

    public void registerWin() {
        wins++;
        winsLossesSum++;
    }

    public void registerLoss() {
        losses++;
        winsLossesSum--;
    }

    public Long getId() {
        return id;
    }

    public Ref<EgoEaterUser> getProfileId() {
        return profileId;
    }

    public Ref<EgoEaterUser> getRatedProfileId() {
        return ratedProfileId;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public int getWinsLossesSum() {
        return winsLossesSum;
    }
}
