/* 
 This file is part of the PolePosition database benchmark
 http://www.polepos.org

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public
 License along with this program; if not, write to the Free
 Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 MA  02111-1307, USA. */

package org.polepos.teams.jpa;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Christian Ernst
 */
public class JpaEnhancer {

    private String                   className;
    private String                   methodName;
    private Class[]                  types;
    private Object[]                 params;

    private final static Map<String, JpaEnhancer> enhancers = registerEnhancers();
    
    private JpaEnhancer(){
    }

    public JpaEnhancer(String enhancerClass, String enhancerMethod, Class[] parameterTypes,
        Object[] parameters) {
        className = enhancerClass;
        methodName = enhancerMethod;
        types = parameterTypes;
        params = parameters;
    }

    public static void main(String[] args) throws Exception{
        
        if(args == null || args.length == 0){
            System.err.println("Supply the name of the enhancer to org.polepos.teams.Jpa.JpaEnhancer#main()");
            printRegisteredEnhancers();
            return;
        }
        
        JpaEnhancer enhancer = enhancers.get(args[0]);
        if(enhancer == null){
            System.err.println("Enhancer " + args[0] + " is not registered in org.polepos.teams.Jpa.JpaEnhancer");
            printRegisteredEnhancers();
            return;
        }
        
        if(enhancer.isRunnable()){
            enhancer.run();
        }else{
            try {
                enhancer.callByReflection();
            } catch (Exception e) {
                System.err.println("Jpa enhancing was not possible with the supplied enhancer name.");
                e.printStackTrace();
                printRegisteredEnhancers();
            }
        }
    }
    
    private static void printRegisteredEnhancers(){
        System.err.println("The following enhancers are registered, but they are only");
        System.err.println("available if the respective Jars are present in /lib");
        for(String key : enhancers.keySet()){
            System.err.println(key);
        }
    }

    private void callByReflection() throws ClassNotFoundException, SecurityException,
        NoSuchMethodException, IllegalArgumentException, IllegalAccessException,
        InvocationTargetException {
        Class clazz = Class.forName(className);
        Method method = clazz.getMethod(methodName, types);
        method.invoke(null, params);
    }

    private final static Map<String, JpaEnhancer> registerEnhancers() {

        Map<String, JpaEnhancer> map = new HashMap<String, JpaEnhancer>();
        
        JpaEnhancer datanucleusEnhancer = new JpaEnhancer(){
        	 
        	public boolean isRunnable(){
        		return true;
        	}
        	
        	public void run(){
                
        		try {
					Class enhancerClass = Class.forName("org.datanucleus.enhancer.DataNucleusEnhancer");
					
					Method mainMethod = enhancerClass.getMethod("main", new Class[]{String[].class});
					
					mainMethod.invoke(null,new Object[]{new String[]{"-v","-d","bin","bin/org/polepos/teams/jpa/data/package.jdo"}});
					
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
            }
            
        };
        map.put("datanucleus", datanucleusEnhancer);
        
        JpaEnhancer openjpaEnhancer = new JpaEnhancer(){
       	 
        	public boolean isRunnable(){
        		return true;
        	}
        	
        	public void run(){
                
        		try {
					Class enhancerClass = Class.forName("org.apache.openjpa.enhance.PCEnhancer");
					
					Method mainMethod = enhancerClass.getMethod("main", new Class[]{String[].class});
					
					mainMethod.invoke(null,new Object[]{new String[]{"-d","bin"}});
					
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
            }
            
        };
        map.put("openjpa", openjpaEnhancer);
        
        // TODO: add more enhancers here and register them like above

        return map;
    }

    public void run() {
        // virtual method to override 
    }
    
    public boolean isRunnable() {
        // virtual method to override
        return false;
    }

}
