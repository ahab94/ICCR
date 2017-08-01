package org.iotacontrolcenter.dto;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.BitSet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NeighborDto {

    private boolean active;
    private String  descr;
    private String  key;
    private String  name;
    private String  uri;
    private int     numAt = 0;
    private int     numIt = 0;
    private int     numNt = 0;

    @JsonSerialize(using = BitSetSerializer.class)
    @JsonDeserialize(using = BitSetDeserializer.class)
    private BitSet activity = new BitSet();

    private int iotaNeighborRefreshTime = 1;

    // Length in real time of a tick (minutes)
    private float iotaActivityGranularity;

    // Length in real time to keep history (two weeks in minutes)
    private final int activityRealTimeLength = 2 * 7 * 24 * 60;

    // Number of ticks to store activity
    private int activityTickLength;

    // How many times should the server refresh activity per tick
    public static final int ACTIVITY_REFRESH_SAMPLE_RATE = 10;

    public NeighborDto() {
    }

    public NeighborDto(String key, String uri, String name, String descr,
            boolean active, BitSet activity, int iotaNeighborRefreshTime,
            float iotaActivityGranularity) {
        this(key, uri, name, descr, active, activity, iotaNeighborRefreshTime);

        this.iotaActivityGranularity = iotaActivityGranularity;
    }

    public NeighborDto(String key, String uri, String name, String descr,
            boolean active, BitSet activity, int iotaNeighborRefreshTime) {
        this(key, uri, name, descr, active, iotaNeighborRefreshTime);

        this.activity = activity;
    }

    public NeighborDto(String key, String uri, String name, String descr,
            boolean active, int iotaNeighborRefreshTime) {
        this.key = key;
        this.name = name;
        this.descr = descr;
        this.active = active;
        this.uri = uri;
        this.activity = new BitSet();
        this.iotaNeighborRefreshTime = iotaNeighborRefreshTime;

        this.updateTickLenth();
    }

    private int calcActivityPercentageOverPeriod(ZonedDateTime dayAgo,
            ZonedDateTime now) {
        int startTick = this.getTickAtTime(dayAgo);
        int endTick = this.getTickAtTime(now);
        BitSet activity = this.activity.get(startTick, endTick);

        if (activity.length() < 1) {
            return 0;
        }
        else {
            return 100 * activity.cardinality() / activity.length();
        }
    }

    private ZonedDateTime currentDateTime() {
        return ZonedDateTime.now(ZoneOffset.UTC);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NeighborDto)) {
            return false;
        }

        NeighborDto that = (NeighborDto) o;

        boolean sameKey = getKey().equals(that.getKey());
        boolean sameUri = getUri().equals(that.getUri());
        boolean sameActivity = getActivity().hashCode() == that.getActivity()
                .hashCode();
        boolean sameRefreshTime = getIotaNeighborRefreshTime() == that
                .getIotaNeighborRefreshTime();

        boolean sameTransasctions = getNumAt() == that.getNumAt()
                && getNumIt() == that.getNumIt()
                && getNumNt() == that.getNumNt();

        return sameKey && sameUri && sameActivity && sameRefreshTime
                && sameTransasctions;
    }

    public BitSet getActivity() {
        return activity;
    }

    public int getActivityPercentageOverLastDay() {
        ZonedDateTime now = currentDateTime();
        ZonedDateTime dayAgo = now.minus(Period.ofDays(1));

        return calcActivityPercentageOverPeriod(dayAgo, now);
    }

    public int getActivityPercentageOverLastWeek() {
        ZonedDateTime now = currentDateTime();
        ZonedDateTime weekAgo = now.minus(Period.ofWeeks(1));

        return calcActivityPercentageOverPeriod(weekAgo, now);
    }

    public int getActivityRealTimeLength() {
        return activityRealTimeLength;
    }

    public float getActivityRefreshTime() {
        return iotaActivityGranularity / ACTIVITY_REFRESH_SAMPLE_RATE;
    }

    public float getActivityGranularity() {
        return iotaActivityGranularity;
    }

    public int getActivityTickLength() {
        return activityTickLength;
    }

    protected int getCurrentTick() {
        return this.getTickAtTime(currentDateTime());
    }

    public String getDescr() {
        return descr;
    }

    public int getIotaNeighborRefreshTime() {
        return iotaNeighborRefreshTime;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public int getNumAt() {
        return numAt;
    }

    public int getNumIt() {
        return numIt;
    }

    public int getNumNt() {
        return numNt;
    }

    protected int getTickAtTime(ZonedDateTime dayAgo) {

        // Two week span starting two Sundays ago
        ZonedDateTime two_sundays_ago = currentDateTime()
                .minus(Period.ofWeeks(1))
                .with(TemporalAdjusters.previous(DayOfWeek.SUNDAY)).withHour(0)
                .withMinute(0).withSecond(0).withNano(0);

        Duration location_in_period = Duration.between(two_sundays_ago, dayAgo);
        return (int) (location_in_period.toMinutes()
                / this.iotaActivityGranularity);

    }

    public String getUri() {
        return uri;
    }

    @Override
    public int hashCode() {
        int result = getUri().hashCode();
        result = 31 * result + getKey().hashCode();
        return result;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setActivity(BitSet activity) {
        this.activity = activity;
    }

    public void setActivityGranularity(float activityGranularity) {
        this.iotaActivityGranularity = activityGranularity;
        this.updateTickLenth();
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public void setIotaNeighborRefreshTime(int iotaNeighborRefreshTime) {
        if (iotaNeighborRefreshTime <= 0) {
            throw new IllegalArgumentException(
                    "Refresh time must be greater than 0");
        }
        this.iotaNeighborRefreshTime = iotaNeighborRefreshTime;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNumAt(int numAt) {
        // Update activity
        if (this.iotaActivityGranularity > 0 && numAt > this.numAt) {
            this.activity.set(this.getCurrentTick());
        }
        this.numAt = numAt;
    }

    public void setNumIt(int numIt) {
        this.numIt = numIt;
    }

    public void setNumNt(int numNt) {
        this.numNt = numNt;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    public String toString() {
        return "NeighborDto{" + "key='" + key + '\'' + ", name='" + name + '\''
                + ", descr='" + descr + '\'' + ", active='" + active + '\''
                + ", uri='" + uri + '\'' + '}';
    }

    private void updateTickLenth() {
        this.activityTickLength = (int) Math.ceil(
                this.activityRealTimeLength / this.iotaActivityGranularity);
    }
}
