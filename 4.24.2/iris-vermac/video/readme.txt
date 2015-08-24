The server applications listed below are Tomcat applications 
and are run as a service.  The service script is in the root of this 
module and named, (appropriately) tomcat.  They require a properties
file for configuration.  This should be located in:
/etc/tms and named video.properties.

When run with the property proxy=true,
it acts as a proxy and connects to another video server. When proxy=false,
it connects directly to the cameras/encoders. It serves both images and streams.
A proxy instance must be setup to accept connections on port 80 since many
corporate firewalls block 8080.  Getting tomcat to bind to 80 proved more
difficult than simply setting up a firewall rule to forward port 80 traffic
to port 8080.  The following command will do the port forwarding:
/sbin/iptables -t nat -A PREROUTING -p tcp --dport 80 -j REDIRECT --to-port 8080
I haven't found a way to set it up so that this redirect is active when the
iptables service is restarted.  I just execute the command if I have to restart
iptables.
