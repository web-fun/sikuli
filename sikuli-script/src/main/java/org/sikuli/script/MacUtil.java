package org.sikuli.script;

import java.io.*;
import java.awt.Window;
import javax.swing.JWindow;
import javax.swing.JOptionPane;
import java.awt.Rectangle;
import com.wapmx.nativeutils.jniloader.NativeLoader;
import com.sun.awt.AWTUtilities;

public class MacUtil implements OSUtil {

   private static boolean _askedToEnableAX = false;

   static {
      try{
         NativeLoader.loadLibrary("MacUtil");
         System.out.println("Mac OS X utilities loaded.");
      }
      catch(IOException e){
         e.printStackTrace();
      }
   }

   public int switchApp(String appName){
      return openApp(appName);
   }

   public int openApp(String appName){
      Debug.history("openApp: \"" + appName + "\"");
      if(_openApp(appName))
         return 0;
      return -1;
   }


   public int closeApp(String appName){
      Debug.history("closeApp: \"" + appName +"\"");
      try{
         String cmd[] = {"sh", "-c", 
            "ps aux |  grep " + appName + " | awk '{print $2}' | xargs kill"};
         System.out.println("closeApp: " + appName);
         Process p = Runtime.getRuntime().exec(cmd);
         p.waitFor();
         return p.exitValue();
      }
      catch(Exception e){
         return -1;
      }
   }

   private void checkAxEnabled(String name){
      if(!isAxEnabled()){
         Debug.error(name + " requires Accessibility API to be enabled!");
         if(_askedToEnableAX)
            return;
         int ret = JOptionPane.showConfirmDialog(null,
               "You need to enable Accessibility API to use the function \"" +
               name + "\".\n"+
               "Should I open te System Preferences for you?",
               "Accessibility API not enabled",
               JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
         if(ret == JOptionPane.YES_OPTION){
            openAxSetting();
            JOptionPane.showMessageDialog(null, 
                  "Check \"Enable access for assistant devices\""+
                  "in the System Preferences\n and then close this dialog.", 
                  "Enable Accessibility API", JOptionPane.INFORMATION_MESSAGE);
         }
         _askedToEnableAX = true;
      }
   }
   public Region getWindow(String appName, int winNum){
      checkAxEnabled("getWindow");
      long pid = getPID(appName);
      Rectangle rect = getRegion(pid, winNum);
      if(rect != null)
         return new Region(rect);
      return null;
   }

   public Region getWindow(String appName){
      return getWindow(appName, 0);
   }

   public Region getFocusedWindow(){
      checkAxEnabled("getFocusedWindow");
      Rectangle rect = getFocusedRegion();
      if(rect != null)
         return new Region(rect);
      return null;
   }

   public native void bringWindowToFront(JWindow win, boolean ignoreMouse);
   public static native boolean _openApp(String appName);
   public static native long getPID(String appName);
   public static native Rectangle getRegion(long pid, int winNum);
   public static native Rectangle getFocusedRegion();
   public static native boolean isAxEnabled();
   public static native void openAxSetting();

   public void setWindowOpacity(JWindow win, float alpha){
      win.getRootPane().putClientProperty("Window.alpha", new Float(alpha));
   }

   public void setWindowOpaque(JWindow win, boolean opaque){
      AWTUtilities.setWindowOpaque(win, opaque);
   }


} 
