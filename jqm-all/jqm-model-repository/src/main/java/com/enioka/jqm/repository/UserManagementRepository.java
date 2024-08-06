package com.enioka.jqm.repository;

import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.NoResultException;
import com.enioka.jqm.model.RRole;
import com.enioka.jqm.model.RUser;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha512Hash;
import org.apache.shiro.lang.util.ByteSource;

import java.util.List;

public class UserManagementRepository
{
    /**
     * Create a given security role if it does not already exists.
     *
     * @param cnx
     *            an open connection. No commit is done here.
     * @param roleName
     * @param description
     * @param permissions
     * @return
     */
    public static RRole createRoleIfMissing(DbConn cnx, String roleName, String description, String... permissions)
    {
        List<RRole> rr = RRole.select(cnx, "role_select_by_key", roleName);
        if (rr.size() == 0)
        {
            RRole.create(cnx, roleName, description, permissions);
            return RRole.select(cnx, "role_select_by_key", roleName).get(0);
        }
        return rr.get(0);
    }

    /**
     * Creates a new user if does not exist. If it exists, it is unlocked and roles are reset (password is untouched).
     *
     * @param cnx
     * @param login
     * @param password
     *            the raw password. it will be hashed.
     * @param description
     * @param roles
     */
    public static void createUserIfMissing(DbConn cnx, String login, String password, String description, String... roles)
    {
        try
        {
            long userId = cnx.runSelectSingle("user_select_id_by_key", Long.class, login);
            cnx.runUpdate("user_update_enable_by_id", userId);
            RUser.set_roles(cnx, userId, roles);
        }
        catch (NoResultException e)
        {
            String saltS = null;
            String hash = null;
            if (null != password && !"".equals(password))
            {
                ByteSource salt = new SecureRandomNumberGenerator().nextBytes();
                hash = new Sha512Hash(password, salt, 100000).toHex();
                saltS = salt.toHex();
            }

            RUser.create(cnx, login, hash, saltS, roles);
        }
    }
}
