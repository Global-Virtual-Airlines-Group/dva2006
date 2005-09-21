// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.util;

import java.util.StringTokenizer;

import org.deltava.util.system.SystemData;

/**
 * A utility class for formatting Water Cooler message text.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CoolerFormat {

   /**
    * Emoticon names.
    */
   public static final String[] ICON_NAMES = {"smile", "wink", "cool", "frown", "eek", "mad", "redface", "confused",
         "rolleyes", "biggrin", "razz", "plotting", "judge", "slitwrists"};
   
   /**
    * Emoticon codes.
    */
   public static final String[] ICON_CODES = {":)", ";)", null, ":(", ":O", null, null, null, null, ":D", ":p", null, null, null};
   
   // Singleton constructor
   private CoolerFormat() {
   }
   
   /**
    * Formats a message by converting URLs to proper links, and inserting emoticon image tags.
    * @param msg the raw message text
    * @return the formatted message text
    */
   public static String formatWithEmoticons(String msg) {
      
      // Break out the string
      StringBuffer buf = new StringBuffer();
      StringTokenizer tkns = new StringTokenizer(msg, " \n", true);
      while (tkns.hasMoreTokens()) {
         String token = tkns.nextToken();
         if (token.startsWith("http://")) {
            buf.append("<a href=\"");
            buf.append(token);
            buf.append("\">");
            buf.append(StringUtils.stripInlineHTML(token));
            buf.append("</a>");
         } else if ((token.charAt(0) == ':') && (token.length() > 2) && (token.charAt(token.length() - 1) == ':')) {
            int iCode = StringUtils.arrayIndexOf(ICON_NAMES, token.substring(1, token.length() - 1));
            if (iCode != -1)
               buf.append(emoticonURL(ICON_NAMES[iCode]));   
            else
               buf.append(StringUtils.stripInlineHTML(token));
         } else if ((token.charAt(0) == ':') && (token.length() == 2)) {
            for (int x = 0; x < ICON_CODES.length; x++) {
               if (token.equals(ICON_CODES[x])) {
                  buf.append(emoticonURL(ICON_NAMES[x]));
                  break;
               }
            }
         } else
            buf.append((token.length() > 1) ? StringUtils.stripInlineHTML(token) : token);
      }
      
      // Return the string
      return buf.toString();
   }
   
   /**
    * Formats a message by converting URLs to proper links.
    * @param msg the raw message text
    * @return the formatted message text
    */
   public static String format(String msg) {

      // Break out the string
      StringBuffer buf = new StringBuffer();
      StringTokenizer tkns = new StringTokenizer(msg, " \n", true);
      while (tkns.hasMoreTokens()) {
         String token = tkns.nextToken();
         if (token.startsWith("http://")) {
            buf.append("<a href=\"");
            buf.append(token);
            buf.append("\">");
            buf.append(StringUtils.stripInlineHTML(token));
            buf.append("</a>");
         } else {
            buf.append((token.length() > 1) ? StringUtils.stripInlineHTML(token) : token);
         }
      }
      
      // Return the string
      return buf.toString();
   }
   
   private static String emoticonURL(String name) {
      StringBuffer imgbuf = new StringBuffer("<img src=\"");
      imgbuf.append(SystemData.get("path.img"));
      imgbuf.append("/cooler/emoticons/");
      imgbuf.append(name);
      imgbuf.append(".png\" border=\"0\" />");
      return imgbuf.toString();
   }
}