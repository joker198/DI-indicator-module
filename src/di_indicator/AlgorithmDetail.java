/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di_indicator;

import static di_indicator.CalculateModule.DIRECTION;
import org.json.JSONException;
import org.json.JSONObject;
/**
 *
 * @author joker
 */
public class AlgorithmDetail
{

    /**
     *
     * @param speed
     * @return
     */
    public static String toString(double[] speed)
    {
        JSONObject result = new JSONObject();
        try {
            for(int i = 0; i < DIRECTION.length; ++i) {
                if(speed[i] > 0)
                    result.put(DIRECTION[i], speed[i]);
            }
            return result.toString();
        } catch (JSONException e) {
            System.out.println(e);
        }
        return "";
    }
}
