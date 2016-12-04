package org.codewiz.droid64.emu;

public class Statistics {

    private long statisticsStartTime;
    private volatile int updateCounter;
    private volatile double updatesPerSecond;
    private volatile int updateValueCounter;

    public Statistics() {
        reset();
    }
    
    public void reset() {
        statisticsStartTime = System.currentTimeMillis();
        updatesPerSecond = 0.0;
        updateValueCounter = 0;
    }

    public void update() {
        update(1);
    }
    
    public void update(int increment) {
        
        updateCounter++;
        
        long currentTime = System.currentTimeMillis();
        double elapsedTime = (double) (currentTime - statisticsStartTime) / 1000.0;
        
        if (elapsedTime < 1.0) {
            return;
        }
        
        updatesPerSecond = (double) updateCounter / elapsedTime;
        updateValueCounter++;
        
        updateCounter = 0;
        statisticsStartTime = currentTime;
    }
    
    public boolean wasUpdated() {
        return (updateValueCounter > 0);
    }
    
    public double getUpdatesPerSecond() {
        
        if (updateValueCounter > 0) {
            updateValueCounter--;
        }
        
        return updatesPerSecond;
    }
    
}
