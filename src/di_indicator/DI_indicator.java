package di_indicator;

import static di_indicator.CalculateModule.DATE_FORMAT;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
/**
 *
 * @author joker
 */
public class DI_indicator {
    public static String markerUrl = "http://68.183.238.65:5000/api/data/markers";
    public static String rectangleUrl = "http://68.183.238.65:5000/api/data/rectangles";
    public static String cellsUrl = "http://68.183.238.65:5000/api/data/cells-detail";
    public static int count = 0, markerIndex = 0, ALGORITHM;
    public static String START_TIME;
    public static String END_TIME;
    public static final int CELLSIZE = 250;
    public static final int DURATION = 60;
    public static String[] mileStones;
    private static String mainKey = "b189e26c558284cf";
    private static String backupKey = "47009eebae23b690";

    public static enum Interval {
        TINY(5),SMALL(10), DEFAULT(15), MEDIUM(30);
        private final int value;
        private Interval(int value) {
            this.value = value;
        }
    }
    
    public DI_indicator(int algorithm)
    {
        try {
            mileStones = CalculateModule.initSplitDuration(DURATION, Interval.DEFAULT.value);
        } catch (ParseException ex) {
            Logger.getLogger(DI_indicator.class.getName()).log(Level.SEVERE, null, ex);
        }
        START_TIME = mileStones[0];
        END_TIME = mileStones[mileStones.length-1];
        ALGORITHM = algorithm;
    }

    public DI_indicator(int algorithm, String time)
    {
        try {
            mileStones = CalculateModule.splitDuration(time, DURATION,Interval.DEFAULT.value);
        } catch (ParseException ex) {
            Logger.getLogger(DI_indicator.class.getName()).log(Level.SEVERE, null, ex);
        }
        START_TIME = mileStones[0];
        END_TIME = mileStones[mileStones.length-1];
        ALGORITHM = algorithm;
    }
    
    /**
     * 
     * @param markers
     * @param rectangle
     * @param mileStonesIndex
     * @param mainDirection
     * @param speedByDirection
     * @param model
     * @return 
     * @throws org.json.JSONException 
     */
    public static JSONObject[] fitCellsByMileStones(Marker[] markers, Rectangle rectangle, int mileStonesIndex, int mainDirection[][], double speedByDirection[][], JSONArray model) throws JSONException{
        int id = rectangle.getId();
        int height = rectangle.getHeight(), width = rectangle.getWidth();
        int gridSize = height * width;
        double east = rectangle.getEast(), west = rectangle.getWest();
        double south = rectangle.getSouth(), north = rectangle.getNorth();
        Cells_detail cells[] = new Cells_detail[gridSize];
        RecordUsers[] getIn = new RecordUsers[gridSize];
        
        for (int i = 0; i < gridSize; i++) {
            cells[i] = new Cells_detail();
            getIn[i] = new RecordUsers(model);
        }
        mainDirection = new int[gridSize][CalculateModule.DIRECTION.length];
        speedByDirection = new double[gridSize][CalculateModule.DIRECTION.length];
        count = 0;
        int x, y, postion, i, directionIndex;
        Marker marker;
        
        for (i = markerIndex; i < markers.length; i++) {
            marker = markers[i];
            if (marker.getRecord_time().compareTo(mileStones[mileStonesIndex]) >= 0) break;
            
            x = CalculateModule.whereX(east, west, marker.getLng(), width);
            y = CalculateModule.whereY(south, north, marker.getLat(), height);
            if (x < 0 || y < 0) continue;
            postion = y * width + x;
            directionIndex = CalculateModule.directionIndex(marker.getDirect());
            mainDirection[postion][directionIndex]++;
            speedByDirection[postion][directionIndex] += marker.getSpeed();
            getIn[postion].turnStatus(marker.getRecord_user());
            if(cells[postion].getMarkerCount() == 0) {
                ++count;
                cells[postion].setX_axis(x);
                cells[postion].setY_axis(y);
                cells[postion].setAlgorithm(ALGORITHM);
                cells[postion].setId_cell(id);
                cells[postion].increaseMarkerCount();
                cells[postion].setStart_time(mileStones[mileStonesIndex-1]);
                cells[postion].setEnd_time(mileStones[mileStonesIndex]);
            } else {
                cells[postion].increaseMarkerCount();
            }
        }
        
        JSONObject result[] = new JSONObject[count];
        int mainDirectionCount;
        double speed, avg_speed, indicator;
        
        for (i = 0; i < gridSize; i++) {
            if(cells[i].getMarkerCount() > 0) {
                speed = 0;
                mainDirectionCount = 0;
                for (int j = 0; j < CalculateModule.DIRECTION.length; j++) {
                    if(mainDirection[i][j] > 0) {
                        mainDirectionCount++;
                        speedByDirection[i][j] = speedByDirection[i][j] / mainDirection[i][j];
                        speed += speedByDirection[i][j];
                    }
                }
                avg_speed = speed / mainDirectionCount;
                indicator = CalculateModule.mapIndicator(avg_speed, mainDirectionCount);
                
                cells[i].setAvg_speed(avg_speed);
                cells[i].setIndicator(indicator);
                cells[i].setMarkerCount(getIn[i].recordedUserCount());
//                if (getIn[i].recordedUserCount() > 1) cells[i].detail();
//                System.out.println(getIn[i].getValue()+" "+cells[i].getCoordinate());
                cells[i].setColor(CalculateModule.COLOR[(int)indicator]);
                cells[i].setAlgorithm_detail(AlgorithmDetail.toString(speedByDirection[i]));
                result[--count] = cells[i].cellToJSONObject(cells[i]);
            }
        }
        return result;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ParseException, InterruptedException, IllegalBlockSizeException, BadPaddingException {
//        Calendar calendar = Calendar.getInstance();//
//        Date startTime = DATE_FORMAT.parse("2019-08-18 14:10:00");//
//        calendar.setTime(startTime);//
//        for (int  index= 1; index <= 270; index++) {//
//            if(index % 9 == 0) Thread.sleep(30000);//
//            calendar.add(Calendar.MINUTE, DURATION);//
//            
//            DI_indicator execute = new DI_indicator(2, DATE_FORMAT.format(calendar.getTime()) );
//            Cells_detail cellsDetail = new Cells_detail(cellsUrl);
//            Marker marker = new Marker(markerUrl, DI_indicator.START_TIME, DI_indicator.END_TIME);
//            Rectangle rectangle = new Rectangle(rectangleUrl);
//
//            JSONObject data;
//            int mainDirection[][] = null;//////////////////////////////////--main direction
//            double speedByDirection[][] = null;
//            try {
//                Rectangle[] rectangles = rectangle.getRectangles();
//                Marker[] markers = marker.getMarkers();
//                if (markers.length == 0 || rectangles.length == 0) continue ;
//                JSONArray diget;
//                for (int i = 1; i < mileStones.length; i++) {
//                    diget = new JSONArray();
//                    for (Rectangle aRectangle:rectangles) {
//                        JSONObject cells[] = DI_indicator.fitCellsByMileStones(markers, aRectangle, i, mainDirection, speedByDirection, new JSONArray(marker.getNumOfRecordUser()));
//                        for (JSONObject cell : cells) {
//                            diget.put(cell);
//                        }
//                    }
//                    data = new JSONObject();
//                    data.put("data", diget);
//                    //System.out.println(data);
//                    cellsDetail.postJsonToApi(data);
//                }
//            } catch (JSONException ex) {
//                Logger.getLogger(DI_indicator.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        System.out.println(DATE_FORMAT.format(calendar.getTime()));

        DI_indicator execute = new DI_indicator(2);
        Cells_detail cellsDetail = new Cells_detail(cellsUrl);
        Marker marker = new Marker(markerUrl, DI_indicator.START_TIME, DI_indicator.END_TIME);
        Rectangle rectangle = new Rectangle(rectangleUrl);

        JSONObject data;
        int mainDirection[][] = null;//////////////////////////////////--main direction
        double speedByDirection[][] = null;
        try {
            Rectangle[] rectangles = rectangle.getRectangles();
            Marker[] markers = marker.getMarkers();
            if (markers.length == 0 || rectangles.length == 0) return ;
            JSONArray diget;
            for (int i = 1; i < mileStones.length; i++) {
                diget = new JSONArray();
                for (Rectangle aRectangle:rectangles) {
                    JSONObject cells[] = DI_indicator.fitCellsByMileStones(markers, aRectangle, i, mainDirection, speedByDirection, new JSONArray(marker.getNumOfRecordUser()));
                    for (JSONObject cell : cells) {
                        diget.put(cell);
                    }
                }
                data = new JSONObject();
                data.put("data", diget);
                //System.out.println(data);
                cellsDetail.postJsonToApi(data);
            }
        } catch (JSONException ex) {
            Logger.getLogger(DI_indicator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
