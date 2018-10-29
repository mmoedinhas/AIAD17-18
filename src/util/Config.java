package util;
import java.util.Properties;

public class Config
{
   Properties configFile;
   public Config(String configFileName)
   {
    configFile = new java.util.Properties();
    try {
      configFile.load(this.getClass().getClassLoader().
      getResourceAsStream("resources/"+configFileName));
    }catch(Exception eta){
        eta.printStackTrace();
    }
   }

   public String getProperty(String key)
   {
    String value = this.configFile.getProperty(key);
    return value;
   }
}