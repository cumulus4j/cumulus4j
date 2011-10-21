package org.polepos.teams.jdo.cumulus4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class JdoCumulus4j {

    private final static JdoCumulus4jSettings sSettings = new JdoCumulus4jSettings();

    public static JdoCumulus4jSettings settings() {
        return sSettings;
    }

    /**
     * runs the JDO enhancer
     * @param arguments
     */
    public static void main(String[] args) {


        if(args == null || args.length == 0){
            System.out.println("Supply the class");
        }

        enhanceObjectDB();
    }

    /**
     * ObjectDB is not supplied with the distribution
     * @throws Exception
     */
    private static void enhanceObjectDB(){

        String clazz = "com.objectdb.Enhancer";
        String method = "enhance";
        Class[] types = new Class[] {String.class};
        Object[] params = new Object[] {"org.polepos.teams.cumulus4j.data.*" };

        try{
            callByReflection(clazz, method, types, params);
        }catch(Exception e){
            System.out.println("ObjectDB libraries are not included");
            e.printStackTrace();
        }
    }

    private static void callByReflection(String enhancerClass, String enhancerMethod, Class[] parameterTypes, Object[] parameters) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException{
        Class clazz = Class.forName(enhancerClass);
        Method method = clazz.getMethod(enhancerMethod, parameterTypes);
        method.invoke(null, parameters);
    }

}
