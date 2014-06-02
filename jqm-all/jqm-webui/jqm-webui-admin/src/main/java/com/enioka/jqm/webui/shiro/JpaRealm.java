package com.enioka.jqm.webui.shiro;

import java.util.Calendar;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.codec.Hex;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;

import com.enioka.jqm.api.AdminService;
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
    public boolean supports(AuthenticationToken token)
    {
        return token instanceof UsernamePasswordToken;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals)
    {
        return getUser((String) principals.fromRealm(getName()).iterator().next());
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException
    {
        UsernamePasswordToken t = (UsernamePasswordToken) token;
        return getUser(t.getUsername());
    }

    private SimpleAccount getUser(String login)
    {
        EntityManager em = AdminService.getEm();
        System.out.println(login);
        try
        {
            RUser user = em
                    .createQuery(
                            "SELECT u FROM RUser u WHERE UPPER(u.login) = UPPER(:l) AND NOT (u.password IS NULL AND u.certificateThumbprint IS NULL)",
                            RUser.class).setParameter("l", login).getSingleResult();

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
        finally
        {
            em.close();
        }
    }

}
