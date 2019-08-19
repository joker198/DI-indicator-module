package di_indicator;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
/**
 *
 * @author joker
 */
public class CalculateModule {
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final String[] DIRECTION = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
    public static final String COLOR[] = {"#808080", "#FF0000", "#FFFF00", "#00FF00", "#0000FF"};
    public static double flowSpeed[][] = {{16, 21, 35},{15, 22, 30},{13, 18, 25}};
    public static final double MAX_LEGAL_SPEED = 60;
    
    /**
     * Split duration between from "end_time" and before "duration" into array intervals
     *
     * @param duration
     * @param interval
     * @return
     * @throws ParseException 
     */
    public static String[] initSplitDuration(int duration, int interval) throws ParseException
    {
        String mileStones[] = initMileStoneArray(duration, interval);
        Calendar calendar = Calendar.getInstance();
        Date endTime = calendar.getTime();
        calendar.add(Calendar.MINUTE, -duration);
        Date startTime = calendar.getTime();
        calendar.setTime(startTime);
        mileStones[0] = DATE_FORMAT.format(startTime);
        for (int i = 1; i < mileStones.length-1; ++i) {
            calendar.add(Calendar.MINUTE, interval);
            mileStones[i] = DATE_FORMAT.format(calendar.getTime());
        }
        mileStones[mileStones.length-1] = DATE_FORMAT.format(endTime);
        return mileStones;
    }
    
    /**
     * Split duration between from "start_time" and after "duration" into array intervals
     *
     * @param start_time
     * @param duration
     * @param interval
     * @return
     * @throws ParseException 
     */
    public static String[] splitDuration(String start_time, int duration, int interval) throws ParseException
    {
        String mileStones[] = initMileStoneArray(duration, interval);
        mileStones[0] = start_time;
        Calendar calendar = Calendar.getInstance();
        Date startTime = DATE_FORMAT.parse(start_time);
        calendar.setTime(startTime);
        for (int i = 1; i < mileStones.length; ++i) {
            calendar.add(Calendar.MINUTE, interval);
            mileStones[i] = DATE_FORMAT.format(calendar.getTime());
        }
        return mileStones;
    }
    
    /**
     * mileStone array base on duration and intervals
     *
     * @param duration
     * @param interval
     * @return String array
     */
    public static String[] initMileStoneArray(int duration, int interval)
    {
        int intNums = duration/interval;
        float floatNums = (float)duration/interval;
        if (floatNums - intNums == 0) {
            return new String[intNums+1];
        }
        return new String[intNums+2];
    }
    
    /**
     * Map color base on Inflow and Main Direction
     *
     * @param speed
     * @param mainDirections
     * @return
     */
    public static double mapIndicator(double speed, int mainDirections)
    {
        if(mainDirections == 0 || speed >= MAX_LEGAL_SPEED) return 4;
        else if(mainDirections == 1) {
            if(speed > flowSpeed[0][2] && speed <= MAX_LEGAL_SPEED) return 4;
            else if(speed > flowSpeed[0][1] && speed <= flowSpeed[0][2]) return 3;
            else if(speed > flowSpeed[0][0] && speed <= flowSpeed[0][1]) return 2;
            else return 1;
        } else if(mainDirections == 2 || mainDirections == 3) {
            if(speed > flowSpeed[1][2] && speed <= MAX_LEGAL_SPEED) return 4;
            else if(speed > flowSpeed[1][1] && speed <= flowSpeed[1][2]) return 3;
            else if(speed > flowSpeed[1][0] && speed <= flowSpeed[1][1]) return 2;
            else return 1;
        } else if(mainDirections >= 4) {
            if(speed > flowSpeed[2][2] && speed <= MAX_LEGAL_SPEED) return 4;
            else if(speed > flowSpeed[2][1] && speed <= flowSpeed[2][2]) return 3;
            else if(speed > flowSpeed[2][0] && speed <= flowSpeed[2][1]) return 2;
            else return 1;
        }
        return 0;
    }
    
    /**
     * Find index of direction variable in DIRECTION array 
     *
     * @param direction
     * @return 
     */
    public static int directionIndex(String direction)
    {
        for (int i = 0; i < DIRECTION.length; i++) {
            if(direction.equals(DIRECTION[i])) return i;
        }
        return -1;
    }
    
    /**
     * Find y_axis of cells
     *
     * @param east
     * @param west
     * @param longitude
     * @param width
     * @return Integer
     */
    public static int whereX(double east, double west, double longitude, int width)
    {
        int result = (int) Math.floor((longitude - west) / (east - west) *width );
        if(result == width) {
            return result-1;
        }
        if(result < 0 || result > width) {
            return -1;
        }
        return result;
    }
    
    /**
     * Find y_axis of cells
     *
     * @param south
     * @param north
     * @param latitude
     * @param height
     * @return Integer
     */
    public static int whereY(double south, double north, double latitude, int height)
    {
        int result = (int) Math.floor((latitude - south) / (north - south)  * height);
        if(result == height) {
            return result-1;
        }
        if(result < 0 || result > height) {
            return -1;
        }
        return result;
    }
    
    /**
     * Calculate distance between 2 point base on latitude and longitude
     *
     * @param latitude1 latitude
     * @param longitude1 longitude
     * @param latitude2 latitude
     * @param longitude2 longitude
     * @return Double
     */
    public static double calculateDistance(double latitude1, double longitude1, double latitude2, double longitude2)
    {
        if (latitude1 == latitude2 && longitude1 == longitude2) {
            return 0;
        }
        double radlatitude1 = Math.PI * latitude1/180;
        double radlatitude2 = Math.PI * latitude2/180;
        double theta = longitude1-longitude2;
        double radtheta = Math.PI * theta / 180;
        double distance = Math.sin(radlatitude1) * Math.sin(radlatitude2) + Math.cos(radlatitude1) * Math.cos(radlatitude2) * Math.cos(radtheta);
        if (distance > 1) {
            distance = 1;
        }
        distance = Math.acos(distance);
        distance = distance * 180/Math.PI;
        distance = distance * 60 * 1.1515;
        return distance * 1609.344;
    }
    
    /**
     * Calculate size of a cell from grid size
     *
     * @param horizontal_distance
     * @param vertical_distance
     * @param cellSize
     * @return Array
     */
    public static int[] calculateCellSize(double horizontal_distance, double vertical_distance, int cellSize)
    {
        int cellSizes[] = {0, 0};
        cellSizes[0] = (int)Math.ceil(vertical_distance/cellSize);
        cellSizes[1] = (int)Math.ceil(horizontal_distance/cellSize);
        return cellSizes;
    }
}
