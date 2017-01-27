package com.squareup.picasso;

/**
 * Since Picasso does not expose any method to clear its cache, we need to make a hack to do so.
 * 
 * Actually it contains a method Cache::clear(), but is is not accessible to the clients since it uses no modifier. 
 * According to the documentation, when a member level has no modifier, it is define as package private, that is it 
 * will be visible in the whole package, and only inside there.
 * 
 * So, in order to access it, we can create a class in the same package name (com.squareup.picasso) and all the 
 * methods on this class will have free access to all package private methods.
 * 
 * See this link for details: http://stackoverflow.com/questions/22016382/invalidate-cache-in-picasso/23544650#23544650
 *
 */
public class PicassoTools {

    public static void clearCache (Picasso p) {
        p.cache.clear();
    }
}
