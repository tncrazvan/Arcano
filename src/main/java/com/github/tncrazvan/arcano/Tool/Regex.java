/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.Tool;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Administrator
 */
public interface Regex {
    
    /**
     * Matches a regular expression on the given subject String.
     * @param subject The string to analyze
     * @param regex Your regex
     * @param flags Regex flags
     * @return the first group matched
     */
    public static boolean match(String subject, String regex, int flags){
        Pattern pattern = Pattern.compile(regex, flags);
        Matcher matcher = pattern.matcher(subject);
        return matcher.find();
    }
    /**
     * Matches a regular expression on the given subject String.
     * @param subject The string to analyze
     * @param regex Your regex
     * @return the first group matched
     */
    public static boolean match(String subject, String regex){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(subject);
        return matcher.find();
    }
    
    /**
     * Extracts the nth occurrence of the given regular expression on the given subject String.
     * @param subject the input String.
     * @param regex your regular expression.
     * @param replacement replacement string.
     * @return the nth occurred String.
     */
    public static String replace(String subject,String regex,String replacement){
        return subject.replaceAll(regex, replacement);
    }
    
    /**
     * Extracts the nth occurrence of the given regular expression on the given subject String.
     * @param subject the input String.
     * @param regex your regular expression.
     * @param n the occurences counter.
     * @param flags Regex flags
     * @return the nth occurred String.
     */
    public static String group(String subject,String regex,int n, int flags){
        Pattern pattern = Pattern.compile(regex,flags);
        Matcher matcher = pattern.matcher(subject);
        if(matcher.find()){
            if(n < 0){
                n = matcher.groupCount() + n;
            }
            return matcher.group(n);
        }
        return null;
    }
    /**
     * Extracts the nth occurrence of the given regular expression on the given subject String.
     * @param subject the input String.
     * @param regex your regular expression.
     * @param n the occurences counter.
     * @return the nth occurred String.
     */
    public static String group(String subject,String regex,int n){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(subject);
        if(matcher.find()){
            if(n < 0){
                n = matcher.groupCount() + n;
            }
            return matcher.group(n);
        }
        return null;
    }
    
    /**
     * Extracts the first occurrence of the given regular expression on the given subject String.
     * @param subject
     * @param regex
     * @param flags Regex flags
     * @return the first occurred String.
     */
    public static String extract(String subject,String regex, int flags){
        return Regex.group(subject, regex, 0, flags);
    }
    
    /**
     * Extracts the first occurrence of the given regular expression on the given subject String.
     * @param subject
     * @param regex
     * @return the first occurred String.
     */
    public static String extract(String subject,String regex){
        return group(subject, regex, 0);
    }
}
