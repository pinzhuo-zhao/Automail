package simulation;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * @program: Automail
 * @description: Utility Class to read the configuration files
 * @author: Pinzhuo Zhao, StudentID:1043915
 * @create: 2021-04-07 17:56
 **/
public class ResourcesUtil {
    public static Properties readProperties(String fileName){
        Properties properties = new Properties();
        FileReader inStream = null;
        try {
            inStream = new FileReader(fileName);
            properties.load(inStream);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return properties;
    }
}
