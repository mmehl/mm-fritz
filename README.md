# mm.fritz

A little program to enable and disable Filter rules in Fritz!Box.

The idea is:
* my son can disable the "Minecraft" filter, which blocks the ports to the Minecraft servers.
* the button can be pressed once a day (button press is logged to a database).
* the filter is automatically disabled after have an hour

The first try was with TR064, but this does not support changing filter rules.

This time I tried with simulating the UI with htmlunit.

The program has the following parts:
- Authenticate.java: check via LDAP if the user is allowed to use this function
- Data.java: log tried and successful to a mysql database
- EnableDisableMinecraft.java: the htmlunit code to simulate login and changing filters
- WebApp.java, index.html: a little servlet and html5 page as ui and controller 

## Dependencies

see ivy.xml

## Resorces

* [AVM API Description](http://avm.de/service/schnittstellen/) (German)
* [Examples](https://github.com/mirthas/FritzTR064/tree/master/examples)
