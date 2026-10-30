package io.github.coderodde.zipfx;

public final class RemainingTime {

    private static final int SECONDS_PER_MINUTE = 60;
    private static final int MINUTES_PER_HOUR   = 60;

    private final int hours;
    private final int minutes;
    private final int seconds;

    public RemainingTime(int remainingTimeSeconds) {
        this.seconds =  remainingTimeSeconds % SECONDS_PER_MINUTE;
        
        this.minutes = (remainingTimeSeconds / SECONDS_PER_MINUTE) 
                                             % MINUTES_PER_HOUR;

        this.hours   = (remainingTimeSeconds / SECONDS_PER_MINUTE 
                                             / MINUTES_PER_HOUR);
    }
    
    @Override
    public String toString() {
        return "%d:%02d:%02d".formatted(hours, minutes, seconds);
    }
}