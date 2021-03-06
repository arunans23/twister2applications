package edu.iu.dsc.tws.storm;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Random;

public class Utils {
  private static Logger LOG = LoggerFactory.getLogger(Utils.class);
  /**
   * Serialize an object using kryo and return the bytes
   * @param kryo instance of kryo
   * @param object the object to be serialized
   * @return the serialized bytes
   */
  public static byte[] serialize(Kryo kryo, Object object) {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    Output output = new Output(byteArrayOutputStream);
    kryo.writeObject(output, object);
    output.flush();
    return byteArrayOutputStream.toByteArray();
  }

  /**
   * De Serialize bytes using kryo and return the object
   * @param kryo instance of kryo
   * @param b the byte to be de serialized
   * @return the serialized bytes
   */
  public static Object deSerialize(Kryo kryo, byte []b, Class e) {
    return kryo.readObject(new Input(new ByteArrayInputStream(b)), e);
  }

  public static byte[] generateData(long size) {
    int s = (int) size;
    byte b[] = new byte[s];
    new Random().nextBytes(b);
    return b;
  }

  public static Option createOption(String opt, boolean hasArg, String description, boolean required) {
    Option symbolListOption = new Option(opt, hasArg, description);
    symbolListOption.setRequired(required);
    return symbolListOption;
  }

  public static void printTime(String id, int size, Long time, Long previousTime) {
    long now = System.nanoTime();
    long expired;
    if (previousTime == null) {
      expired = (now - time);
    } else {
      expired = (now - previousTime);
    }

    LOG.info("ID: " + id + "Total: " + (now - time) + " Time: " + expired);
    System.out.println("ID: " + id + " Size:" + size + " Total: " + (now - time) + " Time: " + expired);
  }
}
