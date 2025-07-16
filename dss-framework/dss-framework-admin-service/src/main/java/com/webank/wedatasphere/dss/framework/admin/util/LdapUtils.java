/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.webank.wedatasphere.dss.framework.admin.util;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.Hashtable;

public class LdapUtils {
    public static LdapContext connectLDAP(String adminName, String adminPassword, String ldapUrl) throws NamingException {

        Hashtable<String, String> HashEnv = new Hashtable<String, String>();
        HashEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        HashEnv.put(Context.SECURITY_AUTHENTICATION, "simple");// "none","simple","strong"
        HashEnv.put(Context.SECURITY_PRINCIPAL, adminName);
        HashEnv.put(Context.SECURITY_CREDENTIALS, adminPassword);
        HashEnv.put(Context.PROVIDER_URL, ldapUrl);

        LdapContext ctx = new InitialLdapContext(HashEnv, null);
        return ctx;
    }


    public static  void closeContext(LdapContext ctx) throws NamingException {
        if (ctx != null) {
            ctx.close();
        }
    }
}
