package basekownledge.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * 序列化工具类
 * @author yuanfei0241@hsyuntai.com
 * @version V1.0.0
 * @title SerializeUtil
 * @date 2020/2/24
 */
public class SerializeUtil<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SerializeUtil.class);
    /**
     * 序列化操作
     * @param object-待序列化的对象
     * @title doSerialize
     * @author yuanfei0241@hsyuntai.com
     * @since v1.0.0
     * @return boolean
     */
    public static <T> boolean doSerialize(T object,String fileName){
        if(null == object){
            LOGGER.error("序列化的object对象为空!");
            return false;
        }
        if(StringUtils.isBlank(fileName)){
            LOGGER.error("序列化的fileName为空!");
            return false;
        }
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(fileName));
            oos.writeObject(object);
            LOGGER.info("object序列化成功!");
            return true;
        }catch (FileNotFoundException e) {
            LOGGER.error("fileName:{},序列化异常FileNotFoundException:{}",fileName,e);
        } catch (IOException e) {
            LOGGER.error("fileName:{},序列化异常IOException:{}",fileName,e);
        }finally {
            if(null != oos){
                try {
                    oos.close();
                } catch (IOException e) {
                    LOGGER.error("oos关闭异常!");
                }
            }
        }
        return false;
    }
    /**
     * 反序列化操作
     * @param fileName-文件名称
     * @title readSerializeObejct
     * @author yuanfei0241@hsyuntai.com
     * @since v1.0.0
     * @return
     */
    public static Object readSerializeObejct(String fileName){
        File file = new File(fileName);
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(file));
            Object result = ois.readObject();
            LOGGER.info("反序列化成功!");
            return result;
        } catch (IOException e) {
            LOGGER.error("反序列化异常，fileName:{},IOException异常:{}",fileName,e);
        } catch (ClassNotFoundException e) {
            LOGGER.error("反序列化异常，fileName:{},ClassNotFoundException异常:{}",fileName,e);
        }finally {
            if(null != ois){
                try {
                    ois.close();
                } catch (IOException e) {
                    LOGGER.error("ois关闭异常!");
                }
            }
        }
        return null;
    }
}
