package oldproject.client;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class DB  {

    public static void writeObjectToFile(UserInfo userInfo, String filepath){
        try {

            FileOutputStream fileOut = new FileOutputStream(filepath);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(userInfo);
            objectOut.close();
            System.out.println("The Object was successfully written to a file");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static UserInfo readObjectFromFile(String filepath) {
        try {
            FileInputStream fis = new FileInputStream(filepath);
            ObjectInputStream objectIn = new ObjectInputStream(fis);
            return (UserInfo) objectIn.readObject();

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
