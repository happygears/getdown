//
// Getdown - application installer, patcher and launcher
// Copyright (C) 2004-2016 Getdown authors
// https://github.com/threerings/getdown/blob/master/LICENSE

package com.threerings.getdown.tools;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;

import com.threerings.getdown.data.Digest;
import com.threerings.getdown.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Produces a signed hash of the appbase, appname, and image path to ensure that signed copies of
 * Getdown are not hijacked to run malicious code.
 */
public class AppletParamSigner
{
    public static void main (String[] args)
    {
        if (args.length != 7) {
            System.err.println("AppletParamSigner keystore storepass alias keypass " +
                               "appbase appname imgpath");
            System.exit(255);
        }

        String keystore = args[0];
        String storepass = args[1];
        String alias = args[2];
        String keypass = args[3];
        String appbase = args[4];
        String appname = args[5];
        String imgpath = args[6];
        String params = appbase + appname + imgpath;

        try (FileInputStream fis = new FileInputStream(keystore);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            KeyStore store = KeyStore.getInstance("JKS");
            store.load(bis, storepass.toCharArray());
            PrivateKey key = (PrivateKey)store.getKey(alias, keypass.toCharArray());
            Signature sig = Signature.getInstance(Digest.sigAlgorithm(Digest.VERSION));
            sig.initSign(key);
            sig.update(params.getBytes(UTF_8));
            String signed = Base64.encodeToString(sig.sign(), Base64.DEFAULT);
            System.out.println("<param name=\"appbase\" value=\"" + appbase + "\" />");
            System.out.println("<param name=\"appname\" value=\"" + appname + "\" />");
            System.out.println("<param name=\"bgimage\" value=\"" + imgpath + "\" />");
            System.out.println("<param name=\"signature\" value=\"" + signed + "\" />");

        } catch (Exception e) {
            System.err.println("Failed to produce signature.");
            e.printStackTrace();
        }
    }
}
