Web administration console
###############################

As all serious server-oriented middlewares, JQM is first and foremost a CLI (and configuration files) administered tool. However,
for a more Windows administrator-friendly experience, a web console is also offered. It allows every parameter modification (alter the definition of jobs, 
remove an engine node from the cluster, etc) and every client operation (new launch, consult launch history...) available.

It is **disabled by default**.

Enabling the console
*************************

The console must be enabled node by node. It should only be enabled on a single node, as one console is able to administer every node
referenced inside the central database.

For the GUI basic functions to work, the admin web service API must be enabled. See :doc:`security` for this. For full functionality the three WS APIs must be enabled.

A CLI shortcut is offered to enable all what is needed to use the GUI::

    ./jqm.sh enablegui <rootpassword>
    ./jqm.sh restart


First connection
*******************

Using either Internet Explorer (>= 10), Chrome (>= 25), Firefox (>= 28), connect to:

    http://servername:httpport (or, if SSL is enabled, https://...)

The port is written during node startup inside the JQM log.

Then click on "login", and submit authentication data for user "root" (its password can be reset through the CLI if needed).

Then head to the "users" tab, and create your own user with your own password and associate it with a suitable role.

If SSL is enabled
*******************

In that case, the recommended approach is to use a certificate to connect.

Head to the "users" tab and select your own user. In the lower right part of the page, click on "new certificate" and save the proposed file.

Unzip the file on your computer. For Linux and Unixes, then import the unzipped PFX file into your usual browser following its specific instructions.

For Windows, just double click on the PFX file, click next, next, enter the password (do NOT select make the key exportable), then 
next and accept everything. Restart IE or Chrome and farewell password prompts. (Won't work for Firefox, which has chosen to have its own certificate
store so either import it inside Firefox using the method on their website or use another browser)

.. note:: password for the PFX file is always "SuperPassword" without quotes.
