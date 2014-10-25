/**
 * Copyright © 2013 enioka. All rights reserved
 *
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
 */
package com.enioka.jqm.webui.shiro;

import java.util.Calendar;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.codec.Hex;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;

import com.enioka.jqm.api.Helpers;
import com.enioka.jqm.jpamodel.RPermission;
import com.enioka.jqm.jpamodel.RRole;
import com.enioka.jqm.jpamodel.RUser;

public class JpaRealm extends AuthorizingRealm
{
    public JpaRealm()
    {
        setName("database");
    }

    @Override
    protected void assertCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) throws AuthenticationException
    {
        if (token instanceof CertificateToken)
        {
            if (!((CertificateToken) token).getUserName().equals(info.getPrincipals().getPrimaryPrincipal()))
            {
                throw new IncorrectCredentialsException("certificate presented did not match the awaited username");
            }
            return;
        }
        super.assertCredentialsMatch(token, info);
    }

    @Override
    public boolean supports(AuthenticationToken token)
    {
        return (token instanceof UsernamePasswordToken) || (token instanceof CertificateToken);
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals)
    {
        return getUser((String) principals.fromRealm(getName()).iterator().next());
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException
    {
        if (token instanceof CertificateToken)
        {
            return getUser(((CertificateToken) token).getUserName());
        }
        UsernamePasswordToken t = (UsernamePasswordToken) token;
        return getUser(t.getUsername());
    }

    private SimpleAccount getUser(String login)
    {
        EntityManager em = null;
        try
        {
            em = Helpers.getEm();
            RUser user = em.createQuery("SELECT u FROM RUser u WHERE UPPER(u.login) = UPPER(:l)", RUser.class).setParameter("l", login)
                    .getSingleResult();

            // Credential is a password - in token, it is as a char array
            SimpleAccount res = new SimpleAccount(user.getLogin(), user.getPassword(), getName());

            if (user.getExpirationDate() != null)
            {
                res.setCredentialsExpired(user.getExpirationDate().before(Calendar.getInstance()));
            }
            else
            {
                // No limit = never expires
                res.setCredentialsExpired(false);
            }
            if (user.getHashSalt() != null)
            {
                res.setCredentialsSalt(ByteSource.Util.bytes(Hex.decode(user.getHashSalt())));
            }
            else
            {
                res.setCredentialsSalt(null);
            }
            res.setLocked(user.getLocked());

            // Roles
            for (RRole r : user.getRoles())
            {
                res.addRole(r.getName());
                for (RPermission p : r.getPermissions())
                {
                    res.addStringPermission(p.getName());
                }
            }
            return res;
        }
        catch (NoResultException e)
        {
            // No such user in realm
            return null;
        }
        catch (RuntimeException e)
        {
            e.printStackTrace();
            throw e;
        }
        finally
        {
            em.close();
        }
    }

}
